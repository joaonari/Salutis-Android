package br.senac.salutis;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONObject;
import java.util.List;

/** Adapter genérico: cada item JSON vira um cartão criado pela Factory. */
class Lista extends RecyclerView.Adapter<Lista.VH> {
    interface Factory { View view(Context c, JSONObject item); }
    private final List<JSONObject> itens; private final Factory factory;
    Lista(List<JSONObject> itens, Factory f) { this.itens = itens; this.factory = f; }

    @Override public VH onCreateViewHolder(ViewGroup p, int t) {
        FrameLayout fl = new FrameLayout(p.getContext());
        fl.setLayoutParams(new RecyclerView.LayoutParams(-1, -2));
        return new VH(fl);
    }
    @Override public void onBindViewHolder(VH h, int i) {
        FrameLayout fl = (FrameLayout) h.itemView;
        fl.removeAllViews();
        fl.addView(factory.view(fl.getContext(), itens.get(i)), new FrameLayout.LayoutParams(-1, -2));
    }
    @Override public int getItemCount() { return itens.size(); }
    static class VH extends RecyclerView.ViewHolder { VH(View v) { super(v); } }
}
