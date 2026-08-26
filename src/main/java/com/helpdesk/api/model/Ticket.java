package com.helpdesk.api.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tickets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false, length = 2000)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Prioridad prioridad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Estado estado = Estado.ABIERTO;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime creadoEn;

    private LocalDateTime slaVenceEn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creado_por_id", nullable = false)
    private Usuario creadoPor;

    @PrePersist
    protected void onCreate() {
        if (creadoEn == null) {
            creadoEn = LocalDateTime.now();
        }
        if (slaVenceEn == null && prioridad != null) {
            calcularSLA();
        }
    }

    public void calcularSLA() {
        int horas = switch (prioridad) {
            case ALTA -> 4;
            case MEDIA -> 24;
            case BAJA -> 72;
        };
        this.slaVenceEn = this.creadoEn.plusHours(horas);
    }

    public boolean isVencido() {
        return !Estado.RESUELTO.equals(estado)
                && LocalDateTime.now().isAfter(slaVenceEn);
    }
}
