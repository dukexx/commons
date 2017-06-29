package com.dukexx.commons.boot.shiro.exception;

/**
 * @author dukexx
 * @date 2017/4/14
 * @since 1.0.0
 */
public class IllegalShiroConfigException extends RuntimeException {
    public IllegalShiroConfigException() {
    }

    public IllegalShiroConfigException(String message) {
        super(message);
    }

    public IllegalShiroConfigException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalShiroConfigException(Throwable cause) {
        super(cause);
    }

    public IllegalShiroConfigException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
