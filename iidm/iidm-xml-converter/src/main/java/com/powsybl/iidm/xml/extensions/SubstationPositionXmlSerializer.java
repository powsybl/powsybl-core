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
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.commons.xml.XmlWriterContext;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.extensions.Coordinate;
import com.powsybl.iidm.network.extensions.SubstationPosition;
import com.powsybl.iidm.network.extensions.SubstationPositionAdder;

import javax.xml.stream.XMLStreamException;

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
    public void write(SubstationPosition substationPosition, XmlWriterContext context) throws XMLStreamException {
        context.getWriter().writeEmptyElement(getNamespaceUri(), "coordinate");
        XmlUtil.writeDouble("longitude", substationPosition.getCoordinate().getLongitude(), context.getWriter());
        XmlUtil.writeDouble("latitude", substationPosition.getCoordinate().getLatitude(), context.getWriter());
    }

    @Override
    public SubstationPosition read(Substation substation, XmlReaderContext context) throws XMLStreamException {
        Coordinate[] coordinate = new Coordinate[1];
        XmlUtil.readUntilEndElement(getExtensionName(), context.getReader(), () -> {
            double longitude = XmlUtil.readDoubleAttribute(context.getReader(), "longitude");
            double latitude = XmlUtil.readDoubleAttribute(context.getReader(), "latitude");
            coordinate[0] = new Coordinate(latitude, longitude);
        });
        return substation.newExtension(SubstationPositionAdder.class)
                .withCoordinate(coordinate[0])
                .add();
    }

}
