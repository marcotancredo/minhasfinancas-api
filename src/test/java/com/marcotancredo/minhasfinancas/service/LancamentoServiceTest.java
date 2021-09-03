package com.marcotancredo.minhasfinancas.service;

import com.marcotancredo.minhasfinancas.exceptions.RegraNegocioException;
import com.marcotancredo.minhasfinancas.model.entity.Lancamento;
import com.marcotancredo.minhasfinancas.model.entity.Usuario;
import com.marcotancredo.minhasfinancas.model.enums.StatusLancamento;
import com.marcotancredo.minhasfinancas.model.repository.LancamentoRepository;
import com.marcotancredo.minhasfinancas.model.repository.LancamentoRepositoryTest;
import com.marcotancredo.minhasfinancas.service.impl.LancamentoServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.domain.Example;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
class LancamentoServiceTest {

    @SpyBean
    LancamentoServiceImpl lancamentoServiceImpl;

    @MockBean
    LancamentoRepository lancamentoRepository;

    @Test
    void deveSalvarUmLancamento(){
        Lancamento lancamentoAsalvar = LancamentoRepositoryTest.createLancamento();
        doNothing().when(lancamentoServiceImpl).validar(lancamentoAsalvar);

        Lancamento lancamentoSalvo = LancamentoRepositoryTest.createLancamento();
        lancamentoSalvo.setId(1L);
        lancamentoSalvo.setStatus(StatusLancamento.PENDENTE);
        when(lancamentoRepository.save(lancamentoAsalvar)).thenReturn(lancamentoSalvo);

        Lancamento lancamento = lancamentoServiceImpl.salvar(lancamentoAsalvar);

        assertThat(lancamento.getId()).isEqualTo(lancamentoSalvo.getId());
        assertThat(lancamento.getStatus()).isEqualTo(StatusLancamento.PENDENTE);
    }

    @Test
    void naoDeveSalvarUmLancamentoQuandoHouverErroDeValidacao(){
        Lancamento lancamentoAsalvar = LancamentoRepositoryTest.createLancamento();
        doThrow(RegraNegocioException.class).when(lancamentoServiceImpl).validar(lancamentoAsalvar);

        catchThrowableOfType(() -> lancamentoServiceImpl.salvar(lancamentoAsalvar), RegraNegocioException.class);

        verify(lancamentoRepository, never()).save(lancamentoAsalvar);
    }

    @Test
    void deveAtualizarUmLancamento(){
        Lancamento lancamentoSalvo = LancamentoRepositoryTest.createLancamento();
        lancamentoSalvo.setId(1L);
        lancamentoSalvo.setStatus(StatusLancamento.PENDENTE);

        doNothing().when(lancamentoServiceImpl).validar(lancamentoSalvo);

        when(lancamentoRepository.save(any(Lancamento.class))).thenReturn(lancamentoSalvo);

        lancamentoServiceImpl.atualizar(lancamentoSalvo);

        verify(lancamentoRepository, times(1)).save(lancamentoSalvo);
    }

    @Test
    void deveLancarErroAoTentarAtualizarUmLancamentoQueAindaNaoFoiSalvo(){
        Lancamento lancamentoAsalvar = LancamentoRepositoryTest.createLancamento();

        catchThrowableOfType(() -> lancamentoServiceImpl.atualizar(lancamentoAsalvar), NullPointerException.class);

        verify(lancamentoRepository, never()).save(lancamentoAsalvar);
    }

    @Test
    void deveDelatarUmLancamento(){
        Lancamento lancamento = LancamentoRepositoryTest.createLancamento();
        lancamento.setId(1L);

        lancamentoServiceImpl.deletar(lancamento);

        verify(lancamentoRepository).delete(lancamento);
    }

    @Test
    void deveLancarErroAoTentarDeletarUmLancamentoQueAindaNaoFoiSalvo(){
        Lancamento lancamento = LancamentoRepositoryTest.createLancamento();

        catchThrowableOfType(() -> lancamentoServiceImpl.deletar(lancamento), NullPointerException.class);

        verify(lancamentoRepository, never()).delete(lancamento);
    }

    @Test
    void deveFiltrarLancamentos(){
        Lancamento lancamento = LancamentoRepositoryTest.createLancamento();
        lancamento.setId(1L);

        List<Lancamento> lista = Collections.singletonList(lancamento);
        when(lancamentoRepository.findAll(any(Example.class))).thenReturn(lista);

        List<Lancamento> resultado = lancamentoServiceImpl.buscar(lancamento);

        assertThat(resultado).isNotEmpty().hasSize(1).contains(lancamento);
    }

    @Test
    void deveAtualizarOStatusDeUmLancamento(){
        Lancamento lancamento = LancamentoRepositoryTest.createLancamento();
        lancamento.setId(1L);
        lancamento.setStatus(StatusLancamento.PENDENTE);

        StatusLancamento novoStatus = StatusLancamento.EFETIVADO;
        doReturn(lancamento).when(lancamentoServiceImpl).atualizar(lancamento);

        lancamentoServiceImpl.atualizarStatus(lancamento, novoStatus);

        assertThat(lancamento.getStatus()).isEqualTo(novoStatus);
        verify(lancamentoServiceImpl).atualizar(lancamento);
    }

    @Test
    void deveObterUmLancamentoPorID(){
        Long id = 1L;

        Lancamento lancamento = LancamentoRepositoryTest.createLancamento();
        lancamento.setId(id);

        when(lancamentoRepository.findById(id)).thenReturn(Optional.of(lancamento));

        Optional<Lancamento> resultado = lancamentoServiceImpl.obterPorId(id);

        assertThat(resultado).isPresent();
    }

    @Test
    void deveRetornarVazioQuandoOLancamentoNaoExiste(){
        Long id = 1L;

        Lancamento lancamento = LancamentoRepositoryTest.createLancamento();
        lancamento.setId(id);

        when(lancamentoRepository.findById(id)).thenReturn(Optional.empty());

        Optional<Lancamento> resultado = lancamentoServiceImpl.obterPorId(id);

        assertThat(resultado).isEmpty();
    }

    @Test
    void deveLancarErrosAoValidarUmLancamento(){
        Lancamento lancamento = new Lancamento();

        Throwable erro = catchThrowable(() -> lancamentoServiceImpl.validar(lancamento));
        assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe uma Descrição válida.");

        lancamento.setDescricao("");

        erro = catchThrowable(() -> lancamentoServiceImpl.validar(lancamento));
        assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe uma Descrição válida.");

        lancamento.setDescricao("Teste");

        erro = catchThrowable(() -> lancamentoServiceImpl.validar(lancamento));
        assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um Mês válido.");

        lancamento.setMes(0);

        erro = catchThrowable(() -> lancamentoServiceImpl.validar(lancamento));
        assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um Mês válido.");


        lancamento.setMes(13);

        erro = catchThrowable(() -> lancamentoServiceImpl.validar(lancamento));
        assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um Mês válido.");

        lancamento.setMes(10);

        erro = catchThrowable(() -> lancamentoServiceImpl.validar(lancamento));
        assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um Ano válido.");

        lancamento.setAno(221);

        erro = catchThrowable(() -> lancamentoServiceImpl.validar(lancamento));
        assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um Ano válido.");

        lancamento.setAno(2020);

        erro = catchThrowable(() -> lancamentoServiceImpl.validar(lancamento));
        assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um usuário.");

        lancamento.setUsuario(new Usuario());

        erro = catchThrowable(() -> lancamentoServiceImpl.validar(lancamento));
        assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um usuário.");

        lancamento.getUsuario().setId(1L);

        erro = catchThrowable(() -> lancamentoServiceImpl.validar(lancamento));
        assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informar um Valor válido.");

        lancamento.setValor(BigDecimal.ZERO);

        erro = catchThrowable(() -> lancamentoServiceImpl.validar(lancamento));
        assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informar um Valor válido.");

        lancamento.setValor(BigDecimal.valueOf(10));

        erro = catchThrowable(() -> lancamentoServiceImpl.validar(lancamento));
        assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um tipo de lançamento.");

    }
}
