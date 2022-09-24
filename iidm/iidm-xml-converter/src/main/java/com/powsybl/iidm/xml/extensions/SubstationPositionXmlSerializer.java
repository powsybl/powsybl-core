/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.AbstractExtensionXmlSerializer;
import com.powsybl.commons.extensions.ExtensionXmlSerializer;
import com.powsybl.commons.xml.XmlReaderContext;
import com.powsybl.commons.xml.XmlWriterContext;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.extensions.Coordinate;
import com.powsybl.iidm.network.extensions.SubstationPosition;
import com.powsybl.iidm.network.extensions.SubstationPositionAdder;

/**
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
@AutoService(ExtensionXmlSerializer.class)
public class SubstationPositionXmlSerializer extends AbstractExtensionXmlSerializer<Substation, SubstationPosition> {

    public SubstationPositionXmlSerializer() {
        super(SubstationPosition.NAME, "network", SubstationPosition.class, true, "substationPosition.xsd",
                "http://www.powsybl.org/schema/iidm/ext/substation_position/1_0", "sp");
    }

    @Override
    public void write(SubstationPosition substationPosition, XmlWriterContext context) {
        context.getWriter().writeEmptyNode(getNamespaceUri(), "coordinate");
        context.getWriter().writeDoubleAttribute("longitude", substationPosition.getCoordinate().getLongitude());
        context.getWriter().writeDoubleAttribute("latitude", substationPosition.getCoordinate().getLatitude());
    }

    @Override
    public SubstationPosition read(Substation substation, XmlReaderContext context) {
        Coordinate[] coordinate = new Coordinate[1];
        context.getReader().readUntilEndNode(getExtensionName(), () -> {
            double longitude = context.getReader().readDoubleAttribute("longitude");
            double latitude = context.getReader().readDoubleAttribute("latitude");
            coordinate[0] = new Coordinate(latitude, longitude);
        });
        return substation.newExtension(SubstationPositionAdder.class)
                .withCoordinate(coordinate[0])
                .add();
    }

}
