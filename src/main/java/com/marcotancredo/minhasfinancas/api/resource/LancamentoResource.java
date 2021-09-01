package com.marcotancredo.minhasfinancas.api.resource;

import com.marcotancredo.minhasfinancas.api.dto.AtualizaStatusDTO;
import com.marcotancredo.minhasfinancas.api.dto.LancamentoDTO;
import com.marcotancredo.minhasfinancas.exceptions.RegraNegocioException;
import com.marcotancredo.minhasfinancas.model.entity.Lancamento;
import com.marcotancredo.minhasfinancas.model.entity.Usuario;
import com.marcotancredo.minhasfinancas.model.enums.StatusLancamento;
import com.marcotancredo.minhasfinancas.model.enums.TipoLancamento;
import com.marcotancredo.minhasfinancas.service.LancamentoService;
import com.marcotancredo.minhasfinancas.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.xml.ws.Response;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/lancamentos")
@RequiredArgsConstructor
public class LancamentoResource {

  private final LancamentoService lancamentoService;
  private final UsuarioService usuarioService;

  @PostMapping
  public ResponseEntity salvar(@RequestBody LancamentoDTO lancamentoDTO){
    try{
      Lancamento entidade = converter(lancamentoDTO);
      entidade = lancamentoService.salvar(entidade);
      return new ResponseEntity(entidade, HttpStatus.CREATED);
    }catch(RegraNegocioException e){
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  @PutMapping("{id}")
  public ResponseEntity atualizar(@PathVariable(value = "id") Long id,
                                  @RequestBody LancamentoDTO lancamentoDTO){

      lancamentoService.obterPorId(id).map( entity -> {
        try{
          Lancamento lancamento = converter(lancamentoDTO);
          lancamento.setId(entity.getId());
          lancamentoService.atualizar(lancamento);
          return null;
        }catch(RegraNegocioException e){
          return ResponseEntity.badRequest().body(e.getMessage());
        }
      }).orElseGet( () ->
              new ResponseEntity("Lançamento não encontrado na base de Dados", HttpStatus.BAD_REQUEST));

      return ResponseEntity.ok(lancamentoService.obterPorId(id));
  }

  @PutMapping("{id}/atualiza-status")
  public ResponseEntity atualizarStatus(@PathVariable("id") Long id,
                                        @RequestBody AtualizaStatusDTO atualizaStatusDTO){
    return lancamentoService.obterPorId(id).map(entity -> {
      StatusLancamento statusSelecionado = StatusLancamento.valueOf(atualizaStatusDTO.getStatus());
      if(statusSelecionado == null){
        return ResponseEntity.badRequest().body("Não foi possívelp atualizar o status do lançamento, " +
                "envie um status válido.");
      }
      try {
        entity.setStatus(statusSelecionado);
        lancamentoService.atualizar(entity);
        return ResponseEntity.ok(entity);
      }catch(RegraNegocioException e){
        return ResponseEntity.badRequest().body(e.getMessage());
      }
    }).orElseGet( () -> new ResponseEntity("Lançamento não encontrado na base de Dados.", HttpStatus.BAD_REQUEST));

  }

  @DeleteMapping("{id}")
  public ResponseEntity deletar( @PathVariable("id") Long id){
    return lancamentoService.obterPorId(id).map( entidade -> {
      lancamentoService.deletar(entidade);
      return new ResponseEntity(HttpStatus.NO_CONTENT);
    }).orElseGet( () ->
            new ResponseEntity("Lançamento não encontrado na base de Dados.", HttpStatus.BAD_REQUEST));
  }

  @GetMapping
  public ResponseEntity buscar(@RequestParam(value = "descricao", required = false) String descricao,
                               @RequestParam(value = "mes", required = false) Integer mes,
                               @RequestParam(value = "ano", required = false) Integer ano,
                               @RequestParam("usuario") Long idUsuario){
    Lancamento lancamentoFiltro = new Lancamento();
    lancamentoFiltro.setDescricao(descricao);
    lancamentoFiltro.setAno(ano);
    lancamentoFiltro.setMes(mes);

    Optional<Usuario> usuario = usuarioService.obterPorId(idUsuario);
    if(!usuario.isPresent()){
      return ResponseEntity.badRequest().body("Não foi possível realizar a consulta." +
              "Usuário não encontrado para o Id informado");
    } else {
      lancamentoFiltro.setUsuario(usuario.get());
    }

    List<Lancamento> lancamentos = lancamentoService.buscar(lancamentoFiltro);
    return ResponseEntity.ok(lancamentos);
  }

  private Lancamento converter(LancamentoDTO lancamentoDTO){
    Lancamento lancamento = new Lancamento();
    lancamento.setId(lancamento.getId());
    lancamento.setDescricao(lancamentoDTO.getDescricao());
    lancamento.setAno(lancamentoDTO.getAno());
    lancamento.setMes(lancamentoDTO.getMes());
    lancamento.setValor(lancamentoDTO.getValor());

    Usuario usuario = usuarioService.obterPorId(lancamentoDTO.getUsuario())
            .orElseThrow( () -> new RegraNegocioException("Usuário não encontrado para o Id informado."));

    lancamento.setUsuario(usuario);

    if(lancamentoDTO.getTipo() != null){
      lancamento.setTipo(TipoLancamento.valueOf(lancamentoDTO.getTipo()));
    }

    if(lancamentoDTO.getStatus() != null){
      lancamento.setStatus(StatusLancamento.valueOf(lancamentoDTO.getStatus()));
    }


    return lancamento;
  }
}
