/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class WorkingDirectory implements AutoCloseable {

    private final Path path;
    private final boolean debug;
    private boolean closed;

    public WorkingDirectory(Path parentDir, String prefix, boolean debug) throws IOException {
        this.path = Files.createTempDirectory(parentDir, prefix);
        this.debug = debug;
        this.closed = false;
    }

    public Path toPath() {
        return path;
    }

    public boolean isDebug() {
        return debug;
    }

    @Override
    public synchronized void close() throws IOException {
        if (!debug && !closed) {
            FileUtil.removeDir(path);
            closed = true;
        }
    }
}
