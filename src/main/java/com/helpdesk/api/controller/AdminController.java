package com.helpdesk.api.controller;

import com.helpdesk.api.dto.AscensoRequest;
import com.helpdesk.api.dto.UsuarioResponse;
import com.helpdesk.api.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/soporte")
    public ResponseEntity<UsuarioResponse> ascenderASoporte(@Valid @RequestBody AscensoRequest request) {
        return ResponseEntity.ok(adminService.ascenderASoporte(request.getEmail()));
    }
}
