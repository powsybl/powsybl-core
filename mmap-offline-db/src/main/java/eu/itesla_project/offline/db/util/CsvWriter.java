/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.offline.db.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class CsvWriter implements AutoCloseable {

    private final BufferedWriter writer;

    private final Lock lock = new ReentrantLock();

    private final char separator;

    public CsvWriter(Path file, boolean append, char separator) throws IOException {
        if (append) {
            writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } else {
            writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8);
        }
        this.separator = separator;
    }

    public CsvWriter writeLine(String... values) throws IOException {
        lock.lock();
        try {
            for (String value : values) {
                if (value != null) {
                    writer.append(value);
                }
                writer.append(separator);
            }
            writer.append("\n");
        } finally {
            lock.unlock();
        }
        return this;
    }

    public void flush() throws IOException {
        writer.flush();
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }

}