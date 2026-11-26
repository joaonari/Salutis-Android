package br.senac.salutis.web;
import jakarta.persistence.*;
import java.time.*;

@Entity public class Consulta {
  @Id @GeneratedValue public Long id;
  @ManyToOne public Paciente paciente; @ManyToOne public Medico medico;
  public LocalDate data; public LocalTime hora; public String status, motivo;
}
