package dev.gustavosa.urlshortener.service;

public class ShortUrlNotFoundException extends RuntimeException {

    public ShortUrlNotFoundException(String code) {
        super("código não encontrado: " + code);
    }
}
