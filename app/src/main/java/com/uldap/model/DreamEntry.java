package com.uldap.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DreamEntry {
    public String id;
    public String date;
    public String title;
    public String body;
    public String themes;

    public DreamEntry() {}

    public DreamEntry(String title, String body) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HHmm", Locale.US);
        this.id = sdf.format(new Date());
        this.date = id.substring(0, 10);
        this.title = title;
        this.body = body;
        this.themes = "";
    }

    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"id\": \"").append(escape(id)).append("\",\n");
        sb.append("  \"date\": \"").append(escape(this.date)).append("\",\n");
        sb.append("  \"title\": \"").append(escape(title)).append("\",\n");
        sb.append("  \"body\": \"").append(escape(body)).append("\",\n");
        sb.append("  \"themes\": \"").append(escape(themes)).append("\"\n");
        sb.append("}\n");
        return sb.toString();
    }

    public static DreamEntry fromJson(String json) {
        DreamEntry e = new DreamEntry();
        e.id = extract(json, "\"id\"");
        e.date = extract(json, "\"date\"");
        e.title = extract(json, "\"title\"");
        e.body = extract(json, "\"body\"");
        e.themes = extract(json, "\"themes\"");
        return e;
    }

    private static String extract(String json, String key) {
        int start = json.indexOf(key);
        if (start < 0) return "";
        start = json.indexOf("\"", start + key.length() + 2);
        if (start < 0) return "";
        start++;
        int end = json.indexOf("\"", start);
        if (end < 0) return "";
        return json.substring(start, end)
                .replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .replace("\\n", "\n");
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n");
    }
}
