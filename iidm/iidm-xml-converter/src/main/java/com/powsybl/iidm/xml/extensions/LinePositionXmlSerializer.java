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
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.extensions.Coordinate;
import com.powsybl.iidm.network.extensions.LinePosition;
import com.powsybl.iidm.network.extensions.LinePositionAdder;

import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 */
@AutoService(ExtensionXmlSerializer.class)
public class LinePositionXmlSerializer<T extends Identifiable<T>> extends AbstractExtensionXmlSerializer<T, LinePosition<T>> {

    public LinePositionXmlSerializer() {
        super(LinePosition.NAME, "network", LinePosition.class, true, "linePosition.xsd",
                "http://www.powsybl.org/schema/iidm/ext/line_position/1_0", "lp");
    }

    @Override
    public void write(LinePosition<T> linePosition, XmlWriterContext context) throws XMLStreamException {
        for (Coordinate point : linePosition.getCoordinates()) {
            context.getWriter().writeEmptyElement(getNamespaceUri(), "coordinate");
            XmlUtil.writeDouble("longitude", point.getLongitude(), context.getWriter());
            XmlUtil.writeDouble("latitude", point.getLatitude(), context.getWriter());
        }
    }

    @Override
    public LinePosition<T> read(T line, XmlReaderContext context) throws XMLStreamException {
        List<Coordinate> coordinates = new ArrayList<>();
        XmlUtil.readUntilEndElement(getExtensionName(), context.getReader(), () -> {
            double longitude = XmlUtil.readDoubleAttribute(context.getReader(), "longitude");
            double latitude = XmlUtil.readDoubleAttribute(context.getReader(), "latitude");
            coordinates.add(new Coordinate(latitude, longitude));
        });
        LinePositionAdder<T> adder = line.newExtension(LinePositionAdder.class);
        return adder.withCoordinates(coordinates)
                .add();
    }
}
