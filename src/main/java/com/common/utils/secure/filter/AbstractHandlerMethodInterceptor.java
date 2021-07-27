package com.common.utils.secure.filter;

import com.common.utils.secure.filter.exception.AuthorizingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.method.HandlerMethod;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

@Slf4j
public abstract class AbstractHandlerMethodInterceptor {
    private final Class<? extends Annotation> supportedAnnotation;

    protected AbstractHandlerMethodInterceptor(Class<? extends Annotation> supportedAnnotation) {
        this.supportedAnnotation = supportedAnnotation;
    }

    public boolean supported(HandlerMethod handlerMethod) {
        try {
            if (supportedAnnotation == null) return false;

            return containsClassAnnotation(handlerMethod.getBeanType(), supportedAnnotation) || containsMethodAnnotation(handlerMethod.getMethod(), supportedAnnotation);
        } catch (Throwable e) {
            log.error("AbstractHandlerMethodInterceptor supported", e);
            throw e;
        }
    }

    private boolean containsClassAnnotation(Class<?> clazz, Class<? extends Annotation> annotationClass) {
        return AnnotationUtils.findAnnotation(clazz, annotationClass) != null;
    }

    private boolean containsMethodAnnotation(Method method, Class<? extends Annotation> annotationClass) {
        return AnnotationUtils.findAnnotation(method, annotationClass) != null;
    }

    public abstract void checkPermission(HandlerMethod handlerMethod) throws AuthorizingException;
}
