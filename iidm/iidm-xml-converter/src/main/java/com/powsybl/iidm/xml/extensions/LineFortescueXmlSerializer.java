/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
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
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.extensions.LineFortescue;
import com.powsybl.iidm.network.extensions.LineFortescueAdder;

import javax.xml.stream.XMLStreamException;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(ExtensionXmlSerializer.class)
public class LineFortescueXmlSerializer extends AbstractExtensionXmlSerializer<Line, LineFortescue> {

    public LineFortescueXmlSerializer() {
        super("lineFortescue", "network", LineFortescue.class, false,
                "lineFortescue_V1_0.xsd", "http://www.powsybl.org/schema/iidm/ext/line_fortescue/1_0",
                "lf");
    }

    @Override
    public void write(LineFortescue lineFortescue, XmlWriterContext context) throws XMLStreamException {
        XmlUtil.writeOptionalDouble("rz", lineFortescue.getRz(), Double.NaN, context.getWriter());
        XmlUtil.writeOptionalDouble("xz", lineFortescue.getXz(), Double.NaN, context.getWriter());
        XmlUtil.writeOptionalBoolean("openPhaseA", lineFortescue.isOpenPhaseA(), false, context.getWriter());
        XmlUtil.writeOptionalBoolean("openPhaseB", lineFortescue.isOpenPhaseB(), false, context.getWriter());
        XmlUtil.writeOptionalBoolean("openPhaseC", lineFortescue.isOpenPhaseC(), false, context.getWriter());
    }

    @Override
    public LineFortescue read(Line line, XmlReaderContext context) throws XMLStreamException {
        double rz = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "rz");
        double xz = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "xz");
        boolean openPhaseA = XmlUtil.readOptionalBoolAttribute(context.getReader(), "openPhaseA", false);
        boolean openPhaseB = XmlUtil.readOptionalBoolAttribute(context.getReader(), "openPhaseB", false);
        boolean openPhaseC = XmlUtil.readOptionalBoolAttribute(context.getReader(), "openPhaseC", false);
        return line.newExtension(LineFortescueAdder.class)
                .withRz(rz)
                .withXz(xz)
                .withOpenPhaseA(openPhaseA)
                .withOpenPhaseB(openPhaseB)
                .withOpenPhaseC(openPhaseC)
                .add();
    }
}
