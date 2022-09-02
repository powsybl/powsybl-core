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
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.commons.xml.XmlWriterContext;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.extensions.LoadDetail;
import com.powsybl.iidm.network.extensions.LoadDetailAdder;

import javax.xml.stream.XMLStreamException;

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
    public void write(LoadDetail detail, XmlWriterContext context) throws XMLStreamException {
        XmlUtil.writeDouble("fixedActivePower", detail.getFixedActivePower(), context.getWriter());
        XmlUtil.writeDouble("fixedReactivePower", detail.getFixedReactivePower(), context.getWriter());
        XmlUtil.writeDouble("variableActivePower", detail.getVariableActivePower(), context.getWriter());
        XmlUtil.writeDouble("variableReactivePower", detail.getVariableReactivePower(), context.getWriter());
    }

    @Override
    public LoadDetail read(Load load, XmlReaderContext context) {
        double fixedActivePower = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "fixedActivePower");
        if (Double.isNaN(fixedActivePower)) {
            fixedActivePower = XmlUtil.readDoubleAttribute(context.getReader(), "subLoad1ActivePower");
        }
        double fixedReactivePower = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "fixedReactivePower");
        if (Double.isNaN(fixedReactivePower)) {
            fixedReactivePower = XmlUtil.readDoubleAttribute(context.getReader(), "subLoad1ReactivePower");
        }
        double variableActivePower = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "variableActivePower");
        if (Double.isNaN(variableActivePower)) {
            variableActivePower = XmlUtil.readDoubleAttribute(context.getReader(), "subLoad2ActivePower");
        }
        double variableReactivePower = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "variableReactivePower");
        if (Double.isNaN(variableReactivePower)) {
            variableReactivePower = XmlUtil.readDoubleAttribute(context.getReader(), "subLoad2ReactivePower");
        }
        return load.newExtension(LoadDetailAdder.class)
                .withFixedActivePower(fixedActivePower)
                .withFixedReactivePower(fixedReactivePower)
                .withVariableActivePower(variableActivePower)
                .withVariableReactivePower(variableReactivePower)
                .add();
    }
}
