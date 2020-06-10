/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.datastore;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.util.Objects;

/**
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 */
public final class DataStores {

    private DataStores() {
    }

    public static DataStore createDataStore(Path fileName) throws IOException {
        Objects.requireNonNull(fileName);
        DataStore ds = null;
        if (Files.isDirectory(fileName)) {
            ds = new DirectoryDataStore(fileName);
        } else {
            String extension = getExtension(fileName.getFileName().toString());
            if (extension.equals("zip")) {
                ds = new ZipFileDataStore(fileName);
            } else if (extension.equals("gz")) {
                ds = new GzipFileDataStore(fileName);
            } else if (extension.equals("bz2")) {
                ds = new Bzip2FileDataStore(fileName);
            } else {
                Path parent = fileName.toAbsolutePath().getParent();
                if (parent == null) {
                    throw new NotDirectoryException("null");
                }
                ds = new DirectoryDataStore(parent);
            }
        }
        return ds;
    }

    public static String getExtension(String filename) {
        int dotIndex = filename.lastIndexOf(".");
        return dotIndex < 0 ? "" : filename.substring(dotIndex + 1);
    }

    public static String getBasename(String filename) {
        int dotIndex = filename.lastIndexOf(".");
        return dotIndex < 0 ? filename : filename.substring(0, dotIndex);
    }
}
