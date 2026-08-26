package com.helpdesk.api.dto;

import com.helpdesk.api.model.Rol;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UsuarioResponse {
    private Long id;
    private String nombre;
    private String email;
    private Rol rol;
}
