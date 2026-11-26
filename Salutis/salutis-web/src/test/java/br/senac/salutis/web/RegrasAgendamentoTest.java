package br.senac.salutis.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.Test;

/**
 * Testes unitários de RegrasAgendamento.validar (caixa-branca).
 * V(G) = 7  ->  7 caminhos independentes (CT-P1 a CT-P7) + testes de valor limite.
 * Nomenclatura: metodo_condicao_resultadoEsperado. Estrutura: Arrange, Act, Assert.
 */
public class RegrasAgendamentoTest {

    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 11, 25, 10, 0);
    private static final LocalDate AMANHA = LocalDate.of(2026, 11, 26);
    private static final LocalTime HORA_VALIDA = LocalTime.of(9, 0);

    // ---------- Caminhos independentes (cobertura de caminhos, V(G) = 7) ----------

    @Test // P1: data == null
    public void validar_dataNula_retornaEscolhaDataEHorario() {
        // Arrange
        LocalDate data = null;
        // Act
        String resultado = RegrasAgendamento.validar(data, HORA_VALIDA, AGORA, false, false);
        // Assert
        assertEquals("Escolha data e horário", resultado);
    }

    @Test // P2: data != null && hora == null
    public void validar_horaNula_retornaEscolhaDataEHorario() {
        // Arrange
        LocalTime hora = null;
        // Act
        String resultado = RegrasAgendamento.validar(AMANHA, hora, AGORA, false, false);
        // Assert
        assertEquals("Escolha data e horário", resultado);
    }

    @Test // P3: hora fora da grade de atendimento
    public void validar_horaForaDoAtendimento_retornaHorarioForaDoAtendimento() {
        // Arrange
        LocalTime almoco = LocalTime.of(12, 0);
        // Act
        String resultado = RegrasAgendamento.validar(AMANHA, almoco, AGORA, false, false);
        // Assert
        assertEquals("Horário fora do atendimento", resultado);
    }

    @Test // P4: data/hora no passado
    public void validar_dataHoraNoPassado_retornaEscolhaUmaDataFutura() {
        // Arrange
        LocalDate ontem = LocalDate.of(2026, 11, 24);
        // Act
        String resultado = RegrasAgendamento.validar(ontem, HORA_VALIDA, AGORA, false, false);
        // Assert
        assertEquals("Escolha uma data futura", resultado);
    }

    @Test // P5: médico já tem consulta confirmada no horário
    public void validar_medicoOcupado_retornaHorarioJaOcupado() {
        // Arrange
        boolean medicoOcupado = true;
        // Act
        String resultado = RegrasAgendamento.validar(AMANHA, HORA_VALIDA, AGORA, medicoOcupado, false);
        // Assert
        assertEquals("Horário já ocupado", resultado);
    }

    @Test // P6: paciente já tem consulta confirmada no horário
    public void validar_pacienteOcupado_retornaVoceJaTemConsulta() {
        // Arrange
        boolean pacienteOcupado = true;
        // Act
        String resultado = RegrasAgendamento.validar(AMANHA, HORA_VALIDA, AGORA, false, pacienteOcupado);
        // Assert
        assertEquals("Você já tem consulta nesse horário", resultado);
    }

    @Test // P7: caminho feliz
    public void validar_dadosValidosELivres_retornaNulo() {
        // Arrange (constantes da classe: AMANHA, HORA_VALIDA, AGORA)
        // Act
        String resultado = RegrasAgendamento.validar(AMANHA, HORA_VALIDA, AGORA, false, false);
        // Assert
        assertNull(resultado);
    }

    // ---------- Análise de valor limite ----------

    @Test
    public void validar_primeiroHorarioDoDia0800_retornaNulo() {
        assertNull(RegrasAgendamento.validar(AMANHA, LocalTime.of(8, 0), AGORA, false, false));
    }

    @Test
    public void validar_umMinutoAntesDaAbertura0759_retornaForaDoAtendimento() {
        // Arrange
        LocalTime hora = LocalTime.of(7, 59);
        // Act
        String resultado = RegrasAgendamento.validar(AMANHA, hora, AGORA, false, false);
        // Assert
        assertEquals("Horário fora do atendimento", resultado);
    }

    @Test
    public void validar_ultimoHorarioDaManha1100_retornaNulo() {
        assertNull(RegrasAgendamento.validar(AMANHA, LocalTime.of(11, 0), AGORA, false, false));
    }

    @Test
    public void validar_horaDoAlmoco1200_retornaForaDoAtendimento() {
        assertEquals("Horário fora do atendimento",
            RegrasAgendamento.validar(AMANHA, LocalTime.of(12, 0), AGORA, false, false));
    }

    @Test
    public void validar_primeiroHorarioDaTarde1400_retornaNulo() {
        assertNull(RegrasAgendamento.validar(AMANHA, LocalTime.of(14, 0), AGORA, false, false));
    }

    @Test
    public void validar_ultimoHorarioDoDia1700_retornaNulo() {
        assertNull(RegrasAgendamento.validar(AMANHA, LocalTime.of(17, 0), AGORA, false, false));
    }

    @Test
    public void validar_horaDeEncerramento1800_retornaForaDoAtendimento() {
        assertEquals("Horário fora do atendimento",
            RegrasAgendamento.validar(AMANHA, LocalTime.of(18, 0), AGORA, false, false));
    }

    @Test
    public void validar_horarioIgualAoInstanteAtual_retornaNulo() {
        // Arrange: "agora" coincide exatamente com o horário pedido (fronteira: isBefore é falso)
        LocalDateTime agora = LocalDateTime.of(2026, 11, 26, 9, 0);
        // Act
        String resultado = RegrasAgendamento.validar(AMANHA, HORA_VALIDA, agora, false, false);
        // Assert
        assertNull(resultado);
    }

    @Test
    public void validar_umMinutoDepoisDoHorarioPedido_retornaEscolhaUmaDataFutura() {
        // Arrange: agora passou 1 minuto do horário pedido
        LocalDateTime agora = LocalDateTime.of(2026, 11, 26, 9, 1);
        // Act
        String resultado = RegrasAgendamento.validar(AMANHA, HORA_VALIDA, agora, false, false);
        // Assert
        assertEquals("Escolha uma data futura", resultado);
    }

    // ---------- Precedência das regras ----------

    @Test
    public void validar_medicoEPacienteOcupados_priorizaMensagemDoMedico() {
        assertEquals("Horário já ocupado",
            RegrasAgendamento.validar(AMANHA, HORA_VALIDA, AGORA, true, true));
    }
}
