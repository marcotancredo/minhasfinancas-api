package com.marcotancredo.minhasfinancas.model.repository;

import com.marcotancredo.minhasfinancas.model.entity.Lancamento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LancamentoRepository extends JpaRepository<Lancamento, Long> {
}
