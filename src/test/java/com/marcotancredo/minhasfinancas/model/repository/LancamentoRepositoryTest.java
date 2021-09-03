package com.marcotancredo.minhasfinancas.model.repository;

import com.marcotancredo.minhasfinancas.model.entity.Lancamento;
import com.marcotancredo.minhasfinancas.model.enums.StatusLancamento;
import com.marcotancredo.minhasfinancas.model.enums.TipoLancamento;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class LancamentoRepositoryTest {

    @Autowired
    LancamentoRepository repository;

    @Autowired
    TestEntityManager entityManager;

    @Test
    void deveSalvarUmLancamento(){
        Lancamento lancamento = createLancamento();

        lancamento = repository.save(lancamento);

        assertThat(lancamento.getId()).isNotNull();
    }

    public static Lancamento createLancamento() {
        return Lancamento.builder()
                .ano(2019)
                .mes(1)
                .descricao("Lançamento qualquer")
                .valor(BigDecimal.valueOf(10))
                .tipo(TipoLancamento.RECEITA)
                .status(StatusLancamento.PENDENTE)
                .dataCadastro(LocalDate.now())
                .build();
    }

    @Test
    void deveDeletarUmLancamento(){
        Lancamento lancamento = criarEPersistirUmLancamento();

        lancamento = entityManager.find(Lancamento.class, lancamento.getId());
        repository.delete(lancamento);

        Lancamento lancamentoInexistente = entityManager.find(Lancamento.class, lancamento.getId());
        assertThat(lancamentoInexistente).isNull();
    }

    private Lancamento criarEPersistirUmLancamento() {
        Lancamento lancamento = createLancamento();
        entityManager.persist(lancamento);
        return lancamento;
    }

    @Test
    void deveAtualizarUmLancamento(){
        Lancamento lancamento = criarEPersistirUmLancamento();

        lancamento.setAno(2018);
        lancamento.setDescricao("Teste Atualizar");
        lancamento.setStatus(StatusLancamento.CANCELADO);

        repository.save(lancamento);
        Lancamento lancamentoAtualizado = entityManager.find(Lancamento.class, lancamento.getId());

        assertThat(lancamentoAtualizado.getAno()).isEqualTo(2018);
        assertThat(lancamentoAtualizado.getDescricao()).isEqualTo("Teste Atualizar");
        assertThat(lancamentoAtualizado.getStatus()).isEqualTo(StatusLancamento.CANCELADO);
    }

    @Test
    void deveBuscarUmLancamentoPorId(){
        Lancamento lancamento = criarEPersistirUmLancamento();

        Optional<Lancamento> lancamentoEncontrado = repository.findById(lancamento.getId());

        assertThat(lancamentoEncontrado).isPresent();
    }
}
