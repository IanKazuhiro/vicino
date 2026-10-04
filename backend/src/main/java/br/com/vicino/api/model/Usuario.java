package br.com.vicino.api.model;

import br.com.vicino.api.enums.PerfilEnum; 
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.Instant;
   
@Entity 
@Getter
@Setter
@Table (name = "usuario")
@NoArgsConstructor 
@AllArgsConstructor
public class Usuario {
    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    @Column (name = "nome", nullable = false, length = 150)
    private String nome;

    @Column (name = "email", nullable = false, length = 150, unique = true)
    private String email;

    @Column (name = "senha_hash", nullable = false, length = 100)
    private String senhaHash;

    @Enumerated (EnumType.STRING)
    @Column (name = "perfil", nullable = false, length = 20)
    private PerfilEnum perfil;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "unidade_id", nullable = true)
    private Unidade unidade;

    @Column (name = "ativo", nullable = false)
    private Boolean ativo = true;  

    @Column (name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @PrePersist void aoCriar() {
        this.criadoEm = Instant.now();
    }
}
