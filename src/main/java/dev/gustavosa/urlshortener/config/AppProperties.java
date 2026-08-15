package dev.gustavosa.urlshortener.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuração da aplicação, vinda do ambiente.
 *
 * @param environment ambiente corrente, só informativo no health check
 * @param baseUrl     base usada para montar a {@code shortUrl} devolvida pela API
 */
@ConfigurationProperties(prefix = "app")
public record AppProperties(String environment, String baseUrl) {
}
