package dev.gustavosa.urlshortener.controller;

import dev.gustavosa.urlshortener.domain.ShortUrl;

public record ShortUrlResponse(String code, String url, String shortUrl) {

    public static ShortUrlResponse from(ShortUrl entity, String baseUrl) {
        return new ShortUrlResponse(
                entity.getCode(),
                entity.getUrl(),
                baseUrl + "/" + entity.getCode());
    }
}
