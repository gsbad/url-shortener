-- Sequence que alimenta a geração dos códigos curtos.
--
-- Começa em 1.000.000 de propósito: em Base62 isso produz códigos de 4
-- caracteres desde o primeiro registro. Começando em 1, os primeiros códigos
-- teriam 1 caractere ("1", "2", ...), o que fica estranho e desperdiça o
-- espaço curto de nomes.
CREATE SEQUENCE short_code_seq START WITH 1000000 INCREMENT BY 1;

CREATE TABLE short_url (
    id         BIGSERIAL    PRIMARY KEY,
    code       VARCHAR(16)  NOT NULL,
    url        TEXT         NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- UNIQUE garante a invariante mesmo que a geração de código mude no futuro.
-- Também é o índice que atende a resolução por código, que é a leitura quente.
CREATE UNIQUE INDEX idx_short_url_code ON short_url (code);
