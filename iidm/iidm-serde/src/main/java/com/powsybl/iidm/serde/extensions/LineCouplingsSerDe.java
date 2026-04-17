/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.ExtensionSerDe;
import com.powsybl.commons.io.DeserializerContext;
import com.powsybl.commons.io.SerializerContext;
import com.powsybl.commons.io.TreeDataReader;
import com.powsybl.commons.io.TreeDataWriter;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.LineCouplings;
import com.powsybl.iidm.network.extensions.LineCouplingsAdder;
import com.powsybl.iidm.network.extensions.MutualCoupling;
import com.powsybl.iidm.network.extensions.MutualCouplingAdder;
import com.powsybl.iidm.serde.IidmVersion;

import java.util.Map;

/**
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
@AutoService(ExtensionSerDe.class)
public class LineCouplingsSerDe extends AbstractVersionableNetworkExtensionSerDe<Network, LineCouplings, LineCouplingsSerDe.Version> {

    public static final String MUTUAL_COUPLING_ROOT_ELEMENT_NAME = "mutualCoupling";

    public enum Version implements SerDeVersion<Version> {
        V_1_0("/xsd/lineCouplings_V1_0.xsd", "http://www.powsybl.org/schema/iidm/ext/line_couplings/1_0",
                new VersionNumbers(1, 0), IidmVersion.V_1_13, null);

        private final VersionInfo versionInfo;

        Version(String xsdResourcePath, String namespaceUri, VersionNumbers versionNumbers, IidmVersion minIidmVersionIncluded, IidmVersion maxIidmVersionExcluded) {
            this.versionInfo = new VersionInfo(xsdResourcePath, namespaceUri, "lmc", versionNumbers,
                    minIidmVersionIncluded, maxIidmVersionExcluded, LineCouplings.NAME);
        }

        @Override
        public VersionInfo getVersionInfo() {
            return versionInfo;
        }
    }

    public LineCouplingsSerDe() {
        super(LineCouplings.NAME, LineCouplings.class, Version.values());
    }

    @Override
    public Map<String, String> getArrayNameToSingleNameMap() {
        return Map.of(LineCouplings.NAME, MUTUAL_COUPLING_ROOT_ELEMENT_NAME);
    }

    @Override
    public void write(LineCouplings extension, SerializerContext context) {
        TreeDataWriter writer = context.getWriter();
        writer.writeStartNodes();
        for (MutualCoupling mutualCoupling : extension.getMutualCouplings()) {
            writer.writeStartNode(getNamespaceUri(), MUTUAL_COUPLING_ROOT_ELEMENT_NAME);
            writer.writeStringAttribute("line1", mutualCoupling.getLine1().getId());
            writer.writeStringAttribute("line2", mutualCoupling.getLine2().getId());
            writer.writeDoubleAttribute("r", mutualCoupling.getR());
            writer.writeDoubleAttribute("x", mutualCoupling.getX());
            writer.writeDoubleAttribute("line1Start", mutualCoupling.getLine1Start(), 0);
            writer.writeDoubleAttribute("line2Start", mutualCoupling.getLine2Start(), 0);
            writer.writeDoubleAttribute("line1End", mutualCoupling.getLine1End(), 1);
            writer.writeDoubleAttribute("line2End", mutualCoupling.getLine2End(), 1);
            writer.writeEndNode();
        }
        writer.writeEndNodes();
    }

    @Override
    public LineCouplings read(Network network, DeserializerContext context) {
        TreeDataReader reader = context.getReader();
        LineCouplings extension = network.newExtension(LineCouplingsAdder.class).add();
        reader.readChildNodes(elementName -> {
            if (elementName.equals(MUTUAL_COUPLING_ROOT_ELEMENT_NAME)) {
                String line1Id = reader.readStringAttribute("line1");
                String line2Id = reader.readStringAttribute("line2");
                double r = reader.readDoubleAttribute("r");
                double x = reader.readDoubleAttribute("x");
                double line1Start = reader.readDoubleAttribute("line1Start", 0);
                double line2Start = reader.readDoubleAttribute("line2Start", 0);
                double line1End = reader.readDoubleAttribute("line1End", 1);
                double line2End = reader.readDoubleAttribute("line2End", 1);

                Line line1 = network.getLine(line1Id);
                Line line2 = network.getLine(line2Id);

                if (line1 == null) {
                    throw new PowsyblException("Line '" + line1Id + "' not found in network.");
                }
                if (line2 == null) {
                    throw new PowsyblException("Line '" + line2Id + "' not found in network.");
                }

                MutualCouplingAdder adder = extension.newMutualCoupling();
                adder.withLine1(line1)
                    .withLine2(line2)
                    .withR(r)
                    .withX(x)
                    .withLine1Start(line1Start)
                    .withLine2Start(line2Start)
                    .withLine1End(line1End)
                    .withLine2End(line2End)
                    .add();

                reader.readEndNode();
            } else {
                throw new PowsyblException("Unknown element name '" + elementName + "' in 'lineCouplings'");
            }
        });
        return extension;
    }

    @Override
    public boolean isSerializable(LineCouplings extension) {
        return !extension.getMutualCouplings().isEmpty();
    }
}
