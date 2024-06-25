package com.marcotancredo.minhasfinancas.model.service;

import com.marcotancredo.minhasfinancas.model.entity.Usuario;
import com.marcotancredo.minhasfinancas.model.exception.ErroAutenticacao;
import com.marcotancredo.minhasfinancas.model.exception.RegraNegocioException;
import com.marcotancredo.minhasfinancas.model.repository.UsuarioRepository;
import com.marcotancredo.minhasfinancas.model.service.impl.UsuarioServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.core.convert.ConversionService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class UsuarioServiceTest {

    @SpyBean
    UsuarioServiceImpl service;

    @MockBean
    UsuarioRepository repository;

    @Test
     public void deveValidarEmail() {
        Mockito.when(repository.existsByEmail(Mockito.anyString())).thenReturn(false);

        Assertions.assertDoesNotThrow(() -> {
             service.validarEmail("email@email.com");
         });
     }

    @Test
    public void deveLancarExceptionAoValidarEmailQuandoExistirEmailCadastro() {
        Mockito.when(repository.existsByEmail(Mockito.anyString())).thenReturn(true);

        Assertions.assertThrows(RegraNegocioException.class, () -> {
            service.validarEmail("email@email.com");
        });
    }

    @Test
    public void deveAutenticarUmUsuarioComSucesso() {
        String email = "email@email.com";
        String senha = "senha";

        Usuario usuario = Usuario.builder().email(email).senha(senha).id(1L).build();
        Mockito.when(repository.findByEmail(email)).thenReturn(Optional.of(usuario));

        Assertions.assertDoesNotThrow(() -> {
            service.autenticar(email, senha);
        });
    }

    @Test
    public void deveLancarErroQUandoNaoEncontrarUsuarioCadastradoComEmailInformado() {
        Mockito.when(repository.findByEmail(Mockito.anyString())).thenReturn(Optional.empty());

        Exception exception = catchException(() -> service.autenticar("email@email.com", "senha"));

        assertThat(exception).isInstanceOf(ErroAutenticacao.class).hasMessage("Usuário e/ou senha inválidos.");
    }

    @Test
    public void deveLancarErroQUandoSenhaNaoBater() {
        String email = "email@email.com";
        String senha = "senha";

        Usuario usuario = Usuario.builder().email(email).senha(senha).id(1L).build();

        Mockito.when(repository.findByEmail(Mockito.anyString())).thenReturn(Optional.of(usuario));

        Exception exception = catchException(() -> service.autenticar("email@email.com", "1234"));

        assertThat(exception).isInstanceOf(ErroAutenticacao.class).hasMessage("Usuário e/ou senha inválidos.");
    }


    @Test
    public void deveSalvarUmUsuario() {
        String email = "email@email.com";
        String senha = "senha";
        String nome = "nome";

        Mockito.doNothing().when(service).validarEmail(Mockito.anyString());
        Usuario usuario = Usuario.builder().nome(nome).email(email).senha(senha).id(1L).build();

        Mockito.when(repository.save(Mockito.any(Usuario.class))).thenReturn(usuario);

        Usuario usuarioSalvo = service.salvarUsuario(new Usuario());

        assertThat(usuarioSalvo).isNotNull();
        assertThat(usuarioSalvo.getId()).isEqualTo(1L);
        assertThat(usuarioSalvo.getNome()).isEqualTo(nome);
        assertThat(usuarioSalvo.getEmail()).isEqualTo(email);
        assertThat(usuarioSalvo.getSenha()).isEqualTo(senha);
    }

    @Test
    public void naoDeveSalvarUmUsuarioComEmailJaCadastrado() {
        String email = "email@email.com";
        Usuario usuario = Usuario.builder().email(email).build();

        Mockito.doThrow(RegraNegocioException.class).when(service).validarEmail(email);

        Assertions.assertThrows(RegraNegocioException.class, () -> {
            service.salvarUsuario(usuario);
        });

        Mockito.verify(repository, Mockito.never()).save(usuario);
    }
}
