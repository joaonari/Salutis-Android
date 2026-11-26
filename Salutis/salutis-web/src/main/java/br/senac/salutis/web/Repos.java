package br.senac.salutis.web;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.*;
import java.util.*;

interface PacienteRepo extends JpaRepository<Paciente, Long> { Optional<Paciente> findByEmail(String e); }
interface MedicoRepo extends JpaRepository<Medico, Long> { List<Medico> findByEspecialidade(String e); }
interface ConsultaRepo extends JpaRepository<Consulta, Long> {
    List<Consulta> findByPacienteIdOrderByDataAscHoraAsc(Long id);
    boolean existsByMedicoIdAndDataAndHoraAndStatus(Long m, LocalDate d, LocalTime h, String s);
    boolean existsByPacienteIdAndDataAndHoraAndStatus(Long p, LocalDate d, LocalTime h, String s);
}
interface HistoricoRepo extends JpaRepository<Historico, Long> { List<Historico> findByPacienteIdOrderByDataDesc(Long id); }
