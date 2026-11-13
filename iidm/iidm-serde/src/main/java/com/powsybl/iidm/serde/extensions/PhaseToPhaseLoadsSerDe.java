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
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.extensions.Phase;
import com.powsybl.iidm.network.extensions.PhaseToPhaseLoad;
import com.powsybl.iidm.network.extensions.PhaseToPhaseLoads;
import com.powsybl.iidm.network.extensions.PhaseToPhaseLoadsAdder;
import com.powsybl.iidm.serde.IidmVersion;

import java.util.Map;

/**
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
@AutoService(ExtensionSerDe.class)
public class PhaseToPhaseLoadsSerDe extends AbstractVersionableNetworkExtensionSerDe<Load, PhaseToPhaseLoads, PhaseToPhaseLoadsSerDe.Version> {

    public static final String PHASE_TO_PHASE_LOAD_ROOT_ELEMENT_NAME = "phaseToPhaseLoad";

    public enum Version implements SerDeVersion<Version> {
        V_1_0("/xsd/phaseToPhaseLoads_V1_0.xsd", "http://www.powsybl.org/schema/iidm/ext/phase_to_phase_loads/1_0",
                new VersionNumbers(1, 0), IidmVersion.V_1_19, null);

        private final VersionInfo versionInfo;

        Version(String xsdResourcePath, String namespaceUri, VersionNumbers versionNumbers, IidmVersion minIidmVersionIncluded, IidmVersion maxIidmVersionExcluded) {
            this.versionInfo = new VersionInfo(xsdResourcePath, namespaceUri, "ppl", versionNumbers,
                    minIidmVersionIncluded, maxIidmVersionExcluded, PhaseToPhaseLoads.NAME);
        }

        @Override
        public VersionInfo getVersionInfo() {
            return versionInfo;
        }
    }

    public PhaseToPhaseLoadsSerDe() {
        super(PhaseToPhaseLoads.NAME, PhaseToPhaseLoads.class, Version.values());
    }

    @Override
    public Map<String, String> getArrayNameToSingleNameMap() {
        return Map.of(PhaseToPhaseLoads.NAME, PHASE_TO_PHASE_LOAD_ROOT_ELEMENT_NAME);
    }

    @Override
    public void write(PhaseToPhaseLoads extension, SerializerContext context) {
        TreeDataWriter writer = context.getWriter();
        writer.writeStartNodes();
        for (PhaseToPhaseLoad phaseToPhaseLoad : extension.getPhaseToPhaseLoads()) {
            writer.writeStartNode(getNamespaceUri(), PHASE_TO_PHASE_LOAD_ROOT_ELEMENT_NAME);
            writer.writeEnumAttribute("phase1", phaseToPhaseLoad.getPhase1());
            writer.writeEnumAttribute("phase2", phaseToPhaseLoad.getPhase2());
            writer.writeDoubleAttribute("p", phaseToPhaseLoad.getP());
            writer.writeDoubleAttribute("q", phaseToPhaseLoad.getQ());
            writer.writeEndNode();
        }
        writer.writeEndNodes();
    }

    @Override
    public PhaseToPhaseLoads read(Load load, DeserializerContext context) {
        TreeDataReader reader = context.getReader();
        PhaseToPhaseLoads extension = load.newExtension(PhaseToPhaseLoadsAdder.class).add();
        reader.readChildNodes(elementName -> readPhaseToPhaseLoad(elementName, reader, extension));
        return extension;
    }

    private static void readPhaseToPhaseLoad(String elementName, TreeDataReader reader, PhaseToPhaseLoads extension) {
        if (elementName.equals(PHASE_TO_PHASE_LOAD_ROOT_ELEMENT_NAME)) {
            Phase phase1 = reader.readEnumAttribute("phase1", Phase.class);
            Phase phase2 = reader.readEnumAttribute("phase2", Phase.class);
            double p = reader.readDoubleAttribute("p");
            double q = reader.readDoubleAttribute("q");
            extension.newPhaseToPhaseLoad()
                    .withPhase1(phase1)
                    .withPhase2(phase2)
                    .withP(p)
                    .withQ(q)
                    .add();
            reader.readEndNode();
        } else {
            throw new PowsyblException("Unknown element name '" + elementName + "' in '" + PhaseToPhaseLoads.NAME + "'");
        }
    }

    @Override
    public boolean isSerializable(PhaseToPhaseLoads extension) {
        return !extension.getPhaseToPhaseLoads().isEmpty();
    }
}
