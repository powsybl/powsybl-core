/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ShortIdDictionary {

    private final Map<String, String> map = new HashMap<>();

    public ShortIdDictionary(Set<String> ids) {
        char c = 'A';
        for (String id : ids) {
            if (!map.containsKey(id)) {
                map.put(id, Character.toString(c++));
            }
        }
    }

    public ShortIdDictionary(Path file) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(";");
                map.put(tokens[0], tokens[1]);
            }
        }
    }

    public String getShortId(String realId) {
        return map.get(realId);
    }

    public void write(Path file) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            for (Map.Entry<String, String> e : map.entrySet()) {
                writer.write(e.getKey());
                writer.write(";");
                writer.write(e.getValue());
                writer.newLine();
            }
        }
    }
}
