package com.uldap;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.uldap.model.DreamEntry;

import java.util.List;

public class DreamParseActivity extends Activity {

    private Button btnParse;
    private ListView listDreams;
    private ScrollView scrollResults;
    private LinearLayout layoutResults;

    private List<DreamEntry> dreams;
    private int selectedIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parse);

        btnParse = findViewById(R.id.btnParse);
        listDreams = findViewById(R.id.listDreams);
        scrollResults = findViewById(R.id.scrollResults);
        layoutResults = findViewById(R.id.layoutResults);

        dreams = DreamStorage.loadAllDreams(this);

        if (dreams.isEmpty()) {
            listDreams.setAdapter(new ArrayAdapter<String>(
                this, android.R.layout.simple_list_item_1,
                new String[]{"No dreams saved yet."}
            ));
            btnParse.setEnabled(false);
            return;
        }

        String[] items = new String[dreams.size()];
        for (int i = 0; i < dreams.size(); i++) {
            DreamEntry e = dreams.get(i);
            String preview = e.body.length() > 50 ? e.body.substring(0, 50) + "..." : e.body;
            items[i] = e.date + "  |  " + e.title + "\n" + preview;
        }

        listDreams.setAdapter(new ArrayAdapter<String>(
            this, android.R.layout.simple_list_item_1, items
        ));

        listDreams.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedIndex = position;
                view.setSelected(true);
            }
        });

        btnParse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedIndex < 0 || selectedIndex >= dreams.size()) {
                    Toast.makeText(DreamParseActivity.this, "Select a dream first", Toast.LENGTH_SHORT).show();
                    return;
                }
                analyzeDream(selectedIndex);
            }
        });
    }

    private void analyzeDream(int index) {
        DreamEntry entry = dreams.get(index);
        List<DreamThemeParser.ThemeResult> results = DreamThemeParser.analyze(
            entry.title + " " + entry.body
        );

        layoutResults.removeAllViews();
        scrollResults.setVisibility(View.VISIBLE);

        if (results.isEmpty()) {
            TextView tv = new TextView(this);
            tv.setText("No common dream themes detected.");
            tv.setTextSize(18);
            tv.setTextColor(0xFFCCCCCC);
            tv.setPadding(16, 16, 16, 16);
            layoutResults.addView(tv);
            return;
        }

        int barMax = getResources().getDisplayMetrics().widthPixels - 64;

        TextView header = new TextView(this);
        header.setText("Dream: " + entry.title + " (" + entry.date + ")");
        header.setTextSize(20);
        header.setTextColor(0xFFE94560);
        header.setPadding(16, 16, 16, 24);
        header.setTypeface(null, Typeface.BOLD);
        layoutResults.addView(header);

        for (DreamThemeParser.ThemeResult r : results) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.VERTICAL);
            row.setPadding(16, 8, 16, 8);

            TextView label = new TextView(this);
            label.setText(r.name + "  (" + (int)r.percent + "% match, " + r.count + " keywords)");
            label.setTextSize(16);
            label.setTextColor(0xFFFFFFFF);
            row.addView(label);

            LinearLayout barOuter = new LinearLayout(this);
            barOuter.setBackgroundColor(0xFF333355);
            barOuter.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 24
            ));

            View barFill = new View(this);
            int fillW = Math.max(1, (int)(r.percent / 100f * barMax));
            barFill.setLayoutParams(new LinearLayout.LayoutParams(
                fillW, 24
            ));
            barFill.setBackgroundColor(r.percent > 50 ? 0xFFE94560 : 0xFF0F3460);
            barOuter.addView(barFill);
            row.addView(barOuter);

            layoutResults.addView(row);
        }

        listDreams.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        if (scrollResults.getVisibility() == View.VISIBLE) {
            scrollResults.setVisibility(View.GONE);
            listDreams.setVisibility(View.VISIBLE);
            return;
        }
        super.onBackPressed();
    }
}
