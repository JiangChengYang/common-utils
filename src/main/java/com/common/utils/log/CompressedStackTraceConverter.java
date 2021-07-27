package com.common.utils.log;

import ch.qos.logback.classic.pattern.ThrowableProxyConverter;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.core.CoreConstants;
import org.apache.commons.lang3.RegExUtils;

public class CompressedStackTraceConverter extends ThrowableProxyConverter {
    @Override
    protected String throwableProxyToString(IThrowableProxy tp) {
        return RegExUtils.replacePattern(super.throwableProxyToString(tp), "\\R", "\u2028") + CoreConstants.LINE_SEPARATOR;
    }
}
