package com.uldap;

import android.content.Context;

import com.uldap.model.DreamEntry;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DreamStorage {

    private static File getDreamDir(Context ctx) {
        File dir = new File(ctx.getFilesDir(), "dreams");
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    public static void saveDream(Context ctx, DreamEntry entry) throws IOException {
        File dir = getDreamDir(ctx);
        File file = new File(dir, "dream_" + entry.id + ".txt");
        FileWriter w = new FileWriter(file);
        w.write(entry.toJson());
        w.close();
        rebuildIndex(ctx);
    }

    public static List<DreamEntry> loadAllDreams(Context ctx) {
        List<DreamEntry> list = new ArrayList<>();
        File dir = getDreamDir(ctx);
        File[] files = dir.listFiles();
        if (files == null) return list;
        for (File f : files) {
            if (!f.getName().endsWith(".txt") || f.getName().equals("index.txt")) continue;
            try {
                BufferedReader r = new BufferedReader(new FileReader(f));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) sb.append(line).append("\n");
                r.close();
                list.add(DreamEntry.fromJson(sb.toString()));
            } catch (Exception ignored) {}
        }
        return list;
    }

    public static void updateDream(Context ctx, DreamEntry entry) throws IOException {
        File dir = getDreamDir(ctx);
        File file = new File(dir, "dream_" + entry.id + ".txt");
        if (file.exists()) {
            FileWriter w = new FileWriter(file, false);
            w.write(entry.toJson());
            w.close();
            rebuildIndex(ctx);
        }
    }

    private static void rebuildIndex(Context ctx) {
        List<DreamEntry> all = loadAllDreams(ctx);
        File dir = getDreamDir(ctx);
        File idx = new File(dir, "index.txt");
        try {
            FileWriter w = new FileWriter(idx);
            for (DreamEntry e : all) {
                w.write("dream_" + e.id + ".txt\n");
            }
            w.close();
        } catch (IOException ignored) {}
    }

    public static String exportToCsv(Context ctx) {
        String csv = generateCsvText(ctx);
        if (csv == null) return null;
        File dir = getDreamDir(ctx);
        File csvFile = new File(dir, "ezlder_dreams.csv");
        try {
            FileWriter w = new FileWriter(csvFile);
            w.write(csv);
            w.close();
        } catch (IOException e) {
            return null;
        }
        return csvFile.getAbsolutePath();
    }

    public static String generateCsvText(Context ctx) {
        List<DreamEntry> all = loadAllDreams(ctx);
        StringBuilder csv = new StringBuilder();
        csv.append("Date,Title,Body,Themes\n");
        for (DreamEntry e : all) {
            csv.append(csvEscape(e.date)).append(",");
            csv.append(csvEscape(e.title)).append(",");
            csv.append(csvEscape(e.body)).append(",");
            csv.append(csvEscape(e.themes)).append("\n");
        }
        return csv.toString();
    }

    private static String csvEscape(String s) {
        if (s == null) return "\"\"";
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }
}
