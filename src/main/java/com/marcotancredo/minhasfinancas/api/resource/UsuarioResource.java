package com.marcotancredo.minhasfinancas.api.resource;

import com.marcotancredo.minhasfinancas.api.dto.UsuarioDTO;
import com.marcotancredo.minhasfinancas.exceptions.ErroAutenticacao;
import com.marcotancredo.minhasfinancas.exceptions.RegraNegocioException;
import com.marcotancredo.minhasfinancas.model.entity.Usuario;
import com.marcotancredo.minhasfinancas.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioResource {

  private UsuarioService usuarioService;

  public UsuarioResource(UsuarioService usuarioService){
    this.usuarioService = usuarioService;
  }

  @PostMapping("/autenticar")
  public ResponseEntity autenticar( @RequestBody UsuarioDTO usuarioDTO){
    try{
      Usuario usuarioAutenticado = usuarioService.autenticar(usuarioDTO.getEmail(), usuarioDTO.getSenha());
      return ResponseEntity.ok(usuarioAutenticado);
    }catch(ErroAutenticacao e){
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  @PostMapping
  public ResponseEntity salvar( @RequestBody UsuarioDTO usuarioDTO){
    Usuario usuario = Usuario.builder()
            .nome(usuarioDTO.getNome())
            .email(usuarioDTO.getEmail())
            .senha(usuarioDTO.getSenha())
            .build();

    try {
      Usuario usuarioSalvo = usuarioService.salvarUsuario(usuario);
      return new ResponseEntity(usuarioSalvo, HttpStatus.CREATED);
    }catch (RegraNegocioException e){
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }
}
