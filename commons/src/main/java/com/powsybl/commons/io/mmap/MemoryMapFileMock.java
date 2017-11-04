/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.io.mmap;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class MemoryMapFileMock implements MemoryMappedFile {

    @Override
    public boolean exists() {
        return false;
    }

    @Override
    public ByteBuffer getBuffer(int size) throws IOException {
        return ByteBuffer.allocate(size);
    }

    @Override
    public void close() throws IOException {
        // nothing to close in the mock
    }
}
