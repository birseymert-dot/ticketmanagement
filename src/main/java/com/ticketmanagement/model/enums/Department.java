package com.ticketmanagement.model.enums;

import java.util.Locale;

/**
 * Kullanicilarin bagli oldugu ust birimler.
 * ADMIN kullanicisinin departmani yoktur (null).
 */
public enum Department {
    IT("IT Departmani"),
    ANDROID("Android Departmani"),
    IOS("iOS Departmani"),
    IKA("IKA Departmani");

    private final String displayName;

    Department(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Department fromSearchText(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        String normalized = value.trim()
                .toUpperCase(Locale.ROOT)
                .replace("İ", "I")
                .replace("DEPARTMANI", "")
                .replace("DEPARTMAN", "")
                .replaceAll("[^A-Z0-9]", "");

        for (Department department : values()) {
            if (department.name().equals(normalized)) {
                return department;
            }
        }
        return null;
    }
}