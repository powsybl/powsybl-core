/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.ddb.eurostag;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class IdDictionary {

    private final Multimap<String, String> dict = HashMultimap.create();

    public void loadCsv(Path file, int id1column, int id2column) throws IOException {
        int minColumnCount = Math.max(id1column, id2column);
        try (BufferedReader r = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = r.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                if (line.startsWith("#")) {
                    continue;
                }
                String[] tokens = line.split(";");
                if (tokens.length < minColumnCount + 1) {
                    throw new RuntimeException("Error parsing '" + line + "'");
                }
                add(tokens[id1column], tokens[id2column]);
            }
        }
    }

    public void add(String id1, String id2) {
        dict.put(id1, id2.replace(' ', '_'));
    }

    public String get(String id) {
        Collection<String> ids = dict.get(id);
        if (ids.size() > 0) {
            return ids.iterator().next();
        }
        return null;
    }

    public Collection<String> getAll(String id) {
        return dict.get(id);
    }

    public int size() {
        return dict.size();
    }

    public void dump() {
        for (Map.Entry<String, String> entry : dict.entries()) {
            System.out.println(entry.getKey() + ";" + entry.getValue());
        }
    }

}
