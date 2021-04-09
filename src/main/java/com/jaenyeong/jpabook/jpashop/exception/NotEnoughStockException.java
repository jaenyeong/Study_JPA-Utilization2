package com.jaenyeong.jpabook.jpashop.exception;

public final class NotEnoughStockException extends RuntimeException {

    public NotEnoughStockException() {
        super();
    }

    public NotEnoughStockException(final String message) {
    }

    public NotEnoughStockException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public NotEnoughStockException(final Throwable cause) {
        super(cause);
    }
}
