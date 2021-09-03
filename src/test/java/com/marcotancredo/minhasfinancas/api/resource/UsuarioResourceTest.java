package com.marcotancredo.minhasfinancas.api.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marcotancredo.minhasfinancas.api.dto.UsuarioDTO;
import com.marcotancredo.minhasfinancas.exceptions.ErroAutenticacao;
import com.marcotancredo.minhasfinancas.exceptions.RegraNegocioException;
import com.marcotancredo.minhasfinancas.model.entity.Usuario;
import com.marcotancredo.minhasfinancas.service.LancamentoService;
import com.marcotancredo.minhasfinancas.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest( controllers = UsuarioResource.class)
@AutoConfigureMockMvc
class UsuarioResourceTest {

    static final String API = "/api/usuarios";
    public static final MediaType JSON = MediaType.APPLICATION_JSON;

    @Autowired
    MockMvc mvc;

    @MockBean
    UsuarioService usuarioService;

    @MockBean
    LancamentoService lancamentoService;

    @Test
    void deveAutenticarUmUsuario() throws Exception{
        String email = "usuario@email.com";
        String senha = "123";
        UsuarioDTO dto = UsuarioDTO.builder()
                .email(email)
                .senha(senha)
                .build();

        Usuario usuario = Usuario.builder().id(1L).email(email).senha(senha).build();

        when( usuarioService.autenticar(email, senha)).thenReturn(usuario);

        String json = new ObjectMapper().writeValueAsString(dto);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(API.concat("/autenticar"))
                .accept(JSON)
                .contentType(JSON)
                .content(json);

        mvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("id").value(usuario.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("nome").value(usuario.getNome()))
                .andExpect(MockMvcResultMatchers.jsonPath("email").value(usuario.getEmail()));

    }

    @Test
    void deveRetornarBadRequestAoObterErroDeAutenticacao() throws Exception{
        String email = "usuario@email.com";
        String senha = "123";

        UsuarioDTO dto = UsuarioDTO.builder()
                .email(email)
                .senha(senha)
                .build();

        when( usuarioService.autenticar(email, senha)).thenThrow(ErroAutenticacao.class);

        String json = new ObjectMapper().writeValueAsString(dto);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(API.concat("/autenticar"))
                .accept(JSON)
                .contentType(JSON)
                .content(json);

        mvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

    }

    @Test
    void deveCriarUmUsuario() throws Exception{
        String email = "usuario@email.com";
        String senha = "123";
        UsuarioDTO dto = UsuarioDTO.builder()
                .email(email)
                .senha(senha)
                .build();

        Usuario usuario = Usuario.builder().id(1L).email(email).senha(senha).build();

        when( usuarioService.salvarUsuario(any(Usuario.class))).thenReturn(usuario);

        String json = new ObjectMapper().writeValueAsString(dto);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(API)
                .accept(JSON)
                .contentType(JSON)
                .content(json);

        mvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("id").value(usuario.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("nome").value(usuario.getNome()))
                .andExpect(MockMvcResultMatchers.jsonPath("email").value(usuario.getEmail()));

    }

    @Test
    void deveRetornarBadRequestAoTentarCriarUmUsuarioInvalido() throws Exception{
        String email = "usuario@email.com";
        String senha = "123";
        UsuarioDTO dto = UsuarioDTO.builder()
                .email(email)
                .senha(senha)
                .build();

        when( usuarioService.salvarUsuario(any(Usuario.class))).thenThrow(RegraNegocioException.class);

        String json = new ObjectMapper().writeValueAsString(dto);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(API)
                .accept(JSON)
                .contentType(JSON)
                .content(json);

        mvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

    }
}
