/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.commons.io.mmap;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class MemoryMappedFileImpl implements MemoryMappedFile {

    private final Path file;
    
    private RandomAccessFile raf;

    public MemoryMappedFileImpl(Path file) throws IOException {
        this.file = file;
    }

    @Override
    public boolean exists() {
        return Files.exists(file);
    }

    @Override
    public ByteBuffer getBuffer(int size) throws IOException {
        if (raf == null) {
            raf = new RandomAccessFile(file.toFile(), "rw");
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
