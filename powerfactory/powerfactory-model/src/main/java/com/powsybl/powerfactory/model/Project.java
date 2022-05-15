/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.powerfactory.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.powsybl.commons.json.JsonUtil;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class Project {

    private final String name;

    private final DataObject rootObject;

    private final DataObjectIndex index;

    public Project(String name, DataObject rootObject, DataObjectIndex index) {
        this.name = Objects.requireNonNull(name);
        this.rootObject = Objects.requireNonNull(rootObject);
        this.index = Objects.requireNonNull(index);
    }

    public String getName() {
        return name;
    }

    public DataObject getRootObject() {
        return rootObject;
    }

    public DataObjectIndex getIndex() {
        return index;
    }

    static class ParsingContext {
        String name;

        final DataObjectIndex index = new DataObjectIndex();

        DataScheme scheme;

        DataObject rootObject;
    }

    static Project parseJson(JsonParser parser) {
        ParsingContext context = new ParsingContext();
        try {
            parser.nextToken();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        JsonUtil.parseObject(parser, fieldName -> {
            switch (fieldName) {
                case "name":
                    context.name = parser.nextTextValue();
                    return true;
                case "classes":
                    context.scheme = DataScheme.parseJson(parser);
                    return true;
                case "rootObject":
                    parser.nextToken();
                    context.rootObject = DataObject.parseJson(parser, context.index, context.scheme);
                    return true;
                default:
                    return false;
            }
        });
        return new Project(context.name, context.rootObject, context.index);
    }

    public static Project parseJson(Reader reader) {
        return JsonUtil.parseJson(reader, Project::parseJson);
    }

    public static Project readJson(Path file) {
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            return parseJson(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void writeJson(JsonGenerator generator) throws IOException {
        generator.writeStartObject();

        generator.writeStringField("name", name);

        DataScheme scheme = DataScheme.build(rootObject);
        scheme.writeJson(generator);

        generator.writeFieldName("rootObject");
        rootObject.writeJson(generator);

        generator.writeEndObject();
    }

    public void writeJson(Writer writer) {
        JsonUtil.writeJson(writer, generator -> {
            try {
                writeJson(generator);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    public void writeJson(Path file) {
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            writeJson(writer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Get active study case.
     * Following layout is expected:
     * MyProject.IntPrj
     *     pCase (OBJECT)
     *     'Study Cases'.IntPrjfolder
     *         MyStudyCase.Intcase
     *             iStudyTime (INTEGER64)
     *     'Network Model'.IntPrjfolder
     *         'Network Data'.IntPrjfolder
     *             MyNetwork1.ElmNet
     *             MyNetwork2.ElmNet
     *             ...
     * @return the active study case
     */
    public StudyCase getActiveStudyCase() {
        // get active study case
        DataObject studyCaseObj = rootObject.getObjectAttributeValue("pCase").resolve().orElseThrow();
        Instant studyTime = Instant.ofEpochSecond(studyCaseObj.getLongAttributeValue("iStudyTime"));
        String studyCaseName = rootObject.getLocName() + " - " + studyCaseObj.getLocName();
        DataObject netDataObj = rootObject.getChild("Network Model", "Network Data").orElseThrow();
        List<DataObject> elmNets = netDataObj.getChildrenByClass("ElmNet");
        return new StudyCase(studyCaseName, studyTime, elmNets, index);
    }
}
