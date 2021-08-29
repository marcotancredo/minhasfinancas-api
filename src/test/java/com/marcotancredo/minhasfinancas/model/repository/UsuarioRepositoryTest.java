package com.marcotancredo.minhasfinancas.model.repository;

import com.marcotancredo.minhasfinancas.model.entity.Usuario;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class UsuarioRepositoryTest {

    @Autowired
    UsuarioRepository repository;

    @Autowired
    TestEntityManager entityManager;

    @Test
    void deveVerificarAExistenciaDeUmEmail(){
        //cenário
        Usuario usuario = criarUsuario();
        entityManager.persist(usuario);

        //ação/execução
        boolean result = repository.existsByEmail("usuario@email.com");

        //verificação
        Assertions.assertThat(result).isTrue();
    }

    @Test
    void deveRetornarFalsoQuandoNaoHouverUsuarioCadastradoComEmail(){
        //ação/execução
        boolean result = repository.existsByEmail("usuario2@email.com");

        //verificação
        Assertions.assertThat(result).isFalse();
    }

    @Test
    void devePersistirUmUsuarioNaBaseDeDados(){
        //cenário
        Usuario usuario = criarUsuario();

        //Ação
        Usuario usuarioSalvo = repository.save(usuario);

        //Verificação
        Assertions.assertThat(usuario.getId()).isNotNull();
    }

    @Test
    void deveBuscarUmUsuarioPorEmail(){
        //cenário
        Usuario usuario = criarUsuario();

        //Ação
        entityManager.persist(usuario);

        //Verificação
        Optional<Usuario> result = repository.findByEmail("usuario@email.com");

        Assertions.assertThat(result.isPresent()).isTrue();
    }

    @Test
    void deveRetornarVazioAoBuscarUsuarioPorEmailQuandoNaoExisteNaBase(){
        //Verificação
        Optional<Usuario> result = repository.findByEmail("usuario@email.com");

        Assertions.assertThat(result.isPresent()).isFalse();
    }

    public static Usuario criarUsuario(){
        return Usuario
            .builder()
            .nome("usuario")
            .email("usuario@email.com")
            .senha("senha")
            .build();
    }
}
