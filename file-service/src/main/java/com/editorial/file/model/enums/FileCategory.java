package com.editorial.file.model.enums;

public enum FileCategory {
    AVATAR("avatars"),
    COVER("covers"),
    ARTICLE_IMAGE("articles");

    private final String prefix;

    FileCategory(String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix() {
        return prefix;
    }
}