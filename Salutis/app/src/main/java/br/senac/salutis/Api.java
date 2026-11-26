package br.senac.salutis;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import org.json.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;

/** Cliente HTTP da API REST do Salutis (Spring Boot). */
class Api {
    interface Cb { void done(String erro, String json); }
    static final ExecutorService EX = Executors.newCachedThreadPool();

    static SharedPreferences sp(Context c) { return c.getSharedPreferences("s", 0); }
    static String base(Context c) { return sp(c).getString("base", "http://192.168.0.10:8080"); }
    static void setBase(Context c, String url) {
        url = url.trim(); while (url.endsWith("/")) url = url.substring(0, url.length() - 1);
        if (!url.startsWith("http")) url = "http://" + url;
        sp(c).edit().putString("base", url).apply();
    }
    static String token(Context c) { return sp(c).getString("token", null); }
    static void sair(Context c) { sp(c).edit().remove("token").remove("nome").apply(); }

    static void req(Activity a, String metodo, String path, JSONObject body, Cb cb) {
        final String base = base(a), token = token(a);
        UI.busy(a, 1);
        EX.execute(() -> {
            String erro = null, out = "";
            int st = 0;
            try {
                HttpURLConnection c = (HttpURLConnection) new URL(base + path).openConnection();
                c.setRequestMethod(metodo); c.setConnectTimeout(8000); c.setReadTimeout(8000);
                c.setRequestProperty("Content-Type", "application/json");
                if (token != null) c.setRequestProperty("Authorization", "Bearer " + token);
                if (body != null) { c.setDoOutput(true); try (OutputStream o = c.getOutputStream()) { o.write(body.toString().getBytes("UTF-8")); } }
                st = c.getResponseCode();
                InputStream is = st < 400 ? c.getInputStream() : c.getErrorStream();
                if (is != null) {
                    ByteArrayOutputStream bo = new ByteArrayOutputStream(); byte[] buf = new byte[2048]; int n;
                    while ((n = is.read(buf)) > 0) bo.write(buf, 0, n);
                    out = bo.toString("UTF-8");
                }
                if (st >= 400) {
                    try { erro = new JSONObject(out).getString("erro"); } catch (Exception e) { erro = "Erro " + st; }
                }
            } catch (Exception e) { erro = "Sem conexão com o servidor (" + base + "). Verifique o IP e o Wi-Fi."; }
            final String fe = erro, fo = out; final int fs = st;
            a.runOnUiThread(() -> {
                UI.busy(a, -1);
                if (a.isFinishing()) return;
                if (fs == 401 && token != null) { // sessão expirada (ex.: servidor reiniciado)
                    sair(a); UI.toast(a, "Sessão expirada. Entre novamente.");
                    a.startActivity(new Intent(a, LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                    a.finish(); return;
                }
                cb.done(fe, fo);
            });
        });
    }
    static JSONArray arr(String s) { try { return new JSONArray(s); } catch (Exception e) { return new JSONArray(); } }
    static java.util.List<JSONObject> list(String s) {
        JSONArray a = arr(s); java.util.List<JSONObject> l = new java.util.ArrayList<>();
        for (int i = 0; i < a.length(); i++) { JSONObject o = a.optJSONObject(i); if (o != null) l.add(o); }
        return l;
    }
    static String str(JSONObject o, String k) { return o.isNull(k) ? "" : o.optString(k, ""); }
    static String hora(String h) { return h.length() >= 5 ? h.substring(0, 5) : h; }
    static String enc(String s) { try { return URLEncoder.encode(s, "UTF-8"); } catch (Exception e) { return s; } }
}
