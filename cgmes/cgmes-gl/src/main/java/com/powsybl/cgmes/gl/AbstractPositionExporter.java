/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.gl;

import com.powsybl.cgmes.model.CgmesNamespace;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.TripleStore;
import com.powsybl.iidm.network.extensions.Coordinate;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

/**
 *
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 */
public abstract class AbstractPositionExporter {

    protected TripleStore tripleStore;
    protected ExportContext context;

    private static final String IDENTIFIED_OBJECT_NAME = "IdentifiedObject.name";
    private static final String COORDINATE_SYSTEM = "CoordinateSystem";
    private static final String POWER_SYSTEM_RESOURCES = "PowerSystemResources";
    private static final String LOCATION = "Location";
    private static final String X_POSITION = "xPosition";
    private static final String Y_POSITION = "yPosition";
    private static final String SEQUENCE_NUMBER = "sequenceNumber";

    protected AbstractPositionExporter(TripleStore tripleStore, ExportContext context) {
        this.tripleStore = Objects.requireNonNull(tripleStore);
        this.context = Objects.requireNonNull(context);
    }

    protected String addLocation(String id, String name) {

        PropertyBag locationProperties = new PropertyBag(Arrays.asList(IDENTIFIED_OBJECT_NAME, COORDINATE_SYSTEM, POWER_SYSTEM_RESOURCES), true);
        locationProperties.setResourceNames(Arrays.asList(COORDINATE_SYSTEM, POWER_SYSTEM_RESOURCES));
        locationProperties.setClassPropertyNames(Collections.singletonList(IDENTIFIED_OBJECT_NAME));
        locationProperties.put(IDENTIFIED_OBJECT_NAME, name);
        locationProperties.put(POWER_SYSTEM_RESOURCES, id);
        locationProperties.put(COORDINATE_SYSTEM, context.getCoordinateSystemId());

        return tripleStore.add(context.getGlContext(), CgmesNamespace.CIM_16_NAMESPACE, LOCATION, locationProperties);
    }

    protected void addLocationPoint(String locationId, Coordinate coordinate, int seq) {
        PropertyBag locationPointProperties = (seq == 0)
                ? new PropertyBag(Arrays.asList(X_POSITION, Y_POSITION, LOCATION), true)
                : new PropertyBag(Arrays.asList(SEQUENCE_NUMBER, X_POSITION, Y_POSITION, LOCATION), true);
        locationPointProperties.setResourceNames(Collections.singletonList(LOCATION));
        if (seq > 0) {
            locationPointProperties.put(SEQUENCE_NUMBER, Integer.toString(seq));
        }
        locationPointProperties.put(X_POSITION, Double.toString(coordinate.getLongitude()));
        locationPointProperties.put(Y_POSITION, Double.toString(coordinate.getLatitude()));
        locationPointProperties.put(LOCATION, locationId);
        tripleStore.add(context.getGlContext(), CgmesNamespace.CIM_16_NAMESPACE, "PositionPoint", locationPointProperties);
    }

}
