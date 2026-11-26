package br.senac.salutis;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import org.json.*;
import java.util.*;

public class AgendarActivity extends AppCompatActivity {
    final List<Long> medIds = new ArrayList<>(); final List<String> medNomes = new ArrayList<>(); String data;
    Spinner spEsp, spMed; MaterialButton bData; ChipGroup horas; TextView semHoras;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout l = UI.page(this, "Agendar Consulta");
        spEsp = new Spinner(this); spMed = new Spinner(this);
        MaterialCardView c1 = UI.card(this); LinearLayout in = UI.body(c1);
        in.addView(UI.text(this, "Especialidade", 14)); in.addView(spEsp);
        in.addView(UI.text(this, "Médico", 14)); in.addView(spMed);
        l.addView(c1);
        bData = UI.btnOutline(this, "Escolher data", v -> {
            Calendar c = Calendar.getInstance();
            DatePickerDialog d = new DatePickerDialog(this, (p, y, m, dd) -> {
                data = String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, dd);
                bData.setText("Data: " + UI.br(data)); carregarHoras();
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
            d.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            d.show();
        });
        l.addView(bData);
        MaterialCardView c2 = UI.card(this); LinearLayout in2 = UI.body(c2);
        in2.addView(UI.text(this, "Horários livres", 14, true));
        horas = new ChipGroup(this); horas.setSingleSelection(true);
        semHoras = UI.text(this, "Escolha a data para ver os horários.", 14);
        in2.addView(semHoras); in2.addView(horas);
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) c2.getLayoutParams(); lp.topMargin = UI.dp(this, 12);
        l.addView(c2);
        l.addView(UI.btn(this, "Confirmar consulta", v -> confirmar()));
        spEsp.setOnItemSelectedListener(UI.sel(this::medicos));
        spMed.setOnItemSelectedListener(UI.sel(this::carregarHoras));
        Api.req(this, "GET", "/api/especialidades", null, (erro, out) -> {
            if (erro != null) { UI.toast(this, erro); return; }
            JSONArray a = Api.arr(out); List<String> n = new ArrayList<>();
            for (int i = 0; i < a.length(); i++) n.add(a.optString(i));
            spEsp.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, n));
        });
    }
    void confirmar() {
        int pos = spMed.getSelectedItemPosition(), id = horas.getCheckedChipId();
        if (data == null || id == View.NO_ID || pos < 0 || pos >= medIds.size()) { UI.toast(this, "Escolha data e horário"); return; }
        String h = ((Chip) horas.findViewById(id)).getText().toString(), nome = medNomes.get(pos);
        try {
            JSONObject j = new JSONObject().put("medicoId", medIds.get(pos)).put("data", data).put("hora", h);
            Api.req(this, "POST", "/api/consultas", j, (erro, out) -> {
                if (erro != null) { UI.toast(this, erro); carregarHoras(); return; }
                UI.toast(this, "Consulta confirmada com " + nome + " em " + UI.br(data) + " às " + h);
                finish();
            });
        } catch (Exception x) { UI.toast(this, "Erro ao enviar"); }
    }
    void medicos() {
        if (spEsp.getSelectedItem() == null) return;
        Api.req(this, "GET", "/api/medicos?especialidade=" + Api.enc(spEsp.getSelectedItem().toString()), null, (erro, out) -> {
            if (erro != null) { UI.toast(this, erro); return; }
            medIds.clear(); medNomes.clear();
            for (JSONObject m : Api.list(out)) { medIds.add(m.optLong("id")); medNomes.add(Api.str(m, "nome")); }
            spMed.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, new ArrayList<>(medNomes)));
            carregarHoras();
        });
    }
    void carregarHoras() {
        int pos = spMed.getSelectedItemPosition();
        horas.removeAllViews();
        if (data == null || pos < 0 || pos >= medIds.size()) return;
        Api.req(this, "GET", "/api/medicos/" + medIds.get(pos) + "/horarios?data=" + data, null, (erro, out) -> {
            if (erro != null) { UI.toast(this, erro); return; }
            JSONArray a = Api.arr(out); horas.removeAllViews();
            semHoras.setText(a.length() == 0 ? "Sem horários livres nessa data." : "");
            for (int i = 0; i < a.length(); i++) {
                Chip c = new Chip(this); c.setText(Api.hora(a.optString(i))); c.setCheckable(true); c.setId(View.generateViewId());
                horas.addView(c);
            }
        });
    }
}
