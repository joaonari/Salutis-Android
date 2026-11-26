package br.senac.salutis;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import org.json.*;
import java.util.*;

public class ConsultasActivity extends AppCompatActivity {
    @Override protected void onResume() { super.onResume(); montar(); }

    void montar() {
        LinearLayout l = UI.listPage(this, "Minhas Consultas");
        Api.req(this, "GET", "/api/consultas", null, (erro, out) -> {
            if (erro != null) { UI.toast(this, erro); return; }
            UI.recycler(this, l, Api.list(out), (c, o) -> item(o), "Nenhuma consulta agendada.");
        });
    }
    private View item(JSONObject c) {
        JSONObject med = c.optJSONObject("medico");
        String status = Api.str(c, "status"), motivo = Api.str(c, "motivo");
        boolean ativa = status.equals("Confirmada");
        MaterialCardView m = UI.card(this); LinearLayout in = UI.body(m);
        in.addView(UI.text(this, Api.str(med, "nome"), 18, true));
        in.addView(UI.text(this, Api.str(med, "especialidade"), 14));
        in.addView(UI.text(this, UI.br(Api.str(c, "data")) + " às " + Api.hora(Api.str(c, "hora")), 16));
        in.addView(UI.chip(this, status, ativa ? UI.GREEN : UI.GRAY));
        if (!motivo.isEmpty()) in.addView(UI.text(this, "Motivo: " + motivo, 14));
        if (ativa) {
            in.addView(UI.btnOutline(this, "Remarcar", v -> remarcar(c.optLong("id"), med.optLong("id"))));
            in.addView(UI.btnOutline(this, "Cancelar", v -> cancelar(c.optLong("id"))));
        }
        return m;
    }
    void cancelar(long id) {
        TextInputLayout mot = UI.field(this, "Motivo do cancelamento", false);
        new MaterialAlertDialogBuilder(this).setTitle("Cancelar consulta").setView(UI.box(this, mot))
            .setPositiveButton("Confirmar", (d, w) -> {
                try {
                    Api.req(this, "PUT", "/api/consultas/" + id + "/cancelar", new JSONObject().put("motivo", UI.val(mot).trim()),
                        (erro, out) -> { if (erro != null) UI.toast(this, erro); montar(); });
                } catch (Exception x) { UI.toast(this, "Erro ao enviar"); }
            }).setNegativeButton("Voltar", null).show();
    }
    void remarcar(long id, long medicoId) {
        Calendar cal = Calendar.getInstance();
        DatePickerDialog dp = new DatePickerDialog(this, (p, y, m, d) -> {
            String data = String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d);
            Api.req(this, "GET", "/api/medicos/" + medicoId + "/horarios?data=" + data, null, (erro, out) -> {
                if (erro != null) { UI.toast(this, erro); return; }
                JSONArray a = Api.arr(out); List<String> livres = new ArrayList<>();
                for (int i = 0; i < a.length(); i++) livres.add(Api.hora(a.optString(i)));
                if (livres.isEmpty()) { UI.toast(this, "Sem horários nessa data"); return; }
                Spinner sp = new Spinner(this);
                sp.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, livres));
                TextInputLayout mot = UI.field(this, "Motivo da remarcação", false);
                new MaterialAlertDialogBuilder(this).setTitle("Novo horário – " + UI.br(data)).setView(UI.box(this, sp, mot))
                    .setPositiveButton("Remarcar", (dd, w) -> {
                        try {
                            JSONObject j = new JSONObject().put("data", data).put("hora", sp.getSelectedItem().toString()).put("motivo", UI.val(mot).trim());
                            Api.req(this, "PUT", "/api/consultas/" + id + "/remarcar", j, (e2, o2) -> { if (e2 != null) UI.toast(this, e2); montar(); });
                        } catch (Exception x) { UI.toast(this, "Erro ao enviar"); }
                    }).setNegativeButton("Voltar", null).show();
            });
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        dp.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        dp.show();
    }
}
