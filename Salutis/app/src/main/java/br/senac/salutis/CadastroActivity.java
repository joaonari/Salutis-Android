package br.senac.salutis;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputLayout;
import org.json.JSONObject;

public class CadastroActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout l = UI.page(this, "Criar Conta");
        TextInputLayout nome = UI.field(this, "Nome completo", false), email = UI.field(this, "E-mail", false),
                cpf = UI.field(this, "CPF (11 dígitos)", false), s1 = UI.field(this, "Senha (8+, letra e número)", true),
                s2 = UI.field(this, "Confirmar senha", true);
        for (TextInputLayout t : new TextInputLayout[]{nome, email, cpf, s1, s2}) l.addView(t);
        l.addView(UI.btn(this, "Cadastrar", v -> {
            try {
                JSONObject j = new JSONObject().put("nome", UI.val(nome).trim()).put("email", UI.val(email).trim())
                    .put("cpf", UI.val(cpf)).put("senha", UI.val(s1)).put("senha2", UI.val(s2));
                Api.req(this, "POST", "/api/auth/cadastro", j, (erro, out) -> {
                    if (erro != null) { UI.toast(this, erro); return; } // validações feitas no servidor
                    UI.toast(this, "Conta criada! Faça login."); finish();
                });
            } catch (Exception x) { UI.toast(this, "Erro ao enviar"); }
        }));
    }
}
