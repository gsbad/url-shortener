package dev.gustavosa.urlshortener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.gustavosa.urlshortener.service.Base62;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Teste puro: sem Spring, sem banco. Roda em milissegundos.
 */
class Base62Test {

    @Test
    void codificaZero() {
        assertThat(Base62.encode(0)).isEqualTo("0");
    }

    @Test
    void codificaValoresConhecidos() {
        assertThat(Base62.encode(1)).isEqualTo("1");
        assertThat(Base62.encode(61)).isEqualTo("Z");
        assertThat(Base62.encode(62)).isEqualTo("10");
    }

    @Test
    void valorInicialDaSequenceProduzCodigoDeQuatroCaracteres() {
        // A migração começa a sequence em 1_000_000 justamente para isso.
        assertThat(Base62.encode(1_000_000)).hasSize(4);
    }

    @Test
    void naoProduzColisaoEmFaixaContigua() {
        Set<String> codigos = new HashSet<>();
        for (long i = 1_000_000; i < 1_010_000; i++) {
            codigos.add(Base62.encode(i));
        }
        assertThat(codigos).hasSize(10_000);
    }

    @Test
    void rejeitaNegativo() {
        assertThatThrownBy(() -> Base62.encode(-1))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
