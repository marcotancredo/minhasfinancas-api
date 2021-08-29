package com.marcotancredo.minhasfinancas.service;

import com.marcotancredo.minhasfinancas.exceptions.ErroAutenticacao;
import com.marcotancredo.minhasfinancas.exceptions.RegraNegocioException;
import com.marcotancredo.minhasfinancas.model.entity.Usuario;
import com.marcotancredo.minhasfinancas.model.repository.UsuarioRepository;
import com.marcotancredo.minhasfinancas.service.impl.UsuarioServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
class UsuarioServiceTest {

    @SpyBean
    UsuarioServiceImpl service;

    @MockBean
    UsuarioRepository repository;

    @Test
    void deveSalvarUmUsuario(){
        //cenário
        doNothing().when(service).validarEmail(anyString());
        Usuario usuario = criarUsuario();

        when(repository.save(any(Usuario.class))).thenReturn(usuario);

        //ação
        Usuario usuarioSalvo = service.salvarUsuario(new Usuario());

        //verificação
        assertThat(usuarioSalvo).isNotNull();
        assertThat(usuarioSalvo.getId()).isEqualTo(1l);
        assertThat(usuarioSalvo.getNome()).isEqualTo("usuario");
        assertThat(usuarioSalvo.getEmail()).isEqualTo("usuario@email.com");
        assertThat(usuarioSalvo.getSenha()).isEqualTo("senha");
    }

    @Test
    void naoDeveSalvarUmUsuarioComEmailJaCadastrado(){
        assertThrows(RegraNegocioException.class,() -> {
            //cenário
            String email = "email@email.com";
            Usuario usuario = Usuario.builder().email(email).build();
            doThrow(RegraNegocioException.class).when(service).validarEmail(email);

            service.salvarUsuario(usuario);

            verify(repository, never()).save(usuario);
        });
    }

    @Test
    void deveAutenticarUmUsuarioComSucesso(){
        assertDoesNotThrow(() -> {
            //cenário
            String email = "usuario@email.com";
            String senha = "senha";
            Usuario usuario = criarUsuario();

            when(repository.findByEmail(email)).thenReturn(Optional.of(usuario));

            //ação
            Usuario result = service.autenticar(email, senha);

            //verificação
            assertThat(result).isNotNull();
        });
    }

    @Test
    void deveLancarErroQuandoNaoEncontrarUsuarioCadastradoComOEmailInformado(){
        //cenário
        when(repository.findByEmail(anyString())).thenReturn(Optional.empty());

        //ação
        Throwable exception = catchThrowable( () -> service.autenticar("email@email.com", "senha"));

        assertThat(exception).isInstanceOf(ErroAutenticacao.class).hasMessage("Usuário não encontrado para o email informado");
    }

    @Test
    void deveLancarErroQuandoSenhaForDiferente(){
        //cenário
        String senha = "senha";
        Usuario usuario = criarUsuario();

        when(repository.findByEmail(anyString())).thenReturn(Optional.of(usuario));

        //ação
        Throwable exception = catchThrowable( () -> service.autenticar("usuario@email.com", "123"));

        //verificação
        assertThat(exception).isInstanceOf(ErroAutenticacao.class).hasMessage("Senha inválida");
    }

    @Test
    void deveValidarEmail(){
       assertDoesNotThrow(() -> {
           //cenário
           when(repository.existsByEmail(anyString())).thenReturn(false);

           //ação
           service.validarEmail("email@email.com");
       });
    }

    @Test
    void deveLancarErroAoValidarEmailQuandoExistirEmailCadastrado(){
        //cenário
        when(repository.existsByEmail(anyString())).thenReturn(true);

        //ação
        Throwable exception = catchThrowable( () -> service.validarEmail("email@email.com"));

        //verificação
        assertThat(exception).isInstanceOf(RegraNegocioException.class).hasMessage("Já existe um usuário cadastrado com este email.");
    }

    public static Usuario criarUsuario(){
        return Usuario
                .builder()
                .id(1L)
                .nome("usuario")
                .email("usuario@email.com")
                .senha("senha")
                .build();
    }
}
