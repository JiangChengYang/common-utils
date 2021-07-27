package com.common.utils.secure.filter;

import com.common.utils.secure.SecureManager;
import com.common.utils.secure.filter.annotation.Logical;
import com.common.utils.secure.filter.annotation.RequiresRoles;
import com.common.utils.secure.filter.exception.AuthorizingException;
import com.common.utils.secure.filter.exception.LacksPermissionException;
import com.google.common.base.Joiner;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.method.HandlerMethod;

public class RoleHandlerMethodInterceptor extends AuthenticationHandlerMethodInterceptor{
    public RoleHandlerMethodInterceptor() {
        super(RequiresRoles.class);
    }

    @Override
    public void checkPermission(HandlerMethod handlerMethod) throws AuthorizingException {
        super.checkPermission(handlerMethod);

        RequiresRoles permissionAnnotation = AnnotationUtils.findAnnotation(handlerMethod.getMethod(), RequiresRoles.class);
        permissionAnnotation = permissionAnnotation != null ? permissionAnnotation : AnnotationUtils.findAnnotation(handlerMethod.getBeanType(), RequiresRoles.class);

        if (permissionAnnotation.logical() == Logical.AND) {
            if (!SecureManager.hasRole(permissionAnnotation.value())) {
                throw new LacksPermissionException("lacks role,require:" + Joiner.on(",").join(permissionAnnotation.value()));
            }
            return;
        }

        if (!SecureManager.hasAnyRole(permissionAnnotation.value())) {
            throw new LacksPermissionException("lacks role,require:" + Joiner.on(",").join(permissionAnnotation.value()));
        }
    }
}
