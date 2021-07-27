package com.common.utils.secure;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

public class WebContext {
    private static final ThreadLocal<Context> THREAD_LOCAL = new InheritableThreadLocal<>();

    public static void reset() {
        THREAD_LOCAL.remove();
    }

    public static Context get() {
        return THREAD_LOCAL.get();
    }

    public static void set(Context context) {
        THREAD_LOCAL.set(context);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Context {
        private HttpServletRequest request;
        private HttpServletResponse response;
        private AccessToken accessToken;

        public Context(HttpServletRequest request, HttpServletResponse response) {
            this.request = request;
            this.response = response;
        }

        public Optional<AccessToken> getAccessToken() {
            return Optional.ofNullable(accessToken);
        }
    }
}
