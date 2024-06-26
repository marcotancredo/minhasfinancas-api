package com.marcotancredo.minhasfinancas.model.service;

import com.marcotancredo.minhasfinancas.model.entity.Lancamento;
import com.marcotancredo.minhasfinancas.model.entity.Usuario;
import com.marcotancredo.minhasfinancas.model.enums.StatusLancamento;
import com.marcotancredo.minhasfinancas.model.exception.RegraNegocioException;
import com.marcotancredo.minhasfinancas.model.repository.LancamentoRepository;
import com.marcotancredo.minhasfinancas.model.repository.LancamentoRepositoryTest;
import com.marcotancredo.minhasfinancas.model.service.impl.LancamentoServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.domain.Example;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class LancamentoServiceTest {

    @SpyBean
    LancamentoServiceImpl service;
    @MockBean
    LancamentoRepository repository;

    @Test
    public void deveSalvarUmLancamento() {
        Lancamento lancamentoASalvar = LancamentoRepositoryTest.criaLancamento();

        doNothing().when(service).validar(lancamentoASalvar);

        Lancamento lancamentoSalvo = LancamentoRepositoryTest.criaLancamento();
        lancamentoSalvo.setId(1L);
        when(repository.save(lancamentoASalvar)).thenReturn(lancamentoSalvo);

        Lancamento lancamento = service.salvar(lancamentoASalvar);

        assertThat(lancamento.getId()).isEqualTo(lancamentoSalvo.getId());
        assertThat(lancamento.getStatus()).isEqualTo(StatusLancamento.PENDENTE);
    }

    @Test
    public void naoDeveSalvarUmLancamentoQuandoHouverErroValidacao() {
        Lancamento lancamentoASalvar = LancamentoRepositoryTest.criaLancamento();
        doThrow(RegraNegocioException.class).when(service).validar(lancamentoASalvar);

        Assertions.catchThrowableOfType(() -> service.salvar(lancamentoASalvar), RegraNegocioException.class);
        verify(repository, never()).save(lancamentoASalvar);
    }

    @Test
    public void deveAtualizarUmLancamento() {
        Lancamento lancamentoSalvo = LancamentoRepositoryTest.criaLancamento();
        lancamentoSalvo.setId(1L);
        lancamentoSalvo.setStatus(StatusLancamento.PENDENTE);

        doNothing().when(service).validar(lancamentoSalvo);

        when(repository.save(lancamentoSalvo)).thenReturn(lancamentoSalvo);

        Lancamento lancamento = service.atualizar(lancamentoSalvo);

        verify(repository, times(1)).save(lancamentoSalvo);
    }

    @Test
    public void deveLancarErroAoTentarAtualizarUmLancamentoQueAindaNaoFoiSalvo() {
        Lancamento lancamentoASalvar = LancamentoRepositoryTest.criaLancamento();

        Assertions.catchThrowableOfType(() -> service.atualizar(lancamentoASalvar), NullPointerException.class);
        verify(repository, never()).save(lancamentoASalvar);
    }

    @Test
    public void deveDeletarUmLancamento() {
        Lancamento lancamento = LancamentoRepositoryTest.criaLancamento();
        lancamento.setId(1L);

        service.deletar(lancamento);

        verify(repository, times(1)).delete(lancamento);
    }

    @Test
    public void deveLancarErroAoTentarDeletarUmLancamentoQueIandaNaoFoiSalvo(){
        Lancamento lancamento = LancamentoRepositoryTest.criaLancamento();

        Assertions.catchThrowableOfType(() -> service.deletar(lancamento), NullPointerException.class);

        verify(repository, never()).delete(lancamento);
    }

    @Test
    public void deveFiltrarLancamentos() {
        Lancamento lancamento = LancamentoRepositoryTest.criaLancamento();
        lancamento.setId(1L);

        List<Lancamento> lista = List.of(lancamento);
        when(repository.findAll(any(Example.class))).thenReturn(lista);

        List<Lancamento> resultado = service.buscar(lancamento);

        Assertions.assertThat(resultado).isNotEmpty().hasSize(1).contains(lancamento);
    }

    @Test
    public void deveAtualizarOStatusDeUmLancamento() {
        Lancamento lancamento = LancamentoRepositoryTest.criaLancamento();
        lancamento.setId(1L);
        lancamento.setStatus(StatusLancamento.PENDENTE);

        StatusLancamento novoStatus = StatusLancamento.EFETIVADO;

        doReturn(lancamento).when(service).atualizar(lancamento);

        service.atualizarStatus(lancamento, novoStatus);

        Assertions.assertThat(lancamento.getStatus()).isEqualTo(novoStatus);
        verify(service).atualizar(lancamento);
    }

    @Test
    public void deveObterUmLancamentoPorId() {
        Long id = 1L;
        Lancamento lancamento = LancamentoRepositoryTest.criaLancamento();
        lancamento.setId(id);

        when(repository.findById(id)).thenReturn(Optional.of(lancamento));

        Optional<Lancamento> resultado = service.obterPorId(id);

        Assertions.assertThat(resultado.isPresent()).isTrue();
    }

    @Test
    public void deveRetornarVazioQuandoUmLancamentoNaoExiste() {
        Long id = 1L;
        Lancamento lancamento = LancamentoRepositoryTest.criaLancamento();
        lancamento.setId(id);

        when(repository.findById(id)).thenReturn(Optional.empty());

        Optional<Lancamento> resultado = service.obterPorId(id);

        Assertions.assertThat(resultado.isPresent()).isFalse();
    }

    @Test
    public void deveLancarErrosAoValidarUmLancamento() {
        Lancamento lancamento = new Lancamento();

        Throwable erro = Assertions.catchThrowable(() -> service.validar(lancamento));
        Assertions.assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um descrição válida");

        lancamento.setDescricao("Teste");
        erro = Assertions.catchThrowable(() -> service.validar(lancamento));
        Assertions.assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um mês válido");

        lancamento.setMes(0);
        erro = Assertions.catchThrowable(() -> service.validar(lancamento));
        Assertions.assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um mês válido");

        lancamento.setMes(13);
        erro = Assertions.catchThrowable(() -> service.validar(lancamento));
        Assertions.assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um mês válido");

        lancamento.setMes(1);
        erro = Assertions.catchThrowable(() -> service.validar(lancamento));
        Assertions.assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um ano válido");

        lancamento.setAno(202);
        erro = Assertions.catchThrowable(() -> service.validar(lancamento));
        Assertions.assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um ano válido");

        lancamento.setAno(2019);
        erro = Assertions.catchThrowable(() -> service.validar(lancamento));
        Assertions.assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um usuário");

        lancamento.setUsuario(new Usuario());
        erro = Assertions.catchThrowable(() -> service.validar(lancamento));
        Assertions.assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um usuário");

        lancamento.setUsuario(Usuario.builder().id(1L).nome("teste").email("teste@email.com").senha("1223").build());
        erro = Assertions.catchThrowable(() -> service.validar(lancamento));
        Assertions.assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um valor válido");

        lancamento.setValor(BigDecimal.ZERO.subtract(BigDecimal.ONE));
        erro = Assertions.catchThrowable(() -> service.validar(lancamento));
        Assertions.assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um valor válido");

        lancamento.setValor(BigDecimal.TEN);
        erro = Assertions.catchThrowable(() -> service.validar(lancamento));
        Assertions.assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um tipo de lançamento");
    }
}
