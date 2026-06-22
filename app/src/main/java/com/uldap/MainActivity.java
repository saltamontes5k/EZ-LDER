package com.uldap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class MainActivity extends Activity {

    private Button btnDiary;
    private Button btnParse;
    private Button btnExport;
    private Button btnImport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnDiary = findViewById(R.id.btnDiary);
        btnParse = findViewById(R.id.btnParse);
        btnExport = findViewById(R.id.btnExport);
        btnImport = findViewById(R.id.btnImport);

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

        btnImport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                importCsv();
            }
        });
    }

    private void importCsv() {
        if (Build.VERSION.SDK_INT >= 19) {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("text/*");
            startActivityForResult(intent, 1002);
        } else {
            importFromClipboard();
        }
    }

    private void importFromClipboard() {
        ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        if (cm != null && cm.getPrimaryClip() != null && cm.getPrimaryClip().getItemCount() > 0) {
            String csv = cm.getPrimaryClip().getItemAt(0).coerceToText(this).toString();
            int count = DreamStorage.importFromCsv(this, csv);
            if (count > 0) {
                Toast.makeText(this, count + " dreams imported from clipboard!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "No valid CSV data in clipboard", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Clipboard is empty", Toast.LENGTH_SHORT).show();
        }
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
        } else if (requestCode == 1002 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            try {
                BufferedReader r = new BufferedReader(
                    new InputStreamReader(getContentResolver().openInputStream(data.getData())));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) sb.append(line).append("\n");
                r.close();
                int count = DreamStorage.importFromCsv(this, sb.toString());
                if (count > 0) {
                    Toast.makeText(this, count + " dreams imported!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "No valid entries found in CSV", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(this, "Import failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
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
