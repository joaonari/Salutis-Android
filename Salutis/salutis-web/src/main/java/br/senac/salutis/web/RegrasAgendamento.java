package br.senac.salutis.web;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Regra de negócio central do módulo Agendamento (classe de domínio, sem dependência de Spring ou banco).
 * Os fatos que dependem do banco (horário ocupado) chegam como parâmetros, o que torna a regra testável.
 */
public final class RegrasAgendamento {
    /** Horários de atendimento (de 1 em 1 hora): 08h–11h e 14h–17h. */
    public static final List<LocalTime> HORAS = List.of(8, 9, 10, 11, 14, 15, 16, 17).stream().map(h -> LocalTime.of(h, 0)).toList();

    private RegrasAgendamento() {}

    /**
     * Valida um agendamento ou remarcação.
     * @param agora instante de referência ("agora"); injetado para permitir testes determinísticos
     * @return mensagem de erro, ou null se o agendamento é válido
     */
    public static String validar(LocalDate data, LocalTime hora, LocalDateTime agora, boolean medicoOcupado, boolean pacienteOcupado) {
        if (data == null || hora == null) return "Escolha data e horário";
        if (!HORAS.contains(hora)) return "Horário fora do atendimento";
        if (LocalDateTime.of(data, hora).isBefore(agora)) return "Escolha uma data futura";
        if (medicoOcupado) return "Horário já ocupado";
        if (pacienteOcupado) return "Você já tem consulta nesse horário";
        return null;
    }
}
