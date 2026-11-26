package br.senac.salutis;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;

/** Unidade da clínica: endereço, horário, rota no mapa (intent geo) e ligação (discador). */
public class UnidadeActivity extends AppCompatActivity {
    // Altere para os dados reais da clínica
    static final String NOME = "Salutis – Unidade Centro", ENDERECO = "Av. Paulista, 1000, Bela Vista, São Paulo - SP",
        TELEFONE = "1130000000", HORARIO = "Segunda a sexta, 08h às 12h e 14h às 18h";

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout l = UI.page(this, "Unidade");
        MaterialCardView m = UI.card(this); LinearLayout in = UI.body(m);
        in.addView(UI.text(this, NOME, 20, true));
        in.addView(UI.text(this, ENDERECO, 16));
        in.addView(UI.text(this, "Atendimento: " + HORARIO, 14));
        in.addView(UI.text(this, "Telefone: " + TELEFONE, 14));
        l.addView(m);
        l.addView(UI.btn(this, "Como chegar", v -> {
            Uri geo = Uri.parse("geo:0,0?q=" + Uri.encode(ENDERECO));
            try { startActivity(new Intent(Intent.ACTION_VIEW, geo)); }
            catch (ActivityNotFoundException e) { // sem app de mapas: abre no navegador
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=" + Uri.encode(ENDERECO))));
            }
        }));
        l.addView(UI.btnOutline(this, "Ligar para a clínica", v ->
            startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + TELEFONE)))));
    }
}
