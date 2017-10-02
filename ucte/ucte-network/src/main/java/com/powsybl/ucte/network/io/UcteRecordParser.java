/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ucte.network.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class UcteRecordParser {

    private final BufferedReader reader;

    private String line;

    private final Set<UcteRecordType> parsedRecordTypes = EnumSet.noneOf(UcteRecordType.class);

    UcteRecordParser(BufferedReader reader) throws IOException {
        this.reader = reader;
        nextLine();
    }

    String getLine() {
        return line;
    }

    boolean nextLine() throws IOException {
        do {
            line = reader.readLine();
        } while (line != null && line.trim().isEmpty()); // skip empty lines
        return line != null;
    }

    UcteRecordType scanRecordType() {
        if (line != null && line.startsWith("##") && line.length() >= 3) {
            for (UcteRecordType recordType : UcteRecordType.values()) {
                if (line.startsWith("##" + recordType)) {
                    parsedRecordTypes.add(recordType);
                    return recordType;
                }
            }
        }
        return null;
    }

    Set<UcteRecordType> getParsedRecordTypes() {
        return parsedRecordTypes;
    }

    String parseString(int beginIndex, int endIndex) {
        return parseString(beginIndex, endIndex, true);
    }

    String parseString(int beginIndex, int endIndex, boolean trim) {
        String untrimmed = line == null || endIndex > line.length() ? null : line.substring(beginIndex, endIndex);
        return untrimmed == null || !trim ? untrimmed : untrimmed.trim();
    }

    Character parseChar(int index) {
        return line == null || index > line.length() ? null : line.charAt(index);
    }

    Integer parseInt(int beginIndex, int endIndex) {
        String str = parseString(beginIndex, endIndex);
        return str == null || str.trim().isEmpty() ? null : Integer.valueOf(str);
    }

    Integer parseInt(int index) {
        Character c = parseChar(index);
        return c == null || c == ' ' ? null : Integer.valueOf(Character.toString(c));
    }

    float parseFloat(int beginIndex, int endIndex) {
        String str = parseString(beginIndex, endIndex);
        return str == null || str.trim().isEmpty() ? Float.NaN : Float.valueOf(str);
    }

    <E extends Enum<E>> E parseEnumOrdinal(int index, Class<E> clazz) {
        Integer order = parseInt(index);
        return order == null ? null : clazz.getEnumConstants()[order];
    }

    <E extends Enum<E>> E parseEnumValue(int beginIndex, int endIndex, Class<E> clazz) {
        String name = parseString(beginIndex, endIndex);
        return name == null || name.trim().isEmpty() ? null : Enum.valueOf(clazz, name);
    }

    <E extends Enum<E>> E parseEnumValue(int index, Class<E> clazz) {
        Character c = parseChar(index);
        return c == null || c == ' ' ? null : Enum.valueOf(clazz, Character.toString(c));
    }
}
