/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.powerfactory.dgs;

import com.google.common.base.Stopwatch;
import com.powsybl.powerfactory.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class DgsReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(DgsReader.class);

    private final DataObjectIndex index = new DataObjectIndex();

    private final Map<String, DataClass> classesByName = new HashMap<>();

    private final Map<String, String> general = new HashMap<>();

    public static StudyCase read(Path dgsFile) {
        return read(dgsFile, StandardCharsets.ISO_8859_1);
    }

    public static StudyCase read(Path dgsFile, Charset charset) {
        try (Reader reader = Files.newBufferedReader(dgsFile, charset)) {
            return new DgsReader().read(dgsFile.getFileName().toString(), reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static DataAttributeType getDataAttributeType(char attributeType) {
        DataAttributeType type;
        switch (attributeType) {
            case 'a':
                type = DataAttributeType.STRING;
                break;
            case 'i':
                type = DataAttributeType.INTEGER;
                break;
            case 'r':
                type = DataAttributeType.FLOAT;
                break;
            case 'p':
                type = DataAttributeType.OBJECT;
                break;
            default:
                throw new AssertionError("Unexpected attribute type: " + attributeType);
        }
        return type;
    }

    private void buildObjectTree() {
        for (DataObject obj : index.getDataObjects()) {
            obj.findObjectAttributeValue(DataAttribute.FOLD_ID)
                    .flatMap(DataObjectRef::resolve)
                    .ifPresent(obj::setParent);
        }
    }

    private class DgsHandlerImpl implements DgsHandler {

        private DataClass clazz;

        private DataObject object;

        private String descr;

        @Override
        public void onTableHeader(String tableName) {
            if (!tableName.equals("General")) {
                // this is a class description
                clazz = new DataClass(tableName);
                classesByName.put(clazz.getName(), clazz);
            }
        }

        @Override
        public void onAttributeDescription(String attributeName, char attributeType) {
            if (clazz != null && !attributeName.equals("ID")) {
                DataAttributeType type = getDataAttributeType(attributeType);
                clazz.addAttribute(new DataAttribute(attributeName, type, ""));
            }
        }

        @Override
        public void onStringValue(String attributeName, String value) {
            if (clazz != null) {
                if ("ID".equals(attributeName)) {
                    long id = Long.parseLong(value);
                    object = new DataObject(id, clazz, index);
                } else {
                    object.setStringAttributeValue(attributeName, value);
                }
            } else {
                if ("Descr".equals(attributeName)) {
                    descr = value;
                } else if ("Val".equals(attributeName)) {
                    general.put(descr, value);
                }
            }
        }

        @Override
        public void onIntegerValue(String attributeName, int value) {
            object.setIntAttributeValue(attributeName, value);
        }

        @Override
        public void onRealValue(String attributeName, float value) {
            object.setFloatAttributeValue(attributeName, value);
        }

        @Override
        public void onObjectValue(String attributeName, long id) {
            object.setObjectAttributeValue(attributeName, id);
        }
    }

    public StudyCase read(String studyCaseName, Reader reader) {
        Objects.requireNonNull(studyCaseName);
        Objects.requireNonNull(reader);
        Stopwatch stopwatch = Stopwatch.createStarted();

        new DgsParser().read(reader, new DgsHandlerImpl());

        // build object tree (so resolve parents / children links)
        buildObjectTree();

        stopwatch.stop();
        LOGGER.info("DGS file read in {} ms: {} data objects", stopwatch.elapsed(TimeUnit.MILLISECONDS), index.getDataObjects().size());

        return new StudyCase(studyCaseName, Instant.now(), index);
    }
}
