package com.helpdesk.api.dto;

import com.helpdesk.api.model.Estado;
import com.helpdesk.api.model.Prioridad;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TicketResponse {
    private Long id;
    private String titulo;
    private String descripcion;
    private Prioridad prioridad;
    private Estado estado;
    private LocalDateTime creadoEn;
    private LocalDateTime slaVenceEn;
    private boolean vencido;
    private String creadoPorNombre;
}
