/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.geodata.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.powsybl.iidm.geodata.elements.GeoShape;
import com.powsybl.iidm.network.extensions.Coordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Hugo Marcellin {@literal <hugo.marcelin at rte-france.com>}
 */

public class GeoShapeDeserializer extends StdDeserializer<GeoShape> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeoShapeDeserializer.class);

    public GeoShapeDeserializer() {
        super(Coordinate.class);
    }

    @Override
    public GeoShape deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        JsonNode node = parser.getCodec().readTree(parser);
        JsonNode coordinatesNode = node.get("coordinates");
        if (coordinatesNode == null || !coordinatesNode.isArray()) {
            throw new IOException("Invalid Geo Shape structure");
        }

        List<Coordinate> coordinateList = new ArrayList<>();
        for (JsonNode coordinateNode : coordinatesNode) {
            if (coordinateNode.isArray() && coordinateNode.size() == 2) {
                double longitude = coordinateNode.get(0).asDouble();
                double latitude = coordinateNode.get(1).asDouble();
                coordinateList.add(new Coordinate(latitude, longitude));
            } else {
                LOGGER.error("Invalid coordinate format encountered");
            }
        }

        return new GeoShape(coordinateList);
    }

    public static GeoShape read(String json) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(GeoShape.class, new GeoShapeDeserializer());
        mapper.registerModule(module);
        return mapper.readValue(json, GeoShape.class);
    }

}
