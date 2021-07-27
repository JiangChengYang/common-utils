package com.common.utils.secure;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.Cookie;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccessToken {
    @JsonIgnore
    public static ObjectMapper objectMapper;
    @JsonIgnore
    public static SecureProperties secureProperties;

    private String id;
    private Long subjectId;
    private String subjectName;
    private Map<String, String> subjectData;
    private boolean identified;
    private long createTime;
    private long lastTime;

    public static AccessToken create(long subjectId) {
        AccessToken accessToken = create();
        accessToken.setSubjectId(subjectId);
        return accessToken;
    }

    public static AccessToken create(long subjectId, String subjectName) {
        AccessToken accessToken = create(subjectId);
        accessToken.setSubjectName(subjectName);
        return accessToken;
    }

    public static AccessToken create() {
        AccessToken accessToken = new AccessToken();
        accessToken.setId(UUID.randomUUID().toString());
        accessToken.setCreateTime(Instant.now().getEpochSecond());
        accessToken.setLastTime(Instant.now().getEpochSecond());
        return accessToken;
    }

    public AccessToken touch() {
        identified = isAuthenticated();
        this.lastTime = Instant.now().getEpochSecond();
        return this;
    }

    @JsonIgnore
    public boolean isAuthenticated() {
        return identified && !isExpired();
    }

    @JsonIgnore
    private boolean isExpired() {
        return this.lastTime + secureProperties.getExpireAfter() < Instant.now().getEpochSecond();
    }

    public AccessToken withData(Map<String, String> data) {
        this.subjectData = data;
        return this;
    }

    public AccessToken revokeAuthenticate() {
        this.identified = false;
        return this;
    }

    public AccessToken authenticate() {
        if (this.subjectId == null) throw new RuntimeException("required subjectId is not provide");

        this.identified = true;
        return this;
    }

    public AccessToken store() {
        WebContext.get().setAccessToken(this);
        return this;
    }

    public void write() {
        try {
            String token = AES.encrypt(objectMapper.writeValueAsString(this));

            if (secureProperties.getWorkMode() == SecureProperties.WorkMode.COOKIE) {
                if (WebContext.get().getRequest().getCookies() != null) {
                    for (Cookie cookie : WebContext.get().getRequest().getCookies()) {
                        if (cookie.getName().equals(secureProperties.getTokenSymbol())) {
                            cookie.setMaxAge(0);
                            cookie.setValue(null);
                            WebContext.get().getResponse().addCookie(cookie);
                        }
                    }
                }

                Cookie cookie = new Cookie(secureProperties.getTokenSymbol(), token);
                cookie.setDomain(secureProperties.getCookieDomain());
                cookie.setPath(secureProperties.getCookiePath());
                cookie.setMaxAge(secureProperties.getCookieAge());
                cookie.setHttpOnly(true);
                WebContext.get().getResponse().addCookie(cookie);
                return;
            }

            WebContext.get().getResponse().setHeader("Access-Control-Expose-Headers", secureProperties.getTokenSymbol());
            WebContext.get().getResponse().setHeader(secureProperties.getTokenSymbol(), token);
        } catch (Throwable e) {
            log.error("token write error", e);
        }
    }
}
