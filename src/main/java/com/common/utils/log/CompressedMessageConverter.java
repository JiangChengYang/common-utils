package com.common.utils.log;

import ch.qos.logback.classic.pattern.MessageConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.CoreConstants;
import org.apache.commons.lang3.RegExUtils;

public class CompressedMessageConverter extends MessageConverter {
    @Override
    public String convert(ILoggingEvent event) {
        String original = RegExUtils.replacePattern(super.convert(event), "\\R", "\u2028");
        return event.getThrowableProxy() == null ? original + CoreConstants.LINE_SEPARATOR : original;
    }
}
