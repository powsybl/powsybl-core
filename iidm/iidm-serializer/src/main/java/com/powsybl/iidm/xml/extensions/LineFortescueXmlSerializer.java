
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
import com.powsybl.commons.extensions.XmlReaderContext;
import com.powsybl.commons.extensions.XmlWriterContext;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.extensions.LineFortescue;
import com.powsybl.iidm.network.extensions.LineFortescueAdder;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
@AutoService(ExtensionXmlSerializer.class)
public class LineFortescueXmlSerializer extends AbstractExtensionXmlSerializer<Line, LineFortescue> {

    public LineFortescueXmlSerializer() {
        super("lineFortescue", "network", LineFortescue.class,
                "lineFortescue_V1_0.xsd", "http://www.powsybl.org/schema/iidm/ext/line_fortescue/1_0",
                "lf");
    }

    @Override
    public void write(LineFortescue lineFortescue, XmlWriterContext context) {
        context.getWriter().writeDoubleAttribute("rz", lineFortescue.getRz(), Double.NaN);
        context.getWriter().writeDoubleAttribute("xz", lineFortescue.getXz(), Double.NaN);
        context.getWriter().writeBooleanAttribute("openPhaseA", lineFortescue.isOpenPhaseA(), false);
        context.getWriter().writeBooleanAttribute("openPhaseB", lineFortescue.isOpenPhaseB(), false);
        context.getWriter().writeBooleanAttribute("openPhaseC", lineFortescue.isOpenPhaseC(), false);
    }

    @Override
    public LineFortescue read(Line line, XmlReaderContext context) {
        double rz = context.getReader().readDoubleAttribute("rz");
        double xz = context.getReader().readDoubleAttribute("xz");
        boolean openPhaseA = context.getReader().readBooleanAttribute("openPhaseA", false);
        boolean openPhaseB = context.getReader().readBooleanAttribute("openPhaseB", false);
        boolean openPhaseC = context.getReader().readBooleanAttribute("openPhaseC", false);
        context.getReader().readEndNode();
        return line.newExtension(LineFortescueAdder.class)
                .withRz(rz)
                .withXz(xz)
                .withOpenPhaseA(openPhaseA)
                .withOpenPhaseB(openPhaseB)
                .withOpenPhaseC(openPhaseC)
                .add();
    }
}
