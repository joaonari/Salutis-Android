package br.senac.salutis;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;
import org.json.JSONObject;

public class HistoricoActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout l = UI.listPage(this, "Histórico Médico");
        Api.req(this, "GET", "/api/historico", null, (erro, out) -> {
            if (erro != null) { UI.toast(this, erro); return; }
            UI.recycler(this, l, Api.list(out), (c, r) -> item(r), "Nenhum registro.");
        });
    }
    private View item(JSONObject r) {
        MaterialCardView m = UI.card(this); LinearLayout in = UI.body(m);
        in.addView(UI.text(this, UI.br(Api.str(r, "data")), 14));
        in.addView(UI.text(this, Api.str(r, "medico"), 18, true));
        in.addView(UI.text(this, Api.str(r, "resumo"), 15));
        return m;
    }
}
