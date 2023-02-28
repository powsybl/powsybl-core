/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.timeseries;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Jon Harper <jon.harper at rte-france.com>
 */
abstract class AbstractBigBufferTest {

    @TempDir
    Path tempDir;

    protected List<FileChannel> channels;

    protected ByteBuffer testAllocator(int capacity) {
        try {
            FileChannel channel = FileChannel.open(
                tempDir.resolve(Integer.toString(channels.size())),
                StandardOpenOption.READ, StandardOpenOption.WRITE,
                StandardOpenOption.CREATE_NEW, StandardOpenOption.SPARSE,
                StandardOpenOption.DELETE_ON_CLOSE);
            channels.add(channel);
            return channel.map(FileChannel.MapMode.PRIVATE, 0, capacity);
        } catch (Exception e) {
            throw new RuntimeException("error in allocator test", e);
        }
    }

    @BeforeEach
    void before() {
        channels = new ArrayList<>();
    }

    @AfterEach
    void after() throws IOException {
        for (FileChannel channel : channels) {
            channel.close();
        }
    }

}
