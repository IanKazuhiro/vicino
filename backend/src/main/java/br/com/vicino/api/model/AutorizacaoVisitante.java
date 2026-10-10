package br.com.vicino.api.model;

import java.time.Instant;
import java.time.LocalDateTime;

import br.com.vicino.api.enums.StatusAutorizacaoEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity 
@Table(name = "autorizacao_visitante")
@NoArgsConstructor 
@AllArgsConstructor 
@Setter 
@Getter 
public class AutorizacaoVisitante {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "criado_por", nullable = false)
    private Usuario criadoPor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "unidade_id", nullable = false)
    private Unidade unidade;

    @Column(name = "nome", length = 150)
    private String nome;

    @Column(name = "documento", length = 20)
    private String documento;

    @Column(name = "telefone", length = 20)
    private String telefone;

    @Column(name = "placa", length = 10)
    private String placa;

    @Column(name = "inicio", nullable = false)
    private LocalDateTime inicio;

    @Column(name = "fim", nullable = false)
    private LocalDateTime fim;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusAutorizacaoEnum status;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @Column(name = "anonimizado_em")
    private Instant anonimizadoEm;

    @PrePersist
    void aoCriar() {
        this.criadoEm = Instant.now();
    }
}