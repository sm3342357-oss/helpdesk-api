package com.helpdesk.api.controller;

import com.helpdesk.api.dto.*;
import com.helpdesk.api.model.Usuario;
import com.helpdesk.api.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping
    public ResponseEntity<TicketResponse> crearTicket(
            @Valid @RequestBody TicketRequest request,
            @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ticketService.crearTicket(request, usuario));
    }

    @GetMapping("/mios")
    public ResponseEntity<List<TicketResponse>> misTickets(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(ticketService.listarMisTickets(usuario));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketResponse> obtenerTicket(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(ticketService.obtenerTicket(id, usuario));
    }

    @GetMapping
    public ResponseEntity<List<TicketResponse>> listarTodos() {
        return ResponseEntity.ok(ticketService.listarTodos());
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<TicketResponse> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody EstadoRequest request) {
        return ResponseEntity.ok(ticketService.cambiarEstado(id, request.getEstado()));
    }

    @GetMapping("/vencidos")
    public ResponseEntity<List<TicketResponse>> listarVencidos() {
        return ResponseEntity.ok(ticketService.listarVencidos());
    }
}
