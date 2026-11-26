package br.senac.salutis.web;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/** API REST do Salutis. Autenticação: Authorization: Bearer <token> (obtido em /api/auth/login). */
@RestController
@RequestMapping("/api")
@CrossOrigin
class ApiController {
    record CadastroReq(String nome, String email, String cpf, String senha, String senha2) {}
    record LoginReq(String email, String senha) {}
    record AgendarReq(Long medicoId, LocalDate data, LocalTime hora) {}
    record RemarcarReq(LocalDate data, LocalTime hora, String motivo) {}
    record MotivoReq(String motivo) {}
    record MedicoDto(Long id, String nome, String especialidade) {}
    record ConsultaDto(Long id, MedicoDto medico, LocalDate data, LocalTime hora, String status, String motivo) {}
    record HistoricoDto(LocalDate data, String medico, String resumo) {}

    final Svc s;
    final Map<String, Long> tokens = new ConcurrentHashMap<>();
    ApiController(Svc s) { this.s = s; }

    static ResponseEntity<?> erro(int status, String msg) { return ResponseEntity.status(status).body(Map.of("erro", msg)); }
    static ResponseEntity<?> resultado(String erro, Object ok) { return erro == null ? ResponseEntity.ok(ok) : erro(400, erro); }
    Long uid(String auth) { return auth != null && auth.startsWith("Bearer ") ? tokens.get(auth.substring(7)) : null; }
    static MedicoDto dto(Medico m) { return new MedicoDto(m.id, m.nome, m.especialidade); }
    static ConsultaDto dto(Consulta c) { return new ConsultaDto(c.id, dto(c.medico), c.data, c.hora, c.status, c.motivo); }

    // ---- autenticação ----
    @PostMapping("/auth/cadastro")
    ResponseEntity<?> cadastro(@RequestBody CadastroReq r) {
        String e = s.cadastrar(nz(r.nome()), nz(r.email()), nz(r.cpf()), nz(r.senha()), nz(r.senha2()));
        return e == null ? ResponseEntity.status(201).body(Map.of("mensagem", "Conta criada")) : erro(400, e);
    }
    @PostMapping("/auth/login")
    ResponseEntity<?> login(@RequestBody LoginReq r) {
        Optional<Paciente> p = s.login(nz(r.email()), nz(r.senha()));
        if (p.isEmpty()) return erro(401, "E-mail ou senha incorretos");
        String t = UUID.randomUUID().toString(); tokens.put(t, p.get().id);
        return ResponseEntity.ok(Map.of("token", t, "nome", p.get().nome));
    }
    @PostMapping("/auth/logout")
    ResponseEntity<?> logout(@RequestHeader(value = "Authorization", required = false) String a) {
        if (a != null && a.startsWith("Bearer ")) tokens.remove(a.substring(7));
        return ResponseEntity.noContent().build();
    }

    // ---- catálogo ----
    @GetMapping("/especialidades")
    ResponseEntity<?> especialidades(@RequestHeader(value = "Authorization", required = false) String a) {
        if (uid(a) == null) return erro(401, "Não autenticado");
        return ResponseEntity.ok(s.medicos.findAll().stream().map(m -> m.especialidade).distinct().sorted().toList());
    }
    @GetMapping("/medicos")
    ResponseEntity<?> medicos(@RequestParam(required = false) String especialidade, @RequestHeader(value = "Authorization", required = false) String a) {
        if (uid(a) == null) return erro(401, "Não autenticado");
        List<Medico> l = especialidade == null ? s.medicos.findAll() : s.medicos.findByEspecialidade(especialidade);
        return ResponseEntity.ok(l.stream().map(ApiController::dto).toList());
    }
    @GetMapping("/medicos/{id}/horarios")
    ResponseEntity<?> horarios(@PathVariable Long id, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @RequestHeader(value = "Authorization", required = false) String a) {
        if (uid(a) == null) return erro(401, "Não autenticado");
        return ResponseEntity.ok(s.livres(id, data));
    }

    // ---- consultas ----
    @GetMapping("/consultas")
    ResponseEntity<?> consultas(@RequestHeader(value = "Authorization", required = false) String a) {
        Long u = uid(a); if (u == null) return erro(401, "Não autenticado");
        return ResponseEntity.ok(s.consultas.findByPacienteIdOrderByDataAscHoraAsc(u).stream().map(ApiController::dto).toList());
    }
    @PostMapping("/consultas")
    ResponseEntity<?> agendar(@RequestBody AgendarReq r, @RequestHeader(value = "Authorization", required = false) String a) {
        Long u = uid(a); if (u == null) return erro(401, "Não autenticado");
        String e = s.agendar(u, r.medicoId(), r.data(), r.hora());
        return e == null ? ResponseEntity.status(201).body(Map.of("mensagem", "Consulta confirmada")) : erro(400, e);
    }
    @PutMapping("/consultas/{id}/remarcar")
    ResponseEntity<?> remarcar(@PathVariable Long id, @RequestBody RemarcarReq r, @RequestHeader(value = "Authorization", required = false) String a) {
        Long u = uid(a); if (u == null) return erro(401, "Não autenticado");
        return resultado(s.remarcar(u, id, r.data(), r.hora(), r.motivo()), Map.of("mensagem", "Consulta remarcada"));
    }
    @PutMapping("/consultas/{id}/cancelar")
    ResponseEntity<?> cancelar(@PathVariable Long id, @RequestBody MotivoReq r, @RequestHeader(value = "Authorization", required = false) String a) {
        Long u = uid(a); if (u == null) return erro(401, "Não autenticado");
        return resultado(s.cancelar(u, id, r.motivo()), Map.of("mensagem", "Consulta cancelada"));
    }

    // ---- histórico ----
    @GetMapping("/historico")
    ResponseEntity<?> historico(@RequestHeader(value = "Authorization", required = false) String a) {
        Long u = uid(a); if (u == null) return erro(401, "Não autenticado");
        return ResponseEntity.ok(s.historicos.findByPacienteIdOrderByDataDesc(u).stream().map(h -> new HistoricoDto(h.data, h.medico, h.resumo)).toList());
    }
    static String nz(String v) { return v == null ? "" : v; }
}
