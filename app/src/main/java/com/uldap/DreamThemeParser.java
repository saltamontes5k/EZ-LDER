package com.uldap;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class DreamThemeParser {

    private static class ThemeDef {
        String name;
        String[] keywords;
        ThemeDef(String name, String[] keywords) {
            this.name = name;
            this.keywords = keywords;
        }
    }

    private static final ThemeDef[] THEMES = {
        new ThemeDef("Falling", new String[]{
            "fall", "falling", "fell", "trip", "slip", "stumble", "plunge", "gravity", "drop", "downward"
        }),
        new ThemeDef("Flying", new String[]{
            "fly", "flying", "flew", "soar", "soaring", "float", "floating", "levitate", "hover", "glide"
        }),
        new ThemeDef("Chased", new String[]{
            "chase", "chasing", "chased", "follow", "following", "followed", "run", "running",
            "escape", "hide", "hiding", "pursue", "pursued", "flee", "fleeing"
        }),
        new ThemeDef("Teeth", new String[]{
            "teeth", "tooth", "dental", "gum", "gums", "mouth", "jaw", "grind", "cracked tooth",
            "loose tooth", "losing teeth"
        }),
        new ThemeDef("Naked", new String[]{
            "naked", "nude", "exposed", "no clothes", "underwear", "bare", "undressed", "stripped"
        }),
        new ThemeDef("Water", new String[]{
            "water", "ocean", "sea", "river", "lake", "swim", "swimming", "drown", "drowning",
            "flood", "wave", "waves", "tide", "pool"
        }),
        new ThemeDef("Death", new String[]{
            "death", "die", "dying", "dead", "corpse", "corpses", "funeral", "kill", "killed",
            "murder", "grave", "cemetery"
        }),
        new ThemeDef("Lost", new String[]{
            "lost", "losing", "misplace", "misplaced", "can't find", "cannot find", "wandered",
            "wandering", "maze", "nowhere", "direction"
        }),
        new ThemeDef("School", new String[]{
            "school", "test", "exam", "classroom", "teacher", "homework", "study", "studying",
            "class", "professor", "grade", "student"
        }),
        new ThemeDef("Trapped", new String[]{
            "trap", "trapped", "stuck", "cage", "prison", "confined", "bound", "restraint",
            "restricted", "locked", "chains", "captive"
        }),
        new ThemeDef("Animals", new String[]{
            "animal", "dog", "cat", "bear", "snake", "spider", "wolf", "monster", "creature",
            "beast", "lion", "tiger", "shark"
        }),
        new ThemeDef("Money", new String[]{
            "money", "cash", "wallet", "bank", "pay", "expensive", "cost", "wealthy", "rich",
            "poor", "broke", "fortune", "gold"
        }),
        new ThemeDef("Food", new String[]{
            "food", "eat", "eating", "meal", "dinner", "hungry", "feast", "restaurant", "taste",
            "cook", "cooking", "delicious", "bread"
        }),
        new ThemeDef("House", new String[]{
            "house", "home", "room", "building", "door", "window", "stair", "stairs", "floor",
            "wall", "roof", "ceiling", "corridor", "hallway"
        }),
        new ThemeDef("Vehicle", new String[]{
            "car", "drive", "driving", "vehicle", "road", "crash", "accident", "steering",
            "wheel", "truck", "bus", "train", "plane", "airplane"
        }),
        new ThemeDef("Pregnancy", new String[]{
            "pregnant", "pregnancy", "birth", "baby", "babies", "child", "newborn", "labor",
            "delivery", "mother", "maternal"
        }),
    };

    public static class ThemeResult {
        public String name;
        public int count;
        public int totalPossible;
        public float percent;

        ThemeResult(String name, int count, int totalPossible) {
            this.name = name;
            this.count = count;
            this.totalPossible = totalPossible;
            this.percent = totalPossible > 0 ? (count * 100f / totalPossible) : 0;
        }
    }

    public static List<ThemeResult> analyze(String text) {
        List<ThemeResult> results = new ArrayList<>();
        if (text == null || text.trim().isEmpty()) return results;

        String lower = text.toLowerCase();
        String[] words = lower.split("\\W+");

        for (ThemeDef theme : THEMES) {
            int matches = 0;
            for (String kw : theme.keywords) {
                if (lower.contains(kw)) {
                    matches++;
                }
            }
            if (matches > 0) {
                results.add(new ThemeResult(theme.name, matches, theme.keywords.length));
            }
        }

        results.sort((a, b) -> Float.compare(b.percent, a.percent));
        return results;
    }
}
