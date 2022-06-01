/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
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
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.extensions.GeneratorStartup;
import com.powsybl.iidm.network.extensions.GeneratorStartupAdder;

import javax.xml.stream.XMLStreamException;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(ExtensionXmlSerializer.class)
public class GeneratorStartupXmlSerializer extends AbstractExtensionXmlSerializer<Generator, GeneratorStartup> implements ExtensionXmlSerializer<Generator, GeneratorStartup> {

    public GeneratorStartupXmlSerializer() {
        super(GeneratorStartup.NAME, "network", GeneratorStartup.class, false, "generatorStartup.xsd",
                "http://www.powsybl.org/schema/iidm/ext/generator_startup/1_0", "gs");
    }

    @Override
    public void write(GeneratorStartup startup, XmlWriterContext context) throws XMLStreamException {
        XmlUtil.writeDouble("plannedActivePowerSetpoint", startup.getPlannedActivePowerSetpoint(), context.getWriter());
        XmlUtil.writeDouble("startupCost", startup.getStartupCost(), context.getWriter());
        XmlUtil.writeDouble("marginalCost", startup.getMarginalCost(), context.getWriter());
        XmlUtil.writeDouble("plannedOutageRate", startup.getPlannedOutageRate(), context.getWriter());
        XmlUtil.writeDouble("forcedOutageRate", startup.getForcedOutageRate(), context.getWriter());
    }

    @Override
    public GeneratorStartup read(Generator generator, XmlReaderContext context) throws XMLStreamException {
        double plannedActivePowerSetpoint = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "plannedActivePowerSetpoint");
        double startUpCost = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "startupCost");
        double marginalCost = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "marginalCost");
        double plannedOutageRate = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "plannedOutageRate");
        double forcedOutageRate = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "forcedOutageRate");
        generator.newExtension(GeneratorStartupAdder.class)
                .withPlannedActivePowerSetpoint(plannedActivePowerSetpoint)
                .withStartupCost(startUpCost)
                .withMarginalCost(marginalCost)
                .withPlannedOutageRate(plannedOutageRate)
                .withForcedOutageRate(forcedOutageRate)
                .add();
        return generator.getExtension(GeneratorStartup.class);
    }
}
