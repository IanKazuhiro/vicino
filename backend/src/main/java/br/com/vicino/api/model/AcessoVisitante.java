package br.com.vicino.api.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity 
@Table(name = "acesso_visitante")
@NoArgsConstructor 
@AllArgsConstructor 
@Setter
@Getter 
public class AcessoVisitante {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "autorizacao_id", nullable = false)
    private AutorizacaoVisitante autorizacao;

    @Column(name = "entrada_em", nullable = false)
    private LocalDateTime entradaEm;

    @Column(name = "saida_em")
    private LocalDateTime saidaEm;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "registrado_entrada_por", nullable = false)
    private Usuario registradoEntradaPor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registrado_saida_por")
    private Usuario registradoSaidaPor;
}