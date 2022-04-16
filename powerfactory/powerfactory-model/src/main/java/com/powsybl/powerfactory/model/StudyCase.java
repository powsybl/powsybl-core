/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.powerfactory.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.powsybl.commons.json.JsonUtil;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class StudyCase {

    private final String name;

    private final Instant time;

    private final List<DataObject> elmNets;

    private final DataObjectIndex index;

    public StudyCase(String name, Instant time, List<DataObject> elmNets, DataObjectIndex index) {
        this.name = Objects.requireNonNull(name);
        this.time = Objects.requireNonNull(time);
        this.elmNets = Objects.requireNonNull(elmNets);
        this.index = Objects.requireNonNull(index);
    }

    public String getName() {
        return name;
    }

    public Instant getTime() {
        return time;
    }

    public List<DataObject> getElmNets() {
        return elmNets;
    }

    public DataObjectIndex getIndex() {
        return index;
    }

    public void writeJson(JsonGenerator generator) throws IOException {
        generator.writeStartObject();

        generator.writeStringField("name", name);

        DataScheme scheme = DataScheme.build(elmNets);
        scheme.writeJson(generator);

        generator.writeFieldName("elmNets");
        generator.writeStartArray();
        for (DataObject elmNet : elmNets) {
            elmNet.writeJson(generator);
        }
        generator.writeEndArray();

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
