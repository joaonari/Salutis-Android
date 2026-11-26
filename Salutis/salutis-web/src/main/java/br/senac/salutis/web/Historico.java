package br.senac.salutis.web;
import jakarta.persistence.*;
import java.time.*;

@Entity public class Historico { @Id @GeneratedValue public Long id; public Long pacienteId; public LocalDate data; public String medico, resumo; }
