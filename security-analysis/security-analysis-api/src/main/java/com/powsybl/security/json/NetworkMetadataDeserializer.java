/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.google.common.base.Suppliers;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.extensions.ExtensionJsonSerializer;
import com.powsybl.commons.extensions.ExtensionProviders;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.security.NetworkMetadata;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
public class NetworkMetadataDeserializer extends StdDeserializer<NetworkMetadata> {

    private static final Supplier<ExtensionProviders<ExtensionJsonSerializer>> SUPPLIER =
        Suppliers.memoize(() -> ExtensionProviders.createProvider(ExtensionJsonSerializer.class, "security-analysis"));

    public NetworkMetadataDeserializer() {
        super(NetworkMetadata.class);
    }

    @Override
    public NetworkMetadata deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        String id = null;
        String sourceFormat = null;
        ZonedDateTime caseDate = null;
        int forecastDistance = 0;

        List<Extension<NetworkMetadata>> extensions = Collections.emptyList();

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.currentName()) {
                case "id":
                    id = parser.nextTextValue();
                    break;

                case "sourceFormat":
                    sourceFormat = parser.nextTextValue();
                    break;

                case "caseDate":
                    caseDate = ZonedDateTime.parse(parser.nextTextValue());
                    break;

                case "forecastDistance":
                    forecastDistance = parser.nextIntValue(0);
                    break;

                case "extensions":
                    parser.nextToken();
                    extensions = JsonUtil.readExtensions(parser, deserializationContext, SUPPLIER.get());
                    break;

                default:
                    throw new PowsyblException("Unexpected field: " + parser.currentName());
            }
        }

        NetworkMetadata metadata = new NetworkMetadata(id, sourceFormat, caseDate, forecastDistance);
        SUPPLIER.get().addExtensions(metadata, extensions);

        return metadata;
    }
}
