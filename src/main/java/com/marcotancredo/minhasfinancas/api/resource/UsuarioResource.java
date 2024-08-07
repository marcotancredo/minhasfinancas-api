package com.marcotancredo.minhasfinancas.api.resource;

import com.marcotancredo.minhasfinancas.api.dto.TokenDTO;
import com.marcotancredo.minhasfinancas.api.dto.UsuarioDTO;
import com.marcotancredo.minhasfinancas.model.entity.Usuario;
import com.marcotancredo.minhasfinancas.model.exception.ErroAutenticacao;
import com.marcotancredo.minhasfinancas.model.exception.RegraNegocioException;
import com.marcotancredo.minhasfinancas.model.service.JwtService;
import com.marcotancredo.minhasfinancas.model.service.LancamentoService;
import com.marcotancredo.minhasfinancas.model.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Optional;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioResource {

    private final UsuarioService service;
    private final LancamentoService lancamentoService;
    private final JwtService jwtService;

    @PostMapping
    public ResponseEntity<?> salvar(@RequestBody UsuarioDTO dto) {
        Usuario usuario = Usuario.builder().nome(dto.getNome()).email(dto.getEmail()).senha(dto.getSenha()).build();

        try {
            Usuario usuarioSalvo = service.salvarUsuario(usuario);
            return new ResponseEntity<>(usuarioSalvo, HttpStatus.CREATED);
        } catch (RegraNegocioException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/autenticar")
    public ResponseEntity<?> autenticar(@RequestBody UsuarioDTO dto) {
        try {
            Usuario usuarioAutenticado = service.autenticar(dto.getEmail(), dto.getSenha());
            String token = jwtService.gerarToken(usuarioAutenticado);
            TokenDTO tokenDTO = TokenDTO.builder()
                    .nome(usuarioAutenticado.getNome())
                    .token(token)
                    .build();

            return ResponseEntity.ok(tokenDTO);
        } catch (ErroAutenticacao ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("{id}/saldo")
    public ResponseEntity<?> obterSaldo(@PathVariable("id") Long id) {
        Optional<Usuario> usuario = service.obterPorId(id);

        if (usuario.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        BigDecimal saldo = lancamentoService.obterSaldoPorUsuarioEStatus(id);
        return ResponseEntity.ok(saldo);
    }

}
