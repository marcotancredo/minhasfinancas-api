package com.marcotancredo.minhasfinancas.exceptions;

public class ErroAutenticacao extends RuntimeException{
    public ErroAutenticacao(String msg){
        super(msg);
    }
}
