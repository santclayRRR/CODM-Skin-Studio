package com.demo.codmskinstudio;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.content.SharedPreferences;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

public class MainActivity extends Activity {
    private final int BG = Color.rgb(5, 12, 18);
    private final int PANEL = Color.rgb(14, 25, 34);
    private final int YELLOW = Color.rgb(255, 218, 35);
    private final int WHITE = Color.rgb(239, 245, 250);
    private final int MUTED = Color.rgb(155, 174, 188);
    private LinearLayout root, content;
    private String category = "Senjata";
    private String selected = "AK117 — contoh skin";
    private TextView status;
    private SharedPreferences prefs;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        prefs = getSharedPreferences("local_config", MODE_PRIVATE);
        selected = prefs.getString("selected", selected);
        buildUI();
    }

    private GradientDrawable shape(int color, int stroke) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color); d.setCornerRadius(dp(16));
        if (stroke != 0) d.setStroke(dp(1), stroke);
        return d;
    }
    private int dp(float n) { return (int)(n * getResources().getDisplayMetrics().density + 0.5f); }
    private TextView text(String s, int size, int color, boolean bold) {
        TextView t = new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }
    private LinearLayout column() {
        LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); return l;
    }
    private LinearLayout row() {
        LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.HORIZONTAL);
        l.setGravity(Gravity.CENTER_VERTICAL); return l;
    }
    private void pad(View v, int a, int b, int c, int d) {
        v.setPadding(dp(a), dp(b), dp(c), dp(d));
    }
    private TextView button(String label, boolean primary) {
        TextView t = text(label, 14, primary ? Color.BLACK : WHITE, true);
        t.setGravity(Gravity.CENTER); pad(t, 14, 13, 14, 13);
        t.setBackground(shape(primary ? YELLOW : PANEL, primary ? YELLOW : 0xff304352));
        return t;
    }
    private void buildUI() {
        root = column(); root.setBackgroundColor(BG);
        ScrollView scroll = new ScrollView(this);
        content = column(); pad(content, 18, 20, 18, 22);
        scroll.addView(content);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);
        content.addView(text("CODM  /  SKIN STUDIO", 12, YELLOW, true));
        TextView title = text("Skin Manager", 28, WHITE, true);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(-1, -2); tp.topMargin = dp(5);
        content.addView(title, tp);
        TextView subtitle = text("Garena • Pengelola konfigurasi lokal", 13, MUTED, false);
        content.addView(subtitle);
        addSpace(content, 18);
        LinearLayout target = column(); pad(target, 16, 15, 16, 15); target.setBackground(shape(PANEL, 0xff304352));
        target.addView(text("TARGET GAME", 11, MUTED, true));
        addSpace(target, 5);
        target.addView(text("CODM Garena", 18, WHITE, true));
        target.addView(text("com.garena.game.codm", 12, MUTED, false));
        addSpace(content, 14); content.addView(target);
        addSpace(content, 20);
        content.addView(text("Pilih kategori", 18, WHITE, true));
        addSpace(content, 10);
        LinearLayout cats = row();
        TextView weapons = button("Senjata", category.equals("Senjata"));
        TextView operators = button("Karakter", category.equals("Karakter"));
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(0, -2, 1); cp.rightMargin = dp(6);
        cats.addView(weapons, cp);
        LinearLayout.LayoutParams cp2 = new LinearLayout.LayoutParams(0, -2, 1); cp2.leftMargin = dp(6);
        cats.addView(operators, cp2);
        content.addView(cats);
        weapons.setOnClickListener(v -> { category = "Senjata"; refreshItems(); });
        operators.setOnClickListener(v -> { category = "Karakter"; refreshItems(); });
        addSpace(content, 16);
        content.addView(text("Daftar contoh", 18, WHITE, true));
        addSpace(content, 8);
        refreshItems();
        addSpace(content, 16);
        TextView chosen = text("Pilihan saat ini: " + selected, 13, YELLOW, true);
        chosen.setTag("chosen"); content.addView(chosen);
        addSpace(content, 12);
        TextView apply = button("Simpan Pilihan", true);
        content.addView(apply);
        apply.setOnClickListener(v -> {
            prefs.edit().putString("selected", selected).apply();
            status.setText("Tersimpan di konfigurasi lokal. Tidak mengubah file CODM.");
            Toast.makeText(this, "Konfigurasi disimpan", Toast.LENGTH_SHORT).show();
            updateChosen();
        });
        addSpace(content, 10);
        TextView restore = button("Pulihkan / Hapus Pilihan", false);
        content.addView(restore);
        restore.setOnClickListener(v -> {
            prefs.edit().remove("selected").apply();
            selected = "AK117 — contoh skin";
            status.setText("Pilihan lokal dipulihkan ke default.");
            updateChosen(); refreshItems();
        });
        addSpace(content, 14);
        status = text("Mode demo: tombol hanya menyimpan pilihan lokal.", 12, MUTED, false);
        content.addView(status);
        addSpace(content, 18);
        TextView note = text("Catatan: aplikasi ini adalah prototipe UI. Tidak meng-inject ke proses game, tidak membuka skin server, dan tidak mengubah aset CODM.", 12, MUTED, false);
        pad(note, 13, 13, 13, 13); note.setBackground(shape(0xff101a22, 0xff283844)); content.addView(note);
    }
    private void refreshItems() {
        if (content == null) return;
        // Remove previously rendered item cards, which sit after the category row and before "Pilihan saat ini".
        int start = -1, end = -1;
        for (int i = 0; i < content.getChildCount(); i++) {
            View v = content.getChildAt(i);
            if (v.getTag() != null && "items_start".equals(v.getTag())) start = i;
            if (v.getTag() != null && "chosen".equals(v.getTag())) { end = i; break; }
        }
        if (start >= 0) {
            int stop = end >= 0 ? end : content.getChildCount();
            for (int i = stop - 1; i >= start; i--) content.removeViewAt(i);
        }
        // Insert cards after the "Daftar contoh" heading (the category row is followed by heading and spacer).
        int insert = -1;
        for (int i = 0; i < content.getChildCount(); i++) {
            View v = content.getChildAt(i);
            if (v instanceof TextView && "Daftar contoh".contentEquals(((TextView)v).getText())) { insert = i + 2; break; }
        }
        if (insert < 0) return;
        TextView marker = text("", 1, WHITE, false); marker.setTag("items_start");
        content.addView(marker, insert);
        String[] weapons = {"AK117 — contoh skin", "M13 — contoh skin", "Kilo 141 — contoh skin", "DL Q33 — contoh skin"};
        String[] chars = {"Operator — contoh A", "Operator — contoh B", "Operator — contoh C", "Operator — contoh D"};
        String[] list = category.equals("Senjata") ? weapons : chars;
        for (int j = 0; j < list.length; j++) {
            final String item = list[j];
            TextView card = text((selected.equals(item) ? "✓  " : "›  ") + item + "\n     Demo katalog — bukan aset game", 14, WHITE, true);
            pad(card, 14, 13, 14, 13); card.setBackground(shape(PANEL, selected.equals(item) ? YELLOW : 0xff263a48));
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2); p.topMargin = dp(7);
            content.addView(card, insert + 1 + j, p);
            card.setOnClickListener(v -> { selected = item; updateChosen(); refreshItems(); });
        }
    }
    private void updateChosen() {
        if (content == null) return;
        for (int i=0;i<content.getChildCount();i++) {
            View v=content.getChildAt(i);
            if (v.getTag()!=null && "chosen".equals(v.getTag()) && v instanceof TextView)
                ((TextView)v).setText("Pilihan saat ini: " + selected);
        }
    }
    private void addSpace(LinearLayout l, int height) {
        View v = new View(this); l.addView(v, new LinearLayout.LayoutParams(1, dp(height)));
    }
}
