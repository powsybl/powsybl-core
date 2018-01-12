/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.extensions;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public interface ExtensionJson<T extends Extendable, E extends Extension<T>> {

    String getExtensionName();

    Class<? extends E> getExtensionClass();

    void serialize(E extension, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException;

    E deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException;
}
