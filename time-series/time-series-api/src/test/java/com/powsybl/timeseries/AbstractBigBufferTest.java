/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.timeseries;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

import java.nio.file.StandardOpenOption;
import java.nio.channels.FileChannel;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.List;
import java.util.ArrayList;

/**
 * @author Jon Harper <jon.harper at rte-france.com>
 */
abstract class AbstractBigBufferTest {

    private Path tempdir;
    protected List<FileChannel> channels;

    protected ByteBuffer testAllocator(int capacity) {
        try {
            FileChannel channel = FileChannel.open(
                tempdir.resolve(Integer.toString(channels.size())),
                StandardOpenOption.READ, StandardOpenOption.WRITE,
                StandardOpenOption.CREATE_NEW, StandardOpenOption.SPARSE,
                StandardOpenOption.DELETE_ON_CLOSE);
            channels.add(channel);
            ByteBuffer bytebuffer = channel.map(FileChannel.MapMode.PRIVATE, 0, capacity);
            return bytebuffer;
        } catch (Exception e) {
            throw new RuntimeException("error in allocator test", e);
        }
    }

    @BeforeEach
    void before() throws Exception {
        tempdir = Files.createTempDirectory("powsybltimeseriestest");
        channels = new ArrayList<>();
    }

    @AfterEach
    void after() throws Exception {
        for (FileChannel channel : channels) {
            channel.close();
        }
        Files.delete(tempdir);
    }

}
