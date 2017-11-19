/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.storage.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.afs.storage.AppFileSystemStorage;
import com.powsybl.afs.storage.NodeId;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NodeIdJsonDeserializer extends StdDeserializer<NodeId> {

    private final transient AppFileSystemStorage storage;

    public NodeIdJsonDeserializer(AppFileSystemStorage storage) {
        super(NodeId.class);
        this.storage = Objects.requireNonNull(storage);
    }

    @Override
    public NodeId deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) {
        Objects.requireNonNull(storage, "NodeIdJsonDeserializer should not be serialized/deserialized");
        try {
            return storage.fromString(jsonParser.getValueAsString());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
