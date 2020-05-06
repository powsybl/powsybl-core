/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.datastore;

import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.util.Objects;

import com.powsybl.commons.PowsyblException;

/**
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 */
public interface DataStoreUtil {

    static DataStore createDataStore(Path fileName) throws NotDirectoryException {
        Objects.requireNonNull(fileName);
        DataStore ds = null;
        if (Files.isDirectory(fileName)) {
            ds = new DirectoryDataStore(fileName);
        } else if (Files.isRegularFile(fileName)) {
            String extension = FilesUtil.getExtension(fileName.getFileName().toString());
            if (extension.equals("zip")) {
                throw new PowsyblException("Unsupported compression format");
            } else if (extension.equals("gz")) {
                throw new PowsyblException("Unsupported compression format");
            } else if (extension.equals("bz2")) {
                throw new PowsyblException("Unsupported compression format");
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

}
