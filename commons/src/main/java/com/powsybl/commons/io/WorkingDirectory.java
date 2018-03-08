/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class WorkingDirectory implements AutoCloseable {

    private final Path path;

    private final boolean debug;

    public WorkingDirectory(Path parentDir, String prefix, boolean debug) throws IOException {
        if (parentDir.toUri().getHost() == null) {
            path = Files.createTempDirectory(parentDir, prefix);
        } else {
            Path p;
            long n = new SecureRandom().nextLong();
            n = n == -9223372036854775808L ? 0L : Math.abs(n);
            p = parentDir.resolve(prefix + Long.toString(n));
            path = Files.createDirectory(p);
        }
        this.debug = debug;
    }

    public Path toPath() {
        return path;
    }

    @Override
    public void close() throws IOException {
        if (!debug) {
            FileUtil.removeDir(path);
        }
    }
}
