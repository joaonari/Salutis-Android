package br.senac.salutis;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.Gravity;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputLayout;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        if (UI.logado(this)) { startActivity(new Intent(this, DashboardActivity.class)); finish(); return; }
        LinearLayout l = UI.page(this, "SALUTIS");
        TextView sub = UI.text(this, "Seu portal de saúde", 16); sub.setTextColor(UI.GRAY); l.addView(sub);
        MaterialCardView card = UI.card(this); LinearLayout c = UI.body(card);
        card.setPadding(0, 0, 0, 0);
        TextInputLayout url = UI.field(this, "Servidor (ex.: http://192.168.0.10:8080)", false);
        url.getEditText().setText(Api.base(this));
        TextInputLayout email = UI.field(this, "E-mail", false), senha = UI.field(this, "Senha", true);
        c.addView(url); c.addView(email); c.addView(senha);
        c.addView(UI.btn(this, "Entrar", v -> {
            String e = UI.val(email).trim();
            if (!Patterns.EMAIL_ADDRESS.matcher(e).matches()) { email.setError("E-mail inválido"); return; }
            email.setError(null);
            Api.setBase(this, UI.val(url));
            try {
                JSONObject j = new JSONObject().put("email", e).put("senha", UI.val(senha));
                Api.req(this, "POST", "/api/auth/login", j, (erro, out) -> {
                    if (erro != null) { UI.toast(this, erro); return; }
                    try {
                        JSONObject r = new JSONObject(out);
                        Api.sp(this).edit().putString("token", r.getString("token")).putString("nome", r.getString("nome")).apply();
                        startActivity(new Intent(this, DashboardActivity.class)); finish();
                    } catch (Exception x) { UI.toast(this, "Resposta inválida do servidor"); }
                });
            } catch (Exception x) { UI.toast(this, "Erro ao enviar"); }
        }));
        c.addView(UI.btnOutline(this, "Criar conta", v -> {
            Api.setBase(this, UI.val(url));
            startActivity(new Intent(this, CadastroActivity.class));
        }));
        l.addView(card);
    }
}
