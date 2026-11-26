package br.senac.salutis.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/**
 * Testes unitários de RegrasCadastro.validar (caixa-branca).
 * V(G) = 7  ->  7 caminhos independentes (P1 a P7) + testes de valor limite.
 * Nomenclatura: metodo_condicao_resultadoEsperado. Estrutura: Arrange, Act, Assert.
 */
public class RegrasCadastroTest {

    private static final String NOME = "Felipe Silva";
    private static final String EMAIL = "felipe@salutis.com";
    private static final String CPF = "12345678901";
    private static final String SENHA = "Paciente123";

    // ---------- Caminhos independentes (V(G) = 7) ----------

    @Test // P1
    public void validar_nomeCurto_retornaInformeONome() {
        // Arrange
        String nome = "Jo";
        // Act
        String resultado = RegrasCadastro.validar(nome, EMAIL, CPF, SENHA, SENHA, false);
        // Assert
        assertEquals("Informe o nome", resultado);
    }

    @Test // P2
    public void validar_emailSemArroba_retornaEmailInvalido() {
        // Arrange
        String email = "felipe.salutis.com";
        // Act
        String resultado = RegrasCadastro.validar(NOME, email, CPF, SENHA, SENHA, false);
        // Assert
        assertEquals("E-mail inválido", resultado);
    }

    @Test // P3
    public void validar_cpfCom10Digitos_retornaCpfDeveTer11Digitos() {
        // Arrange
        String cpf = "1234567890";
        // Act
        String resultado = RegrasCadastro.validar(NOME, EMAIL, cpf, SENHA, SENHA, false);
        // Assert
        assertEquals("CPF deve ter 11 dígitos", resultado);
    }

    @Test // P4
    public void validar_senhaSemNumero_retornaSenhaFraca() {
        // Arrange
        String senha = "SomenteLetras";
        // Act
        String resultado = RegrasCadastro.validar(NOME, EMAIL, CPF, senha, senha, false);
        // Assert
        assertEquals("Senha fraca: mínimo 8 caracteres com letras e números", resultado);
    }

    @Test // P5
    public void validar_confirmacaoDiferente_retornaSenhasNaoConferem() {
        // Arrange
        String confirmacao = "Paciente124";
        // Act
        String resultado = RegrasCadastro.validar(NOME, EMAIL, CPF, SENHA, confirmacao, false);
        // Assert
        assertEquals("As senhas não conferem", resultado);
    }

    @Test // P6
    public void validar_emailJaCadastrado_retornaEmailJaCadastrado() {
        // Arrange
        boolean emailJaCadastrado = true;
        // Act
        String resultado = RegrasCadastro.validar(NOME, EMAIL, CPF, SENHA, SENHA, emailJaCadastrado);
        // Assert
        assertEquals("E-mail já cadastrado", resultado);
    }

    @Test // P7
    public void validar_dadosValidos_retornaNulo() {
        // Arrange (constantes da classe)
        // Act
        String resultado = RegrasCadastro.validar(NOME, EMAIL, CPF, SENHA, SENHA, false);
        // Assert
        assertNull(resultado);
    }

    // ---------- Análise de valor limite ----------

    @Test
    public void validar_nomeCom2Caracteres_retornaInformeONome() {
        assertEquals("Informe o nome", RegrasCadastro.validar("Ab", EMAIL, CPF, SENHA, SENHA, false));
    }

    @Test
    public void validar_nomeCom3Caracteres_retornaNulo() {
        assertNull(RegrasCadastro.validar("Ana", EMAIL, CPF, SENHA, SENHA, false));
    }

    @Test
    public void validar_nomeComEspacosNasPontas_considerasoOTextoAparado() {
        // Arrange: "  Al  " tem 2 caracteres úteis após trim
        String nome = "  Al  ";
        // Act
        String resultado = RegrasCadastro.validar(nome, EMAIL, CPF, SENHA, SENHA, false);
        // Assert
        assertEquals("Informe o nome", resultado);
    }

    @Test
    public void validar_cpfCom11DigitosEMascara_retornaNulo() {
        assertNull(RegrasCadastro.validar(NOME, EMAIL, "123.456.789-01", SENHA, SENHA, false));
    }

    @Test
    public void validar_cpfCom12Digitos_retornaCpfDeveTer11Digitos() {
        assertEquals("CPF deve ter 11 dígitos", RegrasCadastro.validar(NOME, EMAIL, "123456789012", SENHA, SENHA, false));
    }

    @Test
    public void validar_senhaCom7Caracteres_retornaSenhaFraca() {
        // Arrange
        String senha = "Abcde12";
        // Act
        String resultado = RegrasCadastro.validar(NOME, EMAIL, CPF, senha, senha, false);
        // Assert
        assertEquals("Senha fraca: mínimo 8 caracteres com letras e números", resultado);
    }

    @Test
    public void validar_senhaCom8Caracteres_retornaNulo() {
        assertNull(RegrasCadastro.validar(NOME, EMAIL, CPF, "Abcdef12", "Abcdef12", false));
    }

    @Test
    public void validar_senhaSoComNumeros_retornaSenhaFraca() {
        assertEquals("Senha fraca: mínimo 8 caracteres com letras e números",
            RegrasCadastro.validar(NOME, EMAIL, CPF, "12345678", "12345678", false));
    }

    @Test
    public void validar_emailSemDominioAposPonto_retornaEmailInvalido() {
        assertEquals("E-mail inválido", RegrasCadastro.validar(NOME, "felipe@salutis", CPF, SENHA, SENHA, false));
    }

    // ---------- Precedência das regras ----------

    @Test
    public void validar_nomeEEmailInvalidos_priorizaMensagemDoNome() {
        assertEquals("Informe o nome", RegrasCadastro.validar("Jo", "x", CPF, SENHA, SENHA, true));
    }
}
