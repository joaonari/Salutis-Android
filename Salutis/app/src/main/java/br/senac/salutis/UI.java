package br.senac.salutis;
import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.InputType;
import android.view.View;
import android.widget.*;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import org.json.JSONObject;
import java.util.*;

/** Fábrica de componentes Material usada por todas as telas. */
class UI {
    static final int BLUE = Color.parseColor("#0B5394"), GREEN = Color.parseColor("#2E7D32"), GRAY = Color.parseColor("#757575");
    static final int MATCH = -1, WRAP = -2;
    private static final Map<Activity, Object[]> BUSY = new WeakHashMap<>();

    static int dp(Context c, int v) { return (int) (v * c.getResources().getDisplayMetrics().density); }

    /** Tela com rolagem (formulários). Retorna o contêiner onde os componentes são adicionados. */
    static LinearLayout page(Activity a, String title) { return build(a, title, true); }
    /** Tela sem rolagem própria, para receber um RecyclerView. */
    static LinearLayout listPage(Activity a, String title) { return build(a, title, false); }

    private static LinearLayout build(Activity a, String title, boolean scroll) {
        LinearLayout outer = new LinearLayout(a); outer.setOrientation(LinearLayout.VERTICAL);
        LinearProgressIndicator bar = new LinearProgressIndicator(a);
        bar.setIndeterminate(true); bar.setVisibility(View.GONE);
        outer.addView(bar, new LinearLayout.LayoutParams(MATCH, WRAP));
        LinearLayout l = new LinearLayout(a); l.setOrientation(LinearLayout.VERTICAL);
        int p = dp(a, 16); l.setPadding(p, p, p, p);
        if (scroll) { ScrollView sv = new ScrollView(a); sv.addView(l); outer.addView(sv, new LinearLayout.LayoutParams(MATCH, 0, 1f)); }
        else outer.addView(l, new LinearLayout.LayoutParams(MATCH, 0, 1f));
        a.setContentView(outer); a.setTitle("SALUTIS");
        BUSY.put(a, new Object[]{bar, 0});
        TextView t = text(a, title, 24, true); t.setTextColor(BLUE); l.addView(t);
        return l;
    }
    /** Mostra/oculta a barra de progresso enquanto há requisições em andamento. */
    static void busy(Activity a, int delta) {
        Object[] o = BUSY.get(a); if (o == null) return;
        int n = Math.max(0, (Integer) o[1] + delta); o[1] = n;
        ((View) o[0]).setVisibility(n > 0 ? View.VISIBLE : View.GONE);
    }

    static TextView text(Context c, String s, int sp) { return text(c, s, sp, false); }
    static TextView text(Context c, String s, int sp, boolean bold) {
        TextView t = new TextView(c);
        t.setText(s); t.setTextSize(sp); t.setPadding(0, dp(c, 4), 0, dp(c, 4));
        if (bold) t.setTypeface(null, Typeface.BOLD);
        return t;
    }
    /** Campo de texto Material (contorno, dica flutuante e botão de mostrar senha). */
    static TextInputLayout field(Context c, String hint, boolean pwd) {
        TextInputLayout til = new TextInputLayout(c, null, com.google.android.material.R.attr.textInputOutlinedStyle);
        til.setHint(hint);
        TextInputEditText e = new TextInputEditText(til.getContext());
        e.setInputType(pwd ? InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD : InputType.TYPE_CLASS_TEXT);
        e.setSingleLine(true);
        til.addView(e);
        if (pwd) til.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(MATCH, WRAP); lp.bottomMargin = dp(c, 8);
        til.setLayoutParams(lp);
        return til;
    }
    static String val(TextInputLayout t) { return t.getEditText().getText().toString(); }

    static MaterialButton btn(Context c, String s, View.OnClickListener o) { return style(new MaterialButton(c), s, o); }
    static MaterialButton btnOutline(Context c, String s, View.OnClickListener o) {
        return style(new MaterialButton(c, null, com.google.android.material.R.attr.materialButtonOutlinedStyle), s, o);
    }
    private static MaterialButton style(MaterialButton b, String s, View.OnClickListener o) {
        b.setText(s); b.setOnClickListener(o);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(MATCH, WRAP); lp.topMargin = dp(b.getContext(), 8);
        b.setLayoutParams(lp);
        return b;
    }
    /** Cartão Material com um LinearLayout vertical interno (use body()). */
    static MaterialCardView card(Context c) {
        MaterialCardView m = new MaterialCardView(c);
        m.setRadius(dp(c, 12)); m.setCardElevation(dp(c, 2));
        LinearLayout in = new LinearLayout(c); in.setOrientation(LinearLayout.VERTICAL);
        int p = dp(c, 16); in.setPadding(p, p, p, p); m.addView(in);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(MATCH, WRAP); lp.bottomMargin = dp(c, 12);
        m.setLayoutParams(lp);
        return m;
    }
    static LinearLayout body(MaterialCardView m) { return (LinearLayout) m.getChildAt(0); }
    /** Selo colorido de status (Chip Material não clicável). */
    static Chip chip(Context c, String s, int cor) {
        Chip ch = new Chip(c); ch.setText(s); ch.setClickable(false); ch.setCheckable(false);
        ch.setChipBackgroundColor(ColorStateList.valueOf(cor)); ch.setTextColor(Color.WHITE);
        ch.setLayoutParams(new LinearLayout.LayoutParams(WRAP, WRAP));
        return ch;
    }
    static LinearLayout box(Context c, View... vs) {
        LinearLayout l = new LinearLayout(c); l.setOrientation(LinearLayout.VERTICAL);
        int p = dp(c, 24); l.setPadding(p, p / 2, p, 0);
        for (View v : vs) l.addView(v);
        return l;
    }
    /** Lista em RecyclerView, ou mensagem de lista vazia. */
    static void recycler(Activity a, LinearLayout l, List<JSONObject> itens, Lista.Factory f, String vazio) {
        if (itens.isEmpty()) { l.addView(text(a, vazio, 16)); return; }
        RecyclerView rv = new RecyclerView(a);
        rv.setLayoutManager(new LinearLayoutManager(a)); rv.setAdapter(new Lista(itens, f));
        l.addView(rv, new LinearLayout.LayoutParams(MATCH, 0, 1f));
    }
    static void toast(Context c, String s) { Toast.makeText(c, s, Toast.LENGTH_LONG).show(); }
    static String br(String iso) { String[] p = iso.split("-"); return p.length == 3 ? p[2] + "/" + p[1] + "/" + p[0] : iso; }
    static AdapterView.OnItemSelectedListener sel(Runnable r) {
        return new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> a, View v, int i, long id) { r.run(); }
            public void onNothingSelected(AdapterView<?> a) {}
        };
    }
    static boolean logado(Context c) { return Api.token(c) != null; }
}
