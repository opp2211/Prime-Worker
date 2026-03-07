package ru.maltsev.primeworker.integration.bybit;

public class BybitApiException extends RuntimeException{
    public BybitApiException(String message) {
        super(message);
    }
}
