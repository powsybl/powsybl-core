/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.offline.server.message;

import java.io.StringWriter;
import javax.json.Json;
import javax.json.stream.JsonGenerator;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public abstract class Message {

    protected abstract String getType();

    protected abstract void toJson(JsonGenerator generator);

    public String toJson() {
        StringWriter writer = new StringWriter();
        try (JsonGenerator generator = Json.createGenerator(writer)) {
            generator.writeStartObject()
                    .write("type", getType());
            toJson(generator);
            generator.writeEnd();
        }
        writer.flush();
        return writer.toString();
    }

    public static <U extends Message> String toJson(Iterable<U> messages) {
        StringWriter writer = new StringWriter();
        try (JsonGenerator generator = Json.createGenerator(writer)) {
            generator.writeStartArray();
            for (U message : messages) {
                generator.writeStartObject();
                message.toJson(generator);
                generator.writeEnd();
            }
            generator.writeEnd();
        }
        writer.flush();
        return writer.toString();
    }

    @Override
    public String toString() {
        return toJson();
    }
}
