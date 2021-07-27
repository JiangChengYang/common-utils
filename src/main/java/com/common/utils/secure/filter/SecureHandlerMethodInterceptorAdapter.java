package com.common.utils.secure.filter;

import com.common.utils.secure.filter.exception.AuthorizingException;
import com.common.utils.secure.filter.exception.NotExposedException;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.List;

@Slf4j
public class SecureHandlerMethodInterceptorAdapter extends HandlerInterceptorAdapter {
    private static final List<AbstractHandlerMethodInterceptor> HANDLER_METHOD_INTERCEPTORS = Lists.newArrayList(
            new GuestHandlerMethodInterceptor(),
            new UserHandlerMethodInterceptor(),
            new PermissionHandlerMethodInterceptor(),
            new RoleHandlerMethodInterceptor(),
            new AuthenticationHandlerMethodInterceptor()
    );
    private boolean hideNotExposedHandler;

    public SecureHandlerMethodInterceptorAdapter(boolean hideNotExposedHandler, Collection<AbstractHandlerMethodInterceptor> customizedHandlerMethodInterceptor) {
        this.hideNotExposedHandler = hideNotExposedHandler;
        if (CollectionUtils.isNotEmpty(customizedHandlerMethodInterceptor)) {
            HANDLER_METHOD_INTERCEPTORS.addAll(customizedHandlerMethodInterceptor);
        }
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) return true;

        int supportedInterceptor = 0;

        try {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            for (AbstractHandlerMethodInterceptor handlerMethodInterceptor : HANDLER_METHOD_INTERCEPTORS) {
                if (handlerMethodInterceptor.supported(handlerMethod)) {
                    supportedInterceptor++;
                    handlerMethodInterceptor.checkPermission(handlerMethod);
                    break;
                }
            }
        } catch (AuthorizingException e) {
            log.warn("secure chain prohibit this request", e);
            throw e;
        } catch (Throwable e) {
            log.error("secure chain execute occurs exception", e);
            throw e;
        }

        if (hideNotExposedHandler && supportedInterceptor == 0) {
            throw new NotExposedException("request URI is not exposed, you must annotate your controller method with any of secure annotations");
        }

        return true;
    }
}
