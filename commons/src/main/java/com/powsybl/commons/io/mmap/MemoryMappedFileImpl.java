/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.io.mmap;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Objects;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class MemoryMappedFileImpl implements MemoryMappedFile {

    private final File file;

    private RandomAccessFile raf;

    public MemoryMappedFileImpl(File file) {
        this.file = Objects.requireNonNull(file);
    }

    @Override
    public boolean exists() {
        return file.exists();
    }

    @Override
    public ByteBuffer getBuffer(int size) throws IOException {
        if (raf == null) {
            raf = new RandomAccessFile(file, "rw");
        }
        return raf.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, size);
    }

    @Override
    public void close() throws IOException {
        if (raf != null) {
            raf.close();
        }
    }

}
