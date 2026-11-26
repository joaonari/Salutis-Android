package br.senac.salutis.web;
import java.util.regex.Pattern;

/**
 * Regra de negócio central do módulo Autenticação e Cadastro (classe de domínio pura).
 * O fato que depende do banco (e-mail já cadastrado) chega como parâmetro. Parâmetros de texto não podem ser nulos.
 */
public final class RegrasCadastro {
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern SENHA_FORTE = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d).{8,}$");

    private static final int NOME_MINIMO = 3;
    private static final int CPF_DIGITOS = 11;

    private RegrasCadastro() {}

    /** @return mensagem de erro da primeira regra violada, ou null se o cadastro é válido */
    public static String validar(String nome, String email, String cpf, String senha, String confirmacao, boolean emailJaCadastrado) {
        if (nome.trim().length() < NOME_MINIMO) return "Informe o nome";
        if (!EMAIL.matcher(email.trim()).matches()) return "E-mail inválido";
        if (cpf.replaceAll("\\D", "").length() != CPF_DIGITOS) return "CPF deve ter 11 dígitos";
        if (!SENHA_FORTE.matcher(senha).matches()) return "Senha fraca: mínimo 8 caracteres com letras e números";
        if (!senha.equals(confirmacao)) return "As senhas não conferem";
        if (emailJaCadastrado) return "E-mail já cadastrado";
        return null;
    }
}
