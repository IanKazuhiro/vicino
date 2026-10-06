package br.com.vicino.api.model;

import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "espaco")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Espaco {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false, length = 100, unique = true)
    private String nome;

    @Column(name = "descricao", length = 500)
    private String descricao;

    @Column(name = "hora_abertura", nullable = false)
    private LocalTime horaAbertura;

    @Column(name = "hora_fechamento", nullable = false)
    private LocalTime horaFechamento;

    @Column(name = "duracao_minima_minutos", nullable = false)
    private Integer duracaoMinimaMinutos;

    @Column(name = "duracao_maxima_minutos", nullable = false)
    private Integer duracaoMaximaMinutos;

    @Column(name = "antecedencia_minima_horas", nullable = false)
    private Integer antecedenciaMinimaHoras;

    @Column(name = "antecedencia_maxima_dias", nullable = false)
    private Integer antecedenciaMaximaDias;

    @Column(name = "limite_reservas_por_unidade", nullable = false)
    private Integer limiteReservasPorUnidade;

    @Column(name = "exige_aprovacao", nullable = false)
    private Boolean exigeAprovacao = false;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;
}
