package org.fortishop.notificationservice.exception;

import org.fortishop.notificationservice.global.exception.BaseException;
import org.fortishop.notificationservice.global.exception.BaseExceptionType;

public class NotificationException extends BaseException {
    private final BaseExceptionType exceptionType;

    public NotificationException(BaseExceptionType exceptionType) {
        this.exceptionType = exceptionType;
    }

    @Override
    public BaseExceptionType getExceptionType() {
        return exceptionType;
    }
}
