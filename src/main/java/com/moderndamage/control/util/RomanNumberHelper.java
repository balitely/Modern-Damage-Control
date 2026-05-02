package com.moderndamage.control.util;

public class RomanNumberHelper {
    private static final String[] GRADES = {
            "I", "II", "III", "IV", "V",
            "VI", "VII", "VIII", "IX", "X"
    };

    public static String toRomanGrade(int level) {
        if (level <= 0) return "None";
        int index = Math.min(level / 10, 9);
        return GRADES[index];
    }
}