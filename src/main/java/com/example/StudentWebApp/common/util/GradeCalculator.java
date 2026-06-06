package com.example.StudentWebApp.common.util;

import java.math.BigDecimal;

public final class GradeCalculator {

    private GradeCalculator() {
    }

    public static String grade(BigDecimal averageScore) {
        if (averageScore == null) {
            return "F";
        }
        if (averageScore.compareTo(BigDecimal.valueOf(80)) >= 0) {
            return "A";
        }
        if (averageScore.compareTo(BigDecimal.valueOf(70)) >= 0) {
            return "B";
        }
        if (averageScore.compareTo(BigDecimal.valueOf(60)) >= 0) {
            return "C";
        }
        if (averageScore.compareTo(BigDecimal.valueOf(50)) >= 0) {
            return "D";
        }
        if (averageScore.compareTo(BigDecimal.valueOf(40)) >= 0) {
            return "E";
        }
        return "F";
    }
}
