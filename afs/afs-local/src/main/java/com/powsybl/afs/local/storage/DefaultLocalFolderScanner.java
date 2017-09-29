/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.local.storage;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class DefaultLocalFolderScanner implements LocalFolderScanner {

    @Override
    public LocalFolder scanFolder(Path path, LocalFolderScannerContext context) {
        try {
            if (Files.isDirectory(path) && !Files.isHidden(path)) {
                return new DefaultLocalFolder(path, context.getRootDir(), context.getFileSystemName());
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return null;
    }
}
