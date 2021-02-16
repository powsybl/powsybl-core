/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml.extensions;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.ExtensionXmlSerializer;
import com.powsybl.commons.xml.XmlReaderContext;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.commons.xml.XmlWriterContext;
import com.powsybl.iidm.export.ExportOptions;
import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.extensions.BoundaryFlowLimits;
import com.powsybl.iidm.network.extensions.BoundaryFlowLimitsAdder;
import com.powsybl.iidm.xml.AbstractConnectableXml;
import com.powsybl.iidm.xml.IidmXmlVersion;
import com.powsybl.iidm.xml.NetworkXmlWriterContext;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.InputStream;
import java.util.List;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
@AutoService(ExtensionXmlSerializer.class)
public class BoundaryFlowLimitsXmlSerializer extends AbstractVersionableNetworkExtensionXmlSerializer<DanglingLine, BoundaryFlowLimits> {

    public BoundaryFlowLimitsXmlSerializer() {
        super("boundaryFlowLimits", BoundaryFlowLimits.class, true, "bfl",
                ImmutableMap.of(IidmXmlVersion.V_1_5, ImmutableSortedSet.of("1.0")), ImmutableMap.of("1.0", "http://www.powsybl.org/schema/iidm/ext/boundary_flow_limits/1_0"));
    }

    @Override
    public InputStream getXsdAsStream() {
        return getClass().getResourceAsStream("/xsd/boundaryFlowLimits_V1_5.xsd");
    }

    @Override
    public List<InputStream> getXsdAsStreamList() {
        return ImmutableList.of(getClass().getResourceAsStream("/xsd/boundaryFlowLimits_V1_5.xsd"));
    }

    @Override
    public void write(BoundaryFlowLimits extension, XmlWriterContext context) throws XMLStreamException {
        NetworkXmlWriterContext networkContext = (NetworkXmlWriterContext) context;
        XMLStreamWriter writer = context.getWriter();
        IidmXmlVersion version = networkContext.getVersion();
        String extensionVersion = networkContext.getExtensionVersion(getExtensionName()).orElseGet(() -> getVersion(version));
        String namespaceUri = getNamespaceUri(extensionVersion);
        ExportOptions options = networkContext.getOptions();
        if (extension.getActivePowerLimits() != null) {
            AbstractConnectableXml.writeActivePowerLimits(null, extension.getActivePowerLimits(), writer, namespaceUri, version, options);
        }
        if (extension.getApparentPowerLimits() != null) {
            AbstractConnectableXml.writeApparentPowerLimits(null, extension.getApparentPowerLimits(), writer, namespaceUri, version, options);
        }
        if (extension.getCurrentLimits() != null) {
            AbstractConnectableXml.writeCurrentLimits(null, extension.getCurrentLimits(), writer, namespaceUri, version, options);
        }
    }

    @Override
    public BoundaryFlowLimits read(DanglingLine extendable, XmlReaderContext context) throws XMLStreamException {
        extendable.newExtension(BoundaryFlowLimitsAdder.class).add();
        BoundaryFlowLimits boundaryFlowLimits = extendable.getExtension(BoundaryFlowLimits.class);
        XmlUtil.readUntilEndElement(getExtensionName(), context.getReader(), () -> {
            switch (context.getReader().getLocalName()) {
                case AbstractConnectableXml.ACTIVE_POWER_LIMITS:
                    AbstractConnectableXml.readActivePowerLimits(null, boundaryFlowLimits::newActivePowerLimits, context.getReader());
                    break;
                case AbstractConnectableXml.APPARENT_POWER_LIMITS:
                    AbstractConnectableXml.readApparentPowerLimits(null, boundaryFlowLimits::newApparentPowerLimits, context.getReader());
                    break;
                case AbstractConnectableXml.CURRENT_LIMITS:
                    AbstractConnectableXml.readCurrentLimits(null, boundaryFlowLimits::newCurrentLimits, context.getReader());
                    break;
                default:
                    throw new PowsyblException("Unknown element name <" + context.getReader().getLocalName() + "> in <" + extendable.getId() + ".boundaryFlowLimits>");
            }
        });
        return boundaryFlowLimits;
    }

    @Override
    public boolean isSerializable(BoundaryFlowLimits extension) {
        return !extension.isEmpty();
    }
}
