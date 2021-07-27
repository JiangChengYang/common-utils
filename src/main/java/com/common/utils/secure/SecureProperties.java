package com.common.utils.secure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@Data
@RefreshScope
@ConfigurationProperties(prefix = SecureProperties.PREFIX)
public class SecureProperties {
    public static final String PREFIX = "secure";

    /**
     * permission key store in redis
     */
    private String permissionKey = "secure:pms";

    /**
     * permission cache expire seconds
     */
    private long permissionTTL = 300;

    /**
     * key for encrypt/decrypt client token
     */
    private String aesKey = "";

    /**
     * whether hide not exposed handler
     */
    private boolean hideNotExposedHandler = true;

    /**
     * token name
     */
    private String tokenSymbol = "X-Token";

    /**
     * token brings way
     */
    private WorkMode workMode = WorkMode.HEADER;

    /**
     * cookie domain value when WorkMode is COOKIE
     */
    private String cookieDomain = "";

    /**
     * cookie path value when WorkMode is COOKIE
     */
    private String cookiePath = "/";

    /**
     * cookie age value when WorkMode is COOKIE
     */
    private int cookieAge = -1;

    /**
     * token expired seconds after last access time
     */
    private long expireAfter = 172800;

    public enum WorkMode {
        HEADER, COOKIE
    }
}
