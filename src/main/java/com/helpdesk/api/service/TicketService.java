package com.helpdesk.api.service;

import com.helpdesk.api.dto.TicketRequest;
import com.helpdesk.api.dto.TicketResponse;
import com.helpdesk.api.model.*;
import com.helpdesk.api.repository.TicketRepository;
import com.helpdesk.api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UsuarioRepository usuarioRepository;

    public TicketResponse crearTicket(TicketRequest request, Usuario usuario) {
        Ticket ticket = Ticket.builder()
                .titulo(request.getTitulo())
                .descripcion(request.getDescripcion())
                .prioridad(request.getPrioridad())
                .estado(Estado.ABIERTO)
                .creadoPor(usuario)
                .build();

        ticket.calcularSLA();
        ticketRepository.save(ticket);

        return toResponse(ticket);
    }

    public List<TicketResponse> listarMisTickets(Usuario usuario) {
        return ticketRepository.findByCreadoPorId(usuario.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public TicketResponse obtenerTicket(Long id, Usuario usuario) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));

        if (!ticket.getCreadoPor().getId().equals(usuario.getId())
                && !usuario.getRol().equals(Rol.SOPORTE)
                && !usuario.getRol().equals(Rol.ADMIN)) {
            throw new RuntimeException("No tienes acceso a este ticket");
        }

        return toResponse(ticket);
    }

    public List<TicketResponse> listarTodos() {
        return ticketRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public TicketResponse cambiarEstado(Long id, Estado estado) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));

        ticket.setEstado(estado);
        ticketRepository.save(ticket);

        return toResponse(ticket);
    }

    public List<TicketResponse> listarVencidos() {
        return ticketRepository.findAll()
                .stream()
                .filter(Ticket::isVencido)
                .map(this::toResponse)
                .toList();
    }

    private TicketResponse toResponse(Ticket ticket) {
        return TicketResponse.builder()
                .id(ticket.getId())
                .titulo(ticket.getTitulo())
                .descripcion(ticket.getDescripcion())
                .prioridad(ticket.getPrioridad())
                .estado(ticket.getEstado())
                .creadoEn(ticket.getCreadoEn())
                .slaVenceEn(ticket.getSlaVenceEn())
                .vencido(ticket.isVencido())
                .creadoPorNombre(ticket.getCreadoPor().getNombre())
                .build();
    }
}
