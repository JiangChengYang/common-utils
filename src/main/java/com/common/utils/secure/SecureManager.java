package com.common.utils.secure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

@Slf4j
@Data
public class SecureManager {
    public static SecureProperties secureProperties;
    public static StringRedisTemplate redisTemplate;
    public static ObjectProvider<AuthorizingService> authorizingServiceProvider;
    public static ObjectMapper objectMapper;

    private static String getPmsKey(long accountId) {
        return "permissions:account_id:" + accountId;
    }

    private static String getRoleKey(long accountId) {
        return "roles:account_id:" + accountId;
    }

    public static void clearAllRoleAndPermission() {
        redisTemplate.delete(secureProperties.getPermissionKey());
    }

    public static void clearRoleAndPermission(long accountId) {
        redisTemplate.boundHashOps(secureProperties.getPermissionKey()).delete(getRoleKey(accountId), getPmsKey(accountId));
    }

    public static void clearRole(long accountId) {
        redisTemplate.boundHashOps(secureProperties.getPermissionKey()).delete(getRoleKey(accountId));
    }

    public static void clearPermission(long accountId) {
        redisTemplate.boundHashOps(secureProperties.getPermissionKey()).delete(getPmsKey(accountId));
    }

    public static Set<String> getPermission() {
        return getPms(
                SecureManager::getPmsKey,
                subjectId -> authorizingServiceProvider.getObject().permissions(subjectId)
        );
    }

    public static Set<String> getRole() {
        return getPms(
                SecureManager::getRoleKey,
                subjectId -> authorizingServiceProvider.getObject().roles(subjectId)
        );
    }

    public static boolean hasRole(String... role) {
        return getRole().containsAll(Arrays.asList(role));
    }

    public static boolean hasAnyRole(String... role) {
        return Stream.of(role).anyMatch(getRole()::contains);
    }

    public static boolean hasPermission(String... pms) {
        return getPermission().containsAll(Arrays.asList(pms));
    }

    public static boolean hasAnyPermission(String... pms) {
        return Stream.of(pms).anyMatch(getPermission()::contains);
    }

    public static boolean isLogin() {
        Optional<AccessToken> accessToken = WebContext.get().getAccessToken();
        return accessToken.isPresent() && accessToken.get().isAuthenticated();
    }

    public static Optional<Long> getSubjectId() {
        return WebContext.get().getAccessToken().map(AccessToken::getSubjectId);
    }

    public static Optional<String> getSubjectName() {
        return WebContext.get().getAccessToken().map(AccessToken::getSubjectName);
    }

    public static Map<String, String> getSubjectData() {
        Optional<AccessToken> accessToken = WebContext.get().getAccessToken();
        return accessToken.isPresent() ? accessToken.get().getSubjectData() : Maps.newHashMap();
    }

    public static Set<String> getPms(Function<Long, String> keySupplier, Function<Long, Set<String>> pmsSupplier) {
        if (!WebContext.get().getAccessToken().isPresent()) return Sets.newHashSet();

        Long subjectId = WebContext.get().getAccessToken().get().getSubjectId();
        if (subjectId == null) return Sets.newHashSetWithExpectedSize(0);

        BoundHashOperations<String, String, String> pmsStore = redisTemplate.boundHashOps(secureProperties.getPermissionKey());

        String pmsKey = getRoleKey(subjectId);
        if (BooleanUtils.isNotTrue(pmsStore.hasKey(pmsKey))) {
            Set<String> pms = pmsSupplier.apply(subjectId);
            if (pms == null) {
                pms = Sets.newHashSetWithExpectedSize(0);
            }

            try {
                pmsStore.put(pmsKey, objectMapper.writeValueAsString(new PmsCache(Instant.now().plusSeconds(secureProperties.getPermissionTTL()).getEpochSecond(), pms)));
            } catch (JsonProcessingException e) {
                log.error("save permission error", e);
            }
            return pms;
        }


        final String permissionValue = pmsStore.get(pmsKey);
        if (permissionValue == null) return getPms(keySupplier, pmsSupplier);

        PmsCache cachedPms;
        try {
            cachedPms = objectMapper.readValue(permissionValue, PmsCache.class);
        } catch (JsonProcessingException e) {
            log.error("read permission error", e);
            return Sets.newHashSet();
        }

        if (cachedPms.isExpired()) {
            log.info("permission cache expired, read new");

            redisTemplate.boundHashOps(secureProperties.getPermissionKey()).delete(pmsKey);
            return getPms(keySupplier, pmsSupplier);
        }

        return cachedPms.getPms();
    }

    public static void logout() {
        if (!WebContext.get().getAccessToken().isPresent()) return;

        WebContext.get().getAccessToken().get().revokeAuthenticate().store().write();
    }
}
