package br.senac.salutis.web;
import jakarta.persistence.*;
import java.time.*;

@Entity public class Paciente { @Id @GeneratedValue public Long id; public String nome, cpf, senha; @Column(unique = true) public String email; }
