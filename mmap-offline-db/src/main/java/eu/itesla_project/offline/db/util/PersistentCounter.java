/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.offline.db.util;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class PersistentCounter implements AutoCloseable {

    private final MemoryMappedFile file;

    private final ByteBuffer buffer;

    public PersistentCounter(MemoryMappedFile file, int initialValue) throws IOException {
        boolean exists = file.exists();
        this.file = file;
        buffer = file.getBuffer(Integer.SIZE / 8);
        if (!exists) {
            buffer.putInt(0, initialValue);
        }
    }

    public int nextValue() {
        synchronized (this) {
            int oldValue = buffer.getInt(0);
            int newValue = oldValue + 1;
            buffer.putInt(0, newValue);
            return oldValue;
        }
    }

    public int getValue() {
        synchronized (this) {
            return buffer.getInt(0);
        }
    }
    
    @Override
    public void close() throws IOException {
        file.close();
    }
    
}
