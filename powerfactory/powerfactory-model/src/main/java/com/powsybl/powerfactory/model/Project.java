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

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
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

    static Project parseJson(Reader reader) {
        return JsonUtil.parseJson(reader, Project::parseJson);
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
}
