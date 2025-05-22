package org.fortishop.notificationservice.exception;

import org.fortishop.notificationservice.global.exception.BaseExceptionType;
import org.springframework.http.HttpStatus;

public enum NotificationExceptionType implements BaseExceptionType {
    NOTIFICATION_NOT_FOUND("N001", "해당 알림을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    ID_IS_EMPTY("N002", "알림 받을 ID를 찾을 수 없습니다.", HttpStatus.BAD_REQUEST),
    INVALID_REQUEST("N003", "잘못된 알림 생성 요청입니다.", HttpStatus.BAD_REQUEST),
    WRONG_ROLE("N004", "잘못된 권한입니다.", HttpStatus.BAD_REQUEST),
    TEMPLATE_NOT_FOUND("N005", "해당 알림 템플릿을  찾을 수 없습니다.", HttpStatus.NOT_FOUND);

    private final String errorCode;
    private final String errorMessage;
    private final HttpStatus httpStatus;

    NotificationExceptionType(String errorCode, String errorMessage, HttpStatus httpStatus) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.httpStatus = httpStatus;
    }

    @Override
    public String getErrorCode() {
        return this.errorCode;
    }

    @Override
    public String getErrorMessage() {
        return this.errorMessage;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return this.httpStatus;
    }
}
