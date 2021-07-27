package com.common.utils.secure.filter;

import com.common.utils.secure.SecureManager;
import com.common.utils.secure.filter.annotation.RequiresUser;
import com.common.utils.secure.filter.exception.AuthorizingException;
import com.common.utils.secure.filter.exception.LacksIdentifyException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.method.HandlerMethod;

import java.lang.annotation.Annotation;

@Slf4j
public class UserHandlerMethodInterceptor extends AbstractHandlerMethodInterceptor{
    public UserHandlerMethodInterceptor() {
        super(RequiresUser.class);
    }

    public UserHandlerMethodInterceptor(Class<? extends Annotation> supportedAnnotation) {
        super(supportedAnnotation);
    }

    @Override
    public void checkPermission(HandlerMethod handlerMethod) throws AuthorizingException {
        if (!SecureManager.getSubjectId().isPresent()) {
            throw new LacksIdentifyException("lacks identify exception");
        }
    }
}
