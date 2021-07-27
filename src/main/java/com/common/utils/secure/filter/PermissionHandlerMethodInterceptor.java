package com.common.utils.secure.filter;

import com.common.utils.secure.SecureManager;
import com.common.utils.secure.filter.annotation.Logical;
import com.common.utils.secure.filter.annotation.RequiresPermissions;
import com.common.utils.secure.filter.exception.AuthorizingException;
import com.common.utils.secure.filter.exception.LacksPermissionException;
import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;

@Slf4j
public class PermissionHandlerMethodInterceptor extends AuthenticationHandlerMethodInterceptor{
    public PermissionHandlerMethodInterceptor() {
        super(RequiresPermissions.class);
    }

    @Override
    public void checkPermission(HandlerMethod handlerMethod) throws AuthorizingException {
        super.checkPermission(handlerMethod);

        RequiresPermissions permissionAnnotation = AnnotationUtils.findAnnotation(handlerMethod.getMethod(), RequiresPermissions.class);
        permissionAnnotation = permissionAnnotation == null ? AnnotationUtils.findAnnotation(handlerMethod.getBeanType(), RequiresPermissions.class) : permissionAnnotation;

        assert permissionAnnotation != null;
        String[] requiredPmsCode = permissionAnnotation.value();
        if (ArrayUtils.isEmpty(requiredPmsCode)) {
            String defaultPmsCode = "";

            RequestMapping controllerMapping = AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getBeanType(), RequestMapping.class);
            if (controllerMapping != null) defaultPmsCode += Joiner.on(",").join(controllerMapping.value());

            RequestMapping methodMapping = AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getMethod(), RequestMapping.class);
            if (methodMapping != null) defaultPmsCode += Joiner.on(",").join(methodMapping.value());

            if (StringUtils.isNotBlank(defaultPmsCode)) {
                requiredPmsCode = new String[]{defaultPmsCode};
            }
        }

        if (permissionAnnotation.logical() == Logical.AND) {
            if (!SecureManager.hasPermission(requiredPmsCode)) {
                throw new LacksPermissionException("lacks permission,require:" + Joiner.on(",").join(requiredPmsCode));
            }
            return;
        }

        if (!SecureManager.hasAnyPermission(requiredPmsCode)) {
            throw new LacksPermissionException("lacks permission,require any of :" + Joiner.on(",").join(requiredPmsCode));
        }
    }
}
