/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.util;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.powsybl.commons.PowsyblException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Map;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class StringToIntMapper<S extends Enum<S> & IntCounter> {

    private final Class<S> clazz;

    private final Map<S, BiMap<String, Integer>> id2num;

    private final Map<S, Integer> counter;

    private boolean modified = false;

    public StringToIntMapper(Class<S> clazz) {
        this.clazz = clazz;
        id2num = new EnumMap<>(clazz);
        counter = new EnumMap<>(clazz);
        for (S s : clazz.getEnumConstants()) {
            id2num.put(s, HashBiMap.<String, Integer>create());
            counter.put(s, s.getInitialValue());
        }
    }

    private static IllegalArgumentException createSubsetIsNullException() {
        return new IllegalArgumentException("subset is null");
    }

    public synchronized int newInt(S subset, String id) {
        if (subset == null) {
            throw createSubsetIsNullException();
        }
        if (id == null) {
            throw new IllegalArgumentException("id is null");
        }
        Integer num = id2num.get(subset).get(id);
        if (num == null) {
            num = counter.get(subset);
            counter.put(subset, num + 1);
            id2num.get(subset).put(id, num);
            modified = true;
        }
        return num;
    }

    public synchronized int getInt(S subset, String id) {
        if (subset == null) {
            throw createSubsetIsNullException();
        }
        if (id == null) {
            throw new IllegalArgumentException("id is null");
        }
        Integer num = id2num.get(subset).get(id);
        if (num == null) {
            throw new IllegalStateException("No mapping found for id '" + id + "'");
        }
        return num;
    }

    public synchronized String getId(S subset, int num) {
        if (subset == null) {
            throw createSubsetIsNullException();
        }
        if (num < subset.getInitialValue() || num >= counter.get(subset)) {
            throw new IllegalArgumentException("invalid num " + num);
        }
        return id2num.get(subset).inverse().get(num);
    }

    public synchronized boolean isMapped(S subset, String id) {
        Map<String, Integer> numbers = id2num.get(subset);
        return numbers.containsKey(id);
    }

    public synchronized boolean isModified() {
        return modified;
    }

    public synchronized void dump(Writer writer) throws IOException {
        for (Map.Entry<S, BiMap<String, Integer>> entry : id2num.entrySet()) {
            S subset = entry.getKey();
            for (Map.Entry<String, Integer> entry1 : entry.getValue().entrySet()) {
                String id = entry1.getKey();
                Integer num = entry1.getValue();
                writer.write(subset + ";" + id + ";" + num + System.lineSeparator());
            }
        }
    }

    public void dump(Path file) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(file, Charset.forName("UTF-8"))) {
            dump(writer);
        }
    }

    public synchronized void load(BufferedReader reader) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.trim().isEmpty()) {
                continue;
            }
            String[] tokens = line.split(";");
            if (tokens.length != 3) {
                throw new PowsyblException("Bad format: " + line);
            }
            S subset = Enum.valueOf(clazz, tokens[0]);
            String id = tokens[1];
            int num = Integer.parseInt(tokens[2]);
            id2num.get(subset).put(id, num);
            counter.put(subset, Math.max(counter.get(subset), num) + 1);
        }
    }

    public void load(Path file) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(file, Charset.forName("UTF-8"))) {
            load(reader);
        }
    }

    public synchronized void reset(S subset) {
        if (subset == null) {
            throw new IllegalArgumentException("subset is null");
        }
        id2num.put(subset, HashBiMap.<String, Integer>create());
        counter.put(subset, subset.getInitialValue());
    }

}
