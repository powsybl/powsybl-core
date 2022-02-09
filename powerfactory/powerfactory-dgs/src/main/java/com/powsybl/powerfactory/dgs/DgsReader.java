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
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class DgsReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(DgsReader.class);

    private final Map<Long, DataObject> objectsById = new HashMap<>();

    private final Map<String, DataClass> classesByName = new HashMap<>();

    private final List<ToResolve> toResolveList = new ArrayList<>();

    private final Map<String, String> general = new HashMap<>();

    private DataObject elmNet;

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

    private static final class ToResolve {

        private final DataObject obj;

        private final String attributeName;

        private final long id;

        private ToResolve(DataObject obj, String attributeName, long id) {
            this.obj = obj;
            this.attributeName = attributeName;
            this.id = id;
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

    private void resolveLinks() {
        for (ToResolve toResolve : toResolveList) {
            DataObject obj = objectsById.get(toResolve.id);
            if (obj == null) {
                throw new PowerFactoryException("Object '" + toResolve.id + "' not found");
            }
            toResolve.obj.setObjectAttributeValue(toResolve.attributeName, obj);
        }
    }

    private void buildObjectsTree() {
        for (DataObject obj : objectsById.values()) {
            DataObject folderObj = obj.findObjectAttributeValue(DataAttribute.FOLD_ID).orElse(null);
            if (folderObj != null) {
                obj.setParent(folderObj);
            } else {
                if (obj != elmNet && obj.getDataClassName().startsWith("Elm")) {
                    obj.setParent(elmNet);
                }
            }
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

        private DataObject createDataObject(long id, DataClass clazz) {
            if (objectsById.containsKey(id)) {
                throw new PowerFactoryException("Object '" + id + "' already exists");
            }
            DataObject object = new DataObject(id, clazz);
            objectsById.put(id, object);
            return object;
        }

        @Override
        public void onStringValue(String attributeName, String value) {
            if (clazz != null) {
                if ("ID".equals(attributeName)) {
                    long id = Long.parseLong(value);
                    object = createDataObject(id, clazz);
                    if (clazz.getName().equals("ElmNet")) {
                        elmNet = object;
                    }
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
            toResolveList.add(new ToResolve(object, attributeName, id));
        }
    }

    public StudyCase read(String studyCaseName, Reader reader) {
        Objects.requireNonNull(studyCaseName);
        Objects.requireNonNull(reader);
        Stopwatch stopwatch = Stopwatch.createStarted();

        new DgsParser().read(reader, new DgsHandlerImpl());

        Objects.requireNonNull(elmNet, "ElmNet object is missing");

        // resolve object attributes links
        resolveLinks();

        // build parent child link
        buildObjectsTree();

        stopwatch.stop();
        LOGGER.info("DGS file read in {} ms: {} data objects", stopwatch.elapsed(TimeUnit.MILLISECONDS), objectsById.size());

        StudyCase studyCase = new StudyCase(studyCaseName, Instant.now(), List.of(elmNet));
        for (DataObject obj : objectsById.values()) {
            obj.setStudyCase(studyCase);
        }
        return studyCase;
    }
}
