package br.senac.salutis.web;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.*;
import java.util.*;

/** Regras de negócio (mesmas do app Android). */
@Service
class Svc {
    static final List<LocalTime> HORAS = RegrasAgendamento.HORAS;
    final PacienteRepo pacientes; final MedicoRepo medicos; final ConsultaRepo consultas; final HistoricoRepo historicos;
    Svc(PacienteRepo p, MedicoRepo m, ConsultaRepo c, HistoricoRepo h) { pacientes = p; medicos = m; consultas = c; historicos = h; }

    @PostConstruct void seed() {
        if (medicos.count() > 0) return;
        String[][] m = {{"Clínico Geral","Dra. Marcileide Cardoso"},{"Clínico Geral","Dr. Erick Ferraz"},{"Pediatria","Dr. João Pedro"},
            {"Pediatria","Dr. Lucas Rocha"},{"Dermatologia","Dra. Fernanda Costa"},{"Cardiologia","Dra. Marcileide Cardoso"}};
        for (String[] x : m) { Medico d = new Medico(); d.especialidade = x[0]; d.nome = x[1]; medicos.save(d); }
    }
    static String hash(String s) {
        try { StringBuilder sb = new StringBuilder();
            for (byte b : MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8))) sb.append(String.format("%02x", b));
            return sb.toString(); } catch (Exception e) { throw new IllegalStateException(e); }
    }
    /** @return mensagem de erro ou null se cadastrou */
    String cadastrar(String nome, String email, String cpf, String s1, String s2) {
        String erro = RegrasCadastro.validar(nome, email, cpf, s1, s2, pacientes.findByEmail(email.trim().toLowerCase()).isPresent());
        if (erro != null) return erro;
        Paciente p = new Paciente(); p.nome = nome.trim(); p.email = email.trim().toLowerCase(); p.cpf = cpf; p.senha = hash(s1);
        pacientes.save(p);
        Historico h = new Historico(); h.pacienteId = p.id; h.data = LocalDate.of(2026, 8, 15); h.medico = "Dr. Erick Ferraz";
        h.resumo = "Sintomas de gripe e febre alta. Prescrito analgésico e repouso."; historicos.save(h);
        return null;
    }
    Optional<Paciente> login(String email, String senha) {
        return pacientes.findByEmail(email.trim().toLowerCase()).filter(p -> p.senha.equals(hash(senha)));
    }
    List<LocalTime> livres(Long medicoId, LocalDate d) {
        return HORAS.stream().filter(h -> !consultas.existsByMedicoIdAndDataAndHoraAndStatus(medicoId, d, h, "Confirmada")).toList();
    }
    /** @return erro ou null */
    String agendar(Long pid, Long medicoId, LocalDate d, LocalTime h) {
        String e = validar(pid, medicoId, d, h); if (e != null) return e;
        Consulta c = new Consulta(); c.paciente = pacientes.getReferenceById(pid); c.medico = medicos.getReferenceById(medicoId);
        c.data = d; c.hora = h; c.status = "Confirmada"; consultas.save(c); return null;
    }
    String remarcar(Long pid, Long id, LocalDate d, LocalTime h, String motivo) {
        Consulta c = consultas.findById(id).orElse(null);
        if (c == null || !c.paciente.id.equals(pid) || !"Confirmada".equals(c.status)) return "Consulta indisponível";
        if (motivo == null || motivo.isBlank()) return "Informe o motivo";
        String e = validar(pid, c.medico.id, d, h); if (e != null) return e;
        c.data = d; c.hora = h; c.motivo = motivo.trim(); consultas.save(c); return null;
    }
    String cancelar(Long pid, Long id, String motivo) {
        Consulta c = consultas.findById(id).orElse(null);
        if (c == null || !c.paciente.id.equals(pid) || !"Confirmada".equals(c.status)) return "Consulta indisponível";
        if (motivo == null || motivo.isBlank()) return "Informe o motivo";
        c.status = "Cancelada"; c.motivo = motivo.trim(); consultas.save(c); return null;
    }
    private String validar(Long pid, Long medicoId, LocalDate d, LocalTime h) {
        boolean completo = d != null && h != null;
        boolean medicoOcupado = completo && consultas.existsByMedicoIdAndDataAndHoraAndStatus(medicoId, d, h, "Confirmada");
        boolean pacienteOcupado = completo && consultas.existsByPacienteIdAndDataAndHoraAndStatus(pid, d, h, "Confirmada");
        return RegrasAgendamento.validar(d, h, LocalDateTime.now(), medicoOcupado, pacienteOcupado);
    }
}
