package com.marcotancredo.minhasfinancas.model.exception;

public class ErroAutenticacao extends RuntimeException{

    public ErroAutenticacao(String message) {
        super(message);
    }
}
