package com.marcotancredo.minhasfinancas.model.service.impl;

import com.marcotancredo.minhasfinancas.model.entity.Usuario;
import com.marcotancredo.minhasfinancas.model.exception.ErroAutenticacao;
import com.marcotancredo.minhasfinancas.model.exception.RegraNegocioException;
import com.marcotancredo.minhasfinancas.model.repository.UsuarioRepository;
import com.marcotancredo.minhasfinancas.model.service.UsuarioService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository repository;
    private final PasswordEncoder encoder;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository, PasswordEncoder encoder) {
        this.repository = usuarioRepository;
        this.encoder = encoder;
    }

    @Override
    public Usuario autenticar(String email, String senha) {
        Optional<Usuario> usuario = repository.findByEmail(email);

        if (usuario.isEmpty()) {
            throw new ErroAutenticacao("Usuário e/ou senha inválidos.");
        }

        boolean senhasIguais = encoder.matches(senha, usuario.get().getSenha());

        if (!senhasIguais) {
            throw new ErroAutenticacao("Usuário e/ou senha inválidos.");
        }

        return usuario.get();
    }

    @Override
    @Transactional
    public Usuario salvarUsuario(Usuario usuario) {
        validarEmail(usuario.getEmail());
        criptografarSenha(usuario);
        return repository.save(usuario);
    }

    @Override
    public void validarEmail(String email) {
        boolean existe = repository.existsByEmail(email);
        if (existe) {
            throw new RegraNegocioException("Já existe um usuário cadsatrado com este email.");
        }
    }

    @Override
    public Optional<Usuario> obterPorId(Long id) {
        return repository.findById(id);
    }


    private void criptografarSenha(Usuario usuario) {
        String senha = usuario.getSenha();
        String senhaCripto = encoder.encode(senha);
        usuario.setSenha(senhaCripto);
    }
}
