/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtensionSerDe;
import com.powsybl.commons.extensions.ExtensionSerDe;
import com.powsybl.commons.io.DeserializerContext;
import com.powsybl.commons.io.SerializerContext;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.extensions.Coordinate;
import com.powsybl.iidm.network.extensions.SubstationPosition;
import com.powsybl.iidm.network.extensions.SubstationPositionAdder;

/**
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 */
@AutoService(ExtensionSerDe.class)
public class SubstationPositionSerDe extends AbstractExtensionSerDe<Substation, SubstationPosition> {

    private static final String COORDINATE_ROOT_NODE = "coordinate";

    public SubstationPositionSerDe() {
        super(SubstationPosition.NAME, "network", SubstationPosition.class, "substationPosition.xsd",
                "http://www.powsybl.org/schema/iidm/ext/substation_position/1_0", "sp");
    }

    @Override
    public void write(SubstationPosition substationPosition, SerializerContext context) {
        context.getWriter().writeStartNode(getNamespaceUri(), COORDINATE_ROOT_NODE);
        context.getWriter().writeDoubleAttribute("longitude", substationPosition.getCoordinate().getLongitude());
        context.getWriter().writeDoubleAttribute("latitude", substationPosition.getCoordinate().getLatitude());
        context.getWriter().writeEndNode();
    }

    @Override
    public SubstationPosition read(Substation substation, DeserializerContext context) {
        Coordinate[] coordinate = new Coordinate[1];
        context.getReader().readChildNodes(e -> {
            if (!e.equals(COORDINATE_ROOT_NODE)) {
                throw new PowsyblException("Unknown element name '" + e + "' in 'substationPosition'");
            }
            double longitude = context.getReader().readDoubleAttribute("longitude");
            double latitude = context.getReader().readDoubleAttribute("latitude");
            context.getReader().readEndNode();
            coordinate[0] = new Coordinate(latitude, longitude);
        });
        return substation.newExtension(SubstationPositionAdder.class)
                .withCoordinate(coordinate[0])
                .add();
    }

}
