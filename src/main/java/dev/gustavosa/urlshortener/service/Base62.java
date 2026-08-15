package dev.gustavosa.urlshortener.service;

/**
 * Codificação Base62 — dígitos, minúsculas e maiúsculas.
 *
 * <p>Base62 evita os caracteres que precisariam de escape em URL, ao contrário
 * de Base64. O alfabeto começa pelos dígitos para que a ordem lexicográfica
 * dos códigos acompanhe a ordem numérica, o que facilita depurar.
 */
public final class Base62 {

    private static final String ALPHABET =
            "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int BASE = ALPHABET.length();

    private Base62() {
    }

    public static String encode(long value) {
        if (value < 0) {
            throw new IllegalArgumentException("valor não pode ser negativo: " + value);
        }
        if (value == 0) {
            return String.valueOf(ALPHABET.charAt(0));
        }

        StringBuilder out = new StringBuilder();
        long remaining = value;
        while (remaining > 0) {
            out.append(ALPHABET.charAt((int) (remaining % BASE)));
            remaining /= BASE;
        }
        return out.reverse().toString();
    }
}
