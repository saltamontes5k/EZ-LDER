package com.uldap;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {

    private Button btnDiary;
    private Button btnParse;
    private Button btnExport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnDiary = findViewById(R.id.btnDiary);
        btnParse = findViewById(R.id.btnParse);
        btnExport = findViewById(R.id.btnExport);

        btnDiary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, DreamDiaryActivity.class));
            }
        });

        btnParse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, DreamParseActivity.class));
            }
        });

        btnExport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exportCsv();
            }
        });
    }

    private void exportCsv() {
        if (Build.VERSION.SDK_INT >= 19) {
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("text/csv");
            intent.putExtra(Intent.EXTRA_TITLE, "ezlder_dreams.csv");
            startActivityForResult(intent, 1001);
        } else {
            String path = DreamStorage.exportToCsv(this);
            if (path != null) {
                Toast.makeText(this, "CSV saved to: " + path, Toast.LENGTH_LONG).show();
            } else {
                copyToClipboard();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            String csv = DreamStorage.generateCsvText(this);
            if (csv != null && data.getData() != null) {
                try {
                    java.io.OutputStream os = getContentResolver().openOutputStream(data.getData());
                    if (os != null) {
                        os.write(csv.getBytes("UTF-8"));
                        os.close();
                        Toast.makeText(this, "CSV exported!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } catch (Exception e) {
                    Toast.makeText(this, "Export failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
            copyToClipboard();
        }
    }

    private void copyToClipboard() {
        String csv = DreamStorage.generateCsvText(this);
        if (csv == null || csv.trim().isEmpty()) {
            Toast.makeText(this, "No dreams to export.", Toast.LENGTH_SHORT).show();
            return;
        }
        ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        if (cm != null) {
            cm.setPrimaryClip(ClipData.newPlainText("EZ LDer CSV", csv));
            Toast.makeText(this, "CSV copied to clipboard!", Toast.LENGTH_SHORT).show();
        }
    }
}
