/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package com.powsybl.iidm.xml.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.AbstractExtensionXmlSerializer;
import com.powsybl.commons.extensions.ExtensionXmlSerializer;
import com.powsybl.commons.extensions.XmlReaderContext;
import com.powsybl.commons.extensions.XmlWriterContext;
import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.extensions.HvdcOperatorActivePowerRange;
import com.powsybl.iidm.network.extensions.HvdcOperatorActivePowerRangeAdder;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 * @author Paul Bui-Quang {@literal <paul.buiquang at rte-france.com>}
 */
@AutoService(ExtensionXmlSerializer.class)
public class HvdcOperatorActivePowerRangeXmlSerializer extends AbstractExtensionXmlSerializer<HvdcLine, HvdcOperatorActivePowerRange> {

    public HvdcOperatorActivePowerRangeXmlSerializer() {
        super("hvdcOperatorActivePowerRange", "network", HvdcOperatorActivePowerRange.class,
                "hvdcOperatorActivePowerRange.xsd",
                "http://www.itesla_project.eu/schema/iidm/ext/hvdc_operator_active_power_range/1_0",
                "hopr");
    }

    @Override
    public void write(HvdcOperatorActivePowerRange opr, XmlWriterContext context) {
        context.getWriter().writeFloatAttribute("fromCS1toCS2", opr.getOprFromCS1toCS2());
        context.getWriter().writeFloatAttribute("fromCS2toCS1", opr.getOprFromCS2toCS1());
    }

    @Override
    public HvdcOperatorActivePowerRange read(HvdcLine hvdcLine, XmlReaderContext context) {
        float oprFromCS1toCS2 = context.getReader().readFloatAttribute("fromCS1toCS2");
        float oprFromCS2toCS1 = context.getReader().readFloatAttribute("fromCS2toCS1");
        context.getReader().readEndNode();
        return hvdcLine.newExtension(HvdcOperatorActivePowerRangeAdder.class)
                .withOprFromCS1toCS2(oprFromCS1toCS2)
                .withOprFromCS2toCS1(oprFromCS2toCS1)
                .add();
    }
}
