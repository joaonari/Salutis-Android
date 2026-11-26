package br.senac.salutis.web;
import jakarta.servlet.http.HttpSession;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.*;
import java.util.*;

@Controller
class WebController {
    final Svc s;
    WebController(Svc s) { this.s = s; }

    static Long uid(HttpSession h) { return (Long) h.getAttribute("uid"); }
    static String msg(RedirectAttributes ra, String erro, String ok) {
        ra.addFlashAttribute(erro != null ? "erro" : "ok", erro != null ? erro : ok); return erro;
    }

    @GetMapping("/") String home(HttpSession h) { return uid(h) == null ? "redirect:/login" : "redirect:/dashboard"; }
    @GetMapping("/login") String login() { return "login"; }
    @PostMapping("/login") String doLogin(String email, String senha, HttpSession h, RedirectAttributes ra) {
        Optional<Paciente> p = s.login(email, senha);
        if (p.isEmpty()) { ra.addFlashAttribute("erro", "E-mail ou senha incorretos"); return "redirect:/login"; }
        h.setAttribute("uid", p.get().id); h.setAttribute("nome", p.get().nome); return "redirect:/dashboard";
    }
    @GetMapping("/cadastro") String cadastro() { return "cadastro"; }
    @PostMapping("/cadastro") String doCadastro(String nome, String email, String cpf, String senha, String senha2, RedirectAttributes ra) {
        String e = s.cadastrar(nome, email, cpf, senha, senha2);
        if (e != null) { ra.addFlashAttribute("erro", e); return "redirect:/cadastro"; }
        ra.addFlashAttribute("ok", "Conta criada! Faça login."); return "redirect:/login";
    }
    @GetMapping("/sair") String sair(HttpSession h) { h.invalidate(); return "redirect:/login"; }
    @GetMapping("/dashboard") String dash(HttpSession h) { return uid(h) == null ? "redirect:/login" : "dashboard"; }

    @GetMapping("/agendar") String agendar(@RequestParam(required = false) String esp, @RequestParam(required = false) Long medico,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data, HttpSession h, Model m) {
        if (uid(h) == null) return "redirect:/login";
        List<String> esps = s.medicos.findAll().stream().map(x -> x.especialidade).distinct().sorted().toList();
        if (esp == null) esp = esps.get(0);
        List<Medico> meds = s.medicos.findByEspecialidade(esp);
        if (medico == null || meds.stream().noneMatch(x -> x.id.equals(medico))) medico = meds.get(0).id;
        m.addAttribute("esps", esps); m.addAttribute("esp", esp); m.addAttribute("meds", meds); m.addAttribute("medico", medico);
        m.addAttribute("data", data); m.addAttribute("hoje", LocalDate.now());
        m.addAttribute("horas", data == null ? List.of() : s.livres(medico, data));
        return "agendar";
    }
    @PostMapping("/agendar") String doAgendar(Long medico, @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime hora, HttpSession h, RedirectAttributes ra) {
        if (uid(h) == null) return "redirect:/login";
        String e = msg(ra, s.agendar(uid(h), medico, data, hora), "Consulta confirmada!");
        return e == null ? "redirect:/consultas" : "redirect:/agendar";
    }
    @GetMapping("/consultas") String consultas(HttpSession h, Model m) {
        if (uid(h) == null) return "redirect:/login";
        m.addAttribute("lista", s.consultas.findByPacienteIdOrderByDataAscHoraAsc(uid(h))); m.addAttribute("horas", Svc.HORAS); return "consultas";
    }
    @PostMapping("/consultas/{id}/cancelar") String cancelar(@PathVariable Long id, String motivo, HttpSession h, RedirectAttributes ra) {
        if (uid(h) == null) return "redirect:/login";
        msg(ra, s.cancelar(uid(h), id, motivo), "Consulta cancelada."); return "redirect:/consultas";
    }
    @PostMapping("/consultas/{id}/remarcar") String remarcar(@PathVariable Long id, @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime hora, String motivo, HttpSession h, RedirectAttributes ra) {
        if (uid(h) == null) return "redirect:/login";
        msg(ra, s.remarcar(uid(h), id, data, hora, motivo), "Consulta remarcada."); return "redirect:/consultas";
    }
    @GetMapping("/historico") String historico(HttpSession h, Model m) {
        if (uid(h) == null) return "redirect:/login";
        m.addAttribute("lista", s.historicos.findByPacienteIdOrderByDataDesc(uid(h))); return "historico";
    }
}
