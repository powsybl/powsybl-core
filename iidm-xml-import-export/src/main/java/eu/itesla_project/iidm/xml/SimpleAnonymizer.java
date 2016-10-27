/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.xml;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import eu.itesla_project.iidm.network.Country;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SimpleAnonymizer implements Anonymizer {

    private static final String SEPARATOR = ";";

    private final BiMap<String, String> mapping = HashBiMap.create();

    public static String getAlpha(int num) {
        StringBuilder result = new StringBuilder();
        while (num > 0) {
            num--;
            int remainder = num % 26;
            char digit = (char) (remainder + 97);
            result.insert(0, digit);
            num = (num - remainder) / 26;
        }
        return result.toString();
    }

    @Override
    public String anonymizeString(String str) {
        if (str == null) {
            return null;
        }
        String str2 = mapping.get(str);
        if (str2 == null) {
            str2 = getAlpha(mapping.size() + 1).toUpperCase();
            mapping.put(str, str2);
        }
        return str2;
    }

    @Override
    public String deanonymizeString(String str) {
        if (str == null) {
            return null;
        }
        String str2 = mapping.inverse().get(str);
        if (str2 == null) {
            throw new RuntimeException("Mapping not for anonymized string '" + str + "'");
        }
        return str2;
    }

    @Override
    public Country anonymizeCountry(Country country) {
        return country;
    }

    @Override
    public Country deanonymizeCountry(Country country) {
        return country;
    }

    @Override
    public void read(BufferedReader reader) {
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(SEPARATOR);
                mapping.put(tokens[0], tokens[1]);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void write(BufferedWriter writer) {
        mapping.forEach((s, s2) -> {
            try {
                writer.write(s);
                writer.write(SEPARATOR);
                writer.write(s2);
                writer.newLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
