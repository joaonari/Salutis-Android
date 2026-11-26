package br.senac.salutis.web;
import jakarta.persistence.*;
import java.time.*;

@Entity public class Medico { @Id @GeneratedValue public Long id; public String especialidade, nome; }
