package com.helpdesk.api.repository;

import com.helpdesk.api.model.Ticket;
import com.helpdesk.api.model.Estado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByCreadoPorId(Long usuarioId);
    List<Ticket> findByEstadoNot(Estado estado);
}
