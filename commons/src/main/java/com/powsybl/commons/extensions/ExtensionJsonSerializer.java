/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.extensions;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * An {@link ExtensionProvider} able to serialize/deserialize extensions from JSON.
 *
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
public interface ExtensionJsonSerializer<T extends Extendable, E extends Extension<T>> extends ExtensionProvider<T, E> {

    /**
     * Serializes the provided extension to JSON.
     */
    void serialize(E extension, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException;

    /**
     * Deserializes the provided JSON to an extension of type {@code E}.
     */
    E deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException;

    /**
     * Deserializes the provided JSON to update the provided extension. Returns the updated extension.
     *
     * <p>The default implementation only returns a new object as provided by {@link #deserialize(JsonParser, DeserializationContext)},
     * therefore interface implementations must provide their own implementation if they want the extension to actually be updatable from JSON.
     */
    default E deserializeAndUpdate(JsonParser jsonParser, DeserializationContext deserializationContext, E extension) throws IOException {
        return deserialize(jsonParser, deserializationContext);
    }
}
