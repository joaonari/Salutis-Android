package br.senac.salutis;
import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;

public class DashboardActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout l = UI.page(this, "Olá, " + Api.sp(this).getString("nome", ""));
        l.addView(tile("Agendar Consulta", "Escolha especialidade, médico e horário", AgendarActivity.class));
        l.addView(tile("Minhas Consultas", "Veja, remarque ou cancele", ConsultasActivity.class));
        l.addView(tile("Histórico Médico", "Seus atendimentos anteriores", HistoricoActivity.class));
        l.addView(tile("Unidade da Clínica", "Endereço, rota no mapa e telefone", UnidadeActivity.class));
        l.addView(UI.btnOutline(this, "Sair", v -> {
            Api.req(this, "POST", "/api/auth/logout", null, (e, o) -> {});
            Api.sair(this);
            startActivity(new Intent(this, LoginActivity.class)); finish();
        }));
    }
    private MaterialCardView tile(String titulo, String sub, Class<?> destino) {
        MaterialCardView m = UI.card(this); LinearLayout in = UI.body(m);
        in.addView(UI.text(this, titulo, 20, true)); in.addView(UI.text(this, sub, 14));
        m.setClickable(true); m.setFocusable(true);
        m.setOnClickListener(v -> startActivity(new Intent(this, destino)));
        return m;
    }
}
