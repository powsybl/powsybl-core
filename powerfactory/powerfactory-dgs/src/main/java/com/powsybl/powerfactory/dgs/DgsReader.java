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

    public static Project read(Path dgsFile) {
        return read(dgsFile, StandardCharsets.ISO_8859_1);
    }

    public static Project read(Path dgsFile, Charset charset) {
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

    private DataObject createDataObject(long id, DataClass clazz) {
        if (objectsById.containsKey(id)) {
            throw new PowerFactoryException("Object '" + id + "' already exists");
        }
        DataObject object = new DataObject(id, clazz);
        objectsById.put(id, object);
        return object;
    }

    private DataClass createDataClass(String name) {
        DataClass clazz = classesByName.get(name);
        if (clazz == null) {
            clazz = new DataClass(name);
        }
        return clazz;
    }

    public Project read(String projectName, Reader reader) {
        Objects.requireNonNull(projectName);
        Objects.requireNonNull(reader);
        Stopwatch stopwatch = Stopwatch.createStarted();

        new DgsParser().read(reader, new DgsHandler() {

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
        });

        Objects.requireNonNull(elmNet, "ElmNet object is missing");

        // resolve links
        for (ToResolve toResolve : toResolveList) {
            DataObject obj = objectsById.get(toResolve.id);
            if (obj == null) {
                throw new PowerFactoryException("Object '" + toResolve.id + "' not found");
            }
            toResolve.obj.setObjectAttributeValue(toResolve.attributeName, obj);
        }

        // build parent child link
        List<DataObject> rootObjects = new ArrayList<>();
        for (DataObject obj : objectsById.values()) {
            DataObject folderObj = obj.findObjectAttributeValue("fold_id").orElse(null);
            if (folderObj != null) {
                obj.setParent(folderObj);
            } else {
                if (obj != elmNet && obj.getDataClassName().startsWith("Elm")) {
                    obj.setParent(elmNet);
                } else {
                    rootObjects.add(obj);
                }
            }
        }

        // create classes necessary for study structure
        DataClass intProj = createDataClass("IntPrj")
                .addAttribute(new DataAttribute("loc_name", DataAttributeType.STRING))
                .addAttribute(new DataAttribute("pCase", DataAttributeType.OBJECT));
        DataClass intPrjfolder = createDataClass("IntPrjfolder")
                .addAttribute(new DataAttribute("loc_name", DataAttributeType.STRING));
        DataClass intCase = createDataClass("IntCase")
                .addAttribute(new DataAttribute("loc_name", DataAttributeType.STRING));
        DataClass intRef = createDataClass("IntRef")
                .addAttribute(new DataAttribute("loc_name", DataAttributeType.STRING))
                .addAttribute(new DataAttribute("obj_id", DataAttributeType.OBJECT));
        DataClass setTime = createDataClass("SetTime")
                .addAttribute(new DataAttribute("loc_name", DataAttributeType.STRING))
                .addAttribute(new DataAttribute("datetime", DataAttributeType.INTEGER));

        long nextId = objectsById.keySet().stream().max(Long::compare).orElse(0L) + 1;

        // create project folder and add all roots to this folder
        DataObject projObj = createDataObject(nextId++, intProj)
                .setStringAttributeValue("loc_name", projectName);
        for (DataObject rootObject : rootObjects) {
            rootObject.setParent(projObj);
        }

        // create study cases folder
        DataObject studyCases = createDataObject(nextId++, intPrjfolder)
                .setStringAttributeValue("loc_name", "Study Cases")
                .setParent(projObj);

        // create study case
        DataObject studyCase = createDataObject(nextId++, intCase)
                .setStringAttributeValue("loc_name", "Study Case")
                .setParent(studyCases);

        // set study time
        createDataObject(nextId++, setTime)
                .setStringAttributeValue("loc_name", "Set Study Time")
                .setInstantAttributeValue("datetime", Instant.now())
                .setParent(studyCase);

        // create a link to network element
        DataObject summaryGrid = createDataObject(nextId++, classesByName.get("ElmNet"))
                .setStringAttributeValue("loc_name", "Summary Grid")
                .setParent(studyCase);
        createDataObject(nextId++, intRef)
                .setStringAttributeValue("loc_name", "Ref")
                .setObjectAttributeValue("obj_id", elmNet)
                .setParent(summaryGrid);

        // set active study case
        projObj.setObjectAttributeValue("pCase", studyCase);

        stopwatch.stop();
        LOGGER.info("DGS file read in {} ms: {} data objects", stopwatch.elapsed(TimeUnit.MILLISECONDS), objectsById.size());

        return new Project(projectName, Instant.now(), general, projObj, objectsById);
    }
}
