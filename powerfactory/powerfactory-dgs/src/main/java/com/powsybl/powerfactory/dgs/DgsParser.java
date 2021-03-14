/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.powerfactory.dgs;

import com.powsybl.powerfactory.model.PowerFactoryException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class DgsParser {

    private static final Pattern ATTR_DESCR_PATTERN = Pattern.compile("(.+)\\(([airp]+)(:\\d*)?\\)");

    public void read(Reader reader, DgsHandler handler) {
        Objects.requireNonNull(reader);
        Objects.requireNonNull(handler);
        try (BufferedReader bufferedReader = new BufferedReader(reader)) {
            String line;
            String[] attributesName = null;
            char[] attributesType = null;
            while ((line = bufferedReader.readLine()) != null) {
                String trimmedLine = line.trim();

                if (trimmedLine.isEmpty() || trimmedLine.startsWith("*")) {
                    continue;
                }

                if (trimmedLine.startsWith("$$")) { // new table
                    String[] fields = trimmedLine.split(";");

                    String className = fields[0].substring(2);
                    handler.onTableHeader(className);

                    attributesName = new String[fields.length - 1];
                    attributesType = new char[fields.length - 1];

                    for (int i = 1; i < fields.length; i++) {
                        String field = fields[i];

                        Matcher matcher = ATTR_DESCR_PATTERN.matcher(field);
                        if (!matcher.matches()) {
                            throw new PowerFactoryException("Invalid attribute description: '" + field + "'");
                        }

                        String attributeName = matcher.group(1);
                        char attributeType = matcher.group(2).charAt(0);
                        handler.onAttributeDescription(attributeName, attributeType);

                        attributesName[i - 1] = attributeName;
                        attributesType[i - 1] = attributeType;
                    }
                } else {
                    Objects.requireNonNull(attributesName);
                    Objects.requireNonNull(attributesType);

                    String[] fields = trimmedLine.split(";");

                    for (int i = 0; i < fields.length; i++) {
                        String field = fields[i];

                        if (field.isEmpty()) {
                            continue;
                        }

                        String attributeName = attributesName[i];
                        char attributeType = attributesType[i];
                        switch (attributeType) {
                            case 'a':
                                handler.onStringValue(attributeName, field);
                                break;

                            case 'i':
                                handler.onIntegerValue(attributeName, Integer.parseInt(field));
                                break;

                            case 'r':
                                handler.onRealValue(attributeName, Float.parseFloat(field));
                                break;

                            case 'p':
                                handler.onObjectValue(attributeName, Long.parseLong(field));
                                break;

                            default:
                                throw new PowerFactoryException("Unexpected attribut type:" + attributeType);
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}





