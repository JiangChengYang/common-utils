package com.common.utils.secure;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RegExUtils;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@Slf4j
public class SecureFilter extends OncePerRequestFilter {
    public static final int DEFAULT_ORDER = Integer.MIN_VALUE + 50;
    private SecureProperties secureProperties;
    private ObjectMapper objectMapper;

    public SecureFilter(SecureProperties secureProperties, ObjectMapper objectMapper) {
        this.secureProperties = secureProperties;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        WebContext.set(new WebContext.Context(request, response));

        try {
            Optional<String> encryptToken = parseEncryptToken(request);

            if (encryptToken.isPresent()) {
                objectMapper.readValue(AES.decrypt(encryptToken.get()), AccessToken.class).touch().store();
            }
        } catch (Throwable e) {
            log.error("decrypt token error", e);
        }


        try {
            if (WebContext.get().getAccessToken().isPresent()){
                MDC.put("uId", String.valueOf(WebContext.get().getAccessToken().get().getSubjectId()));
                MDC.put("uName", RegExUtils.replaceAll(WebContext.get().getAccessToken().get().getSubjectName(),"\\R",""));

                WebContext.get().getAccessToken().get().write();
            }

            filterChain.doFilter(request, response);
        } catch (Throwable e) {
            log.error("filterChain execute error", e);
            throw e;
        } finally {
            try {
                WebContext.reset();
                MDC.clear();
            } catch (Throwable e) {
                log.error("WebContext.reset error", e);
            }
        }
    }

    private Optional<Cookie> getTokenCookie(HttpServletRequest request) {
        if (ArrayUtils.isEmpty(request.getCookies())) return Optional.empty();

        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals(secureProperties.getTokenSymbol())) return Optional.of(cookie);
        }
        return Optional.empty();
    }

    private Optional<String> parseEncryptToken(HttpServletRequest request) {

        Optional<String> tokenFromParameter = Optional.ofNullable(request.getParameter(secureProperties.getTokenSymbol()));
        if (tokenFromParameter.isPresent()) {
            return tokenFromParameter;
        }

        if (secureProperties.getWorkMode() == SecureProperties.WorkMode.COOKIE) {
            return getTokenCookie(request).map(Cookie::getValue);
        }

        return Optional.ofNullable(request.getHeader(secureProperties.getTokenSymbol()));
    }
}
