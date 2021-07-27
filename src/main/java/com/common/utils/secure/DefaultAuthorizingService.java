package com.common.utils.secure;

import java.util.Set;

public class DefaultAuthorizingService implements AuthorizingService {
    @Override
    public Set<String> roles(long accountId) {
        return null;
    }

    @Override
    public Set<String> permissions(long accountId) {
        return null;
    }
}
