package com.powsybl.commons.util;

import java.util.Objects;

public final class Filenames {

    private Filenames() {
    }

    public static String getExtension(String filename) {
        Objects.requireNonNull(filename);
        int dotIndex = filename.lastIndexOf(".");
        return dotIndex < 0 ? "" : filename.substring(dotIndex + 1);
    }

    public static String getBasename(String filename) {
        Objects.requireNonNull(filename);
        int dotIndex = filename.lastIndexOf(".");
        return dotIndex < 0 ? filename : filename.substring(0, dotIndex);
    }
}
