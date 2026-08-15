package dev.gustavosa.urlshortener.controller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Corpo de {@code POST /api/urls}.
 *
 * <p>A validação exige http/https explícito. Aceitar esquema arbitrário
 * permitiria encurtar {@code javascript:} e {@code file:}, transformando a API
 * em vetor de redirecionamento malicioso.
 */
public record CreateUrlRequest(
        @NotBlank(message = "url é obrigatória")
        @Size(max = 2048, message = "url excede 2048 caracteres")
        @Pattern(regexp = "^https?://.+", message = "url deve começar com http:// ou https://")
        String url) {
}
