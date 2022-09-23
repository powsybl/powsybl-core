/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml.extensions;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.ExtensionXmlSerializer;
import com.powsybl.commons.xml.XmlReaderContext;
import com.powsybl.commons.xml.XmlWriterContext;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.extensions.GeneratorStartup;
import com.powsybl.iidm.network.extensions.GeneratorStartupAdder;
import com.powsybl.iidm.xml.IidmXmlVersion;
import com.powsybl.iidm.xml.NetworkXmlReaderContext;
import com.powsybl.iidm.xml.NetworkXmlWriterContext;

import javax.xml.stream.XMLStreamException;
import java.io.InputStream;
import java.util.List;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(ExtensionXmlSerializer.class)
public class GeneratorStartupXmlSerializer extends AbstractVersionableNetworkExtensionXmlSerializer<Generator, GeneratorStartup> implements ExtensionXmlSerializer<Generator, GeneratorStartup> {

    private static final String ITESLA_1_0 = "1.0-itesla";
    private static final String V_1_0 = "1.0";
    private static final String V_1_1 = "1.1";

    public GeneratorStartupXmlSerializer() {
        super(GeneratorStartup.NAME, GeneratorStartup.class, false, "gs",
                ImmutableMap.<IidmXmlVersion, ImmutableSortedSet<String>>builder()
                        .put(IidmXmlVersion.V_1_0, ImmutableSortedSet.of(ITESLA_1_0, V_1_0, V_1_1))
                        .put(IidmXmlVersion.V_1_1, ImmutableSortedSet.of(ITESLA_1_0, V_1_0, V_1_1))
                        .put(IidmXmlVersion.V_1_2, ImmutableSortedSet.of(ITESLA_1_0, V_1_0, V_1_1))
                        .put(IidmXmlVersion.V_1_3, ImmutableSortedSet.of(ITESLA_1_0, V_1_0, V_1_1))
                        .put(IidmXmlVersion.V_1_4, ImmutableSortedSet.of(ITESLA_1_0, V_1_0, V_1_1))
                        .put(IidmXmlVersion.V_1_5, ImmutableSortedSet.of(ITESLA_1_0, V_1_0, V_1_1))
                        .put(IidmXmlVersion.V_1_6, ImmutableSortedSet.of(ITESLA_1_0, V_1_0, V_1_1))
                        .put(IidmXmlVersion.V_1_7, ImmutableSortedSet.of(ITESLA_1_0, V_1_0, V_1_1))
                        .put(IidmXmlVersion.V_1_8, ImmutableSortedSet.of(ITESLA_1_0, V_1_0, V_1_1))
                        .build(),
                ImmutableMap.<String, String>builder()
                        .put(ITESLA_1_0, "http://www.itesla_project.eu/schema/iidm/ext/generator_startup/1_0")
                        .put(V_1_0, "http://www.powsybl.org/schema/iidm/ext/generator_startup/1_0")
                        .put(V_1_1, "http://www.powsybl.org/schema/iidm/ext/generator_startup/1_1")
                        .build());
    }

    @Override
    public InputStream getXsdAsStream() {
        return getClass().getResourceAsStream("/xsd/generatorStartup_V1_1.xsd");
    }

    @Override
    public List<InputStream> getXsdAsStreamList() {
        return List.of(getClass().getResourceAsStream("/xsd/generatorStartup_itesla_V1_0.xsd"),
                getClass().getResourceAsStream("/xsd/generatorStartup_V1_0.xsd"),
                getClass().getResourceAsStream("/xsd/generatorStartup_V1_1.xsd"));
    }

    @Override
    public void write(GeneratorStartup startup, XmlWriterContext context) throws XMLStreamException {
        NetworkXmlWriterContext networkContext = (NetworkXmlWriterContext) context;
        String extVersionStr = networkContext.getExtensionVersion("startup")
                .orElseGet(() -> getVersion(networkContext.getVersion()));
        String plannedActivePowerSetpoint;
        String startupCost;
        switch (extVersionStr) {
            case ITESLA_1_0:
            case V_1_0:
                plannedActivePowerSetpoint = "predefinedActivePowerSetpoint";
                startupCost = "startUpCost";
                break;
            case V_1_1:
                plannedActivePowerSetpoint = "plannedActivePowerSetpoint";
                startupCost = "startupCost";
                break;
            default:
                throw new PowsyblException("Unsupported startup version: " + extVersionStr);
        }
        context.getWriter().writeDoubleAttribute(plannedActivePowerSetpoint, startup.getPlannedActivePowerSetpoint());
        context.getWriter().writeDoubleAttribute(startupCost, startup.getStartupCost());
        context.getWriter().writeDoubleAttribute("marginalCost", startup.getMarginalCost());
        context.getWriter().writeDoubleAttribute("plannedOutageRate", startup.getPlannedOutageRate());
        context.getWriter().writeDoubleAttribute("forcedOutageRate", startup.getForcedOutageRate());
    }

    @Override
    public GeneratorStartup read(Generator generator, XmlReaderContext context) throws XMLStreamException {
        double plannedActivePowerSetpoint;
        double startUpCost;
        NetworkXmlReaderContext networkXmlReaderContext = (NetworkXmlReaderContext) context;
        String extensionVersionStr = networkXmlReaderContext.getExtensionVersion(this).orElseThrow(AssertionError::new);
        switch (extensionVersionStr) {
            case ITESLA_1_0:
            case V_1_0:
                plannedActivePowerSetpoint = context.getReader().readDoubleAttribute("predefinedActivePowerSetpoint");
                startUpCost = context.getReader().readDoubleAttribute("startUpCost");
                break;
            case V_1_1:
                plannedActivePowerSetpoint = context.getReader().readDoubleAttribute("plannedActivePowerSetpoint");
                startUpCost = context.getReader().readDoubleAttribute("startupCost");
                break;
            default:
                throw new PowsyblException("Unsupported startup version: " + extensionVersionStr);
        }
        double marginalCost = context.getReader().readDoubleAttribute("marginalCost");
        double plannedOutageRate = context.getReader().readDoubleAttribute("plannedOutageRate");
        double forcedOutageRate = context.getReader().readDoubleAttribute("forcedOutageRate");
        return generator.newExtension(GeneratorStartupAdder.class)
                .withPlannedActivePowerSetpoint(plannedActivePowerSetpoint)
                .withStartupCost(startUpCost)
                .withMarginalCost(marginalCost)
                .withPlannedOutageRate(plannedOutageRate)
                .withForcedOutageRate(forcedOutageRate)
                .add();
    }
}
