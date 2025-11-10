package com.perifaflow.bemestar.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="RITMO_EVENT")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RitmoEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, length=120)
    private String bairro;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=16)
    private Turno turno; // MANHA|TARDE|NOITE

    @Column(nullable=false) private Integer energia;  // 0..2
    @Column(nullable=false) private Integer ambiente; // 0..2
    @Column(nullable=false) private Integer condicao; // 0..2

    @Column(name="ENVIADO_EM", insertable=false, updatable=false)
    private java.time.OffsetDateTime enviadoEm;

}
