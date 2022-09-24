/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
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
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.extensions.LoadDetail;
import com.powsybl.iidm.network.extensions.LoadDetailAdder;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(ExtensionXmlSerializer.class)
public class LoadDetailXmlSerializer extends AbstractExtensionXmlSerializer<Load, LoadDetail> {

    public LoadDetailXmlSerializer() {
        super("detail", "network", LoadDetail.class,
                false, "loadDetail.xsd",
                "http://www.itesla_project.eu/schema/iidm/ext/load_detail/1_0",
                "ld");
    }

    @Override
    public void write(LoadDetail detail, XmlWriterContext context) {
        context.getWriter().writeDoubleAttribute("fixedActivePower", detail.getFixedActivePower());
        context.getWriter().writeDoubleAttribute("fixedReactivePower", detail.getFixedReactivePower());
        context.getWriter().writeDoubleAttribute("variableActivePower", detail.getVariableActivePower());
        context.getWriter().writeDoubleAttribute("variableReactivePower", detail.getVariableReactivePower());
    }

    @Override
    public LoadDetail read(Load load, XmlReaderContext context) {
        double fixedActivePower = context.getReader().readDoubleAttribute("fixedActivePower");
        if (Double.isNaN(fixedActivePower)) {
            fixedActivePower = context.getReader().readDoubleAttribute("subLoad1ActivePower");
        }
        double fixedReactivePower = context.getReader().readDoubleAttribute("fixedReactivePower");
        if (Double.isNaN(fixedReactivePower)) {
            fixedReactivePower = context.getReader().readDoubleAttribute("subLoad1ReactivePower");
        }
        double variableActivePower = context.getReader().readDoubleAttribute("variableActivePower");
        if (Double.isNaN(variableActivePower)) {
            variableActivePower = context.getReader().readDoubleAttribute("subLoad2ActivePower");
        }
        double variableReactivePower = context.getReader().readDoubleAttribute("variableReactivePower");
        if (Double.isNaN(variableReactivePower)) {
            variableReactivePower = context.getReader().readDoubleAttribute("subLoad2ReactivePower");
        }
        return load.newExtension(LoadDetailAdder.class)
                .withFixedActivePower(fixedActivePower)
                .withFixedReactivePower(fixedReactivePower)
                .withVariableActivePower(variableActivePower)
                .withVariableReactivePower(variableReactivePower)
                .add();
    }
}
