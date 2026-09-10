package com.todolist.repository;

import com.todolist.entity.StatusTransacao;
import com.todolist.entity.TipoTransacao;
import com.todolist.entity.Transacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransacaoRepository extends JpaRepository<Transacao, Long> {

    Optional<Transacao> findByIdAndUsuarioId(Long id, Long usuarioId);

    List<Transacao> findByUsuarioIdOrderByDataVencimentoDesc(Long usuarioId);

    List<Transacao> findByUsuarioIdAndDataVencimentoBetweenOrderByDataVencimentoAsc(
            Long usuarioId, LocalDate dataInicio, LocalDate dataFim);

    List<Transacao> findByUsuarioIdAndStatusAndDataVencimentoBetweenOrderByDataVencimentoAsc(
            Long usuarioId, StatusTransacao status, LocalDate dataInicio, LocalDate dataFim);

    List<Transacao> findByUsuarioIdAndDataVencimentoBeforeAndStatusOrderByDataVencimentoAsc(
            Long usuarioId, LocalDate data, StatusTransacao status);

    List<Transacao> findByGrupoParcelaIdAndUsuarioIdOrderByNumeroParcelaAsc(
            String grupoParcelaId, Long usuarioId);

    @Query("SELECT COALESCE(SUM(t.valor), 0) FROM Transacao t WHERE t.usuario.id = :usuarioId AND t.tipo = :tipo AND t.dataVencimento BETWEEN :inicio AND :fim")
    BigDecimal sumValorByUsuarioIdAndTipoAndDataVencimentoBetween(
            @Param("usuarioId") Long usuarioId,
            @Param("tipo") TipoTransacao tipo,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim);

    @Query("SELECT COALESCE(SUM(t.valor), 0) FROM Transacao t WHERE t.usuario.id = :usuarioId AND t.tipo = :tipo AND t.status = :status AND t.dataVencimento BETWEEN :inicio AND :fim")
    BigDecimal sumValorByUsuarioIdAndTipoAndStatusAndDataVencimentoBetween(
            @Param("usuarioId") Long usuarioId,
            @Param("tipo") TipoTransacao tipo,
            @Param("status") StatusTransacao status,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim);

    long countByUsuarioIdAndStatusAndDataVencimentoBefore(
            Long usuarioId, StatusTransacao status, LocalDate data);
}