/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
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
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class StudyCase extends AbstractPowerFactoryData {

    private final Instant time;

    private final List<DataObject> elmNets;

    public StudyCase(String name, Instant time, List<DataObject> elmNets, DataObjectIndex index) {
        super(name, index);
        this.time = Objects.requireNonNull(time);
        this.elmNets = Objects.requireNonNull(elmNets);
    }

    public Instant getTime() {
        return time;
    }

    public List<DataObject> getElmNets() {
        return elmNets;
    }

    static class ParsingContext {
        String name;

        Instant time;

        final DataObjectIndex index = new DataObjectIndex();

        DataScheme scheme;

        List<DataObject> elmNets;
    }

    static StudyCase parseJson(JsonParser parser) {
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
                case "time":
                    context.time = Instant.parse(parser.nextTextValue());
                    return true;
                case "classes":
                    context.scheme = DataScheme.parseJson(parser);
                    return true;
                case "objects":
                    JsonUtil.parseObjectArray(parser, obj -> { },
                        parser2 -> DataObject.parseJson(parser2, context.index, context.scheme));
                    return true;
                case "elmNets":
                    context.elmNets = JsonUtil.parseLongArray(parser).stream()
                            .map(id -> context.index.getDataObjectById(id)
                                    .orElseThrow(() -> new PowerFactoryException("ElmNet object " + id + " not found")))
                            .collect(Collectors.toList());
                    return true;
                default:
                    return false;
            }
        });
        return new StudyCase(context.name, context.time, context.elmNets, context.index);
    }

    public static StudyCase parseJson(Reader reader) {
        return JsonUtil.parseJson(reader, StudyCase::parseJson);
    }

    public static StudyCase readJson(Path file) {
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            return parseJson(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void writeJson(JsonGenerator generator) throws IOException {
        generator.writeStartObject();

        generator.writeStringField("name", name);
        generator.writeStringField("time", time.toString());

        DataScheme scheme = DataScheme.build(elmNets);
        scheme.writeJson(generator);

        generator.writeFieldName("objects");
        generator.writeStartArray();
        for (DataObject obj : index.getRootDataObjects()) {
            obj.writeJson(generator);
        }
        generator.writeEndArray();

        generator.writeFieldName("elmNets");
        generator.writeStartArray();
        for (DataObject elmNet : elmNets) {
            generator.writeNumber(elmNet.getId());
        }
        generator.writeEndArray();

        generator.writeEndObject();
    }
}
