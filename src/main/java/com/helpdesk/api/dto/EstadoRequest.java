package com.helpdesk.api.dto;

import com.helpdesk.api.model.Estado;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EstadoRequest {
    @NotNull(message = "El estado es obligatorio")
    private Estado estado;
}
