package com.common.utils.secure.filter;

import com.common.utils.secure.SecureManager;
import com.common.utils.secure.filter.annotation.RequiresAuthentication;
import com.common.utils.secure.filter.exception.AuthorizingException;
import com.common.utils.secure.filter.exception.UnauthenticatedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.method.HandlerMethod;

import java.lang.annotation.Annotation;

@Slf4j
public class AuthenticationHandlerMethodInterceptor extends UserHandlerMethodInterceptor {
    public AuthenticationHandlerMethodInterceptor() {
        super(RequiresAuthentication.class);
    }

    public AuthenticationHandlerMethodInterceptor(Class<? extends Annotation> supportedAnnotation) {
        super(supportedAnnotation);
    }

    @Override
    public void checkPermission(HandlerMethod handlerMethod) throws AuthorizingException {
        try {
            super.checkPermission(handlerMethod);
        } catch (AuthorizingException e) {
            throw new UnauthenticatedException(e);
        }

        if (!SecureManager.isLogin()) throw new UnauthenticatedException("session is not authenticated!");
    }
}
