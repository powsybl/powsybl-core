/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.ExtensionSerDe;
import com.powsybl.commons.io.DeserializerContext;
import com.powsybl.commons.io.SerializerContext;
import com.powsybl.iidm.network.BoundaryLine;
import com.powsybl.iidm.serde.IidmVersion;
import com.powsybl.iidm.serde.extensions.AbstractVersionableNetworkExtensionSerDe;
import com.powsybl.iidm.serde.extensions.SerDeVersion;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
@AutoService(ExtensionSerDe.class)
public class CgmesBoundaryLineBoundaryNodeSerDe extends AbstractVersionableNetworkExtensionSerDe<BoundaryLine, CgmesBoundaryLineBoundaryNode, CgmesBoundaryLineBoundaryNodeSerDe.Version> {

    public enum Version implements SerDeVersion<CgmesBoundaryLineBoundaryNodeSerDe.Version> {
        V_1_0("/xsd/cgmesDanglingLineBoundaryNode.xsd", "http://www.powsybl.org/schema/iidm/ext/cgmes_dangling_line_boundary_node/1_0",
                new VersionNumbers(1, 0), IidmVersion.V_1_0, IidmVersion.V_1_16, "cgmesDanglingLineBoundaryNode"),
        V_1_1("/xsd/cgmesBoundaryLineBoundaryNode.xsd", "http://www.powsybl.org/schema/iidm/ext/cgmes_boundary_line_boundary_node/1_0",
                new VersionNumbers(1, 1), IidmVersion.V_1_16, null);

        private final VersionInfo versionInfo;

        Version(String xsdResourcePath, String namespaceUri, VersionNumbers versionNumbers, IidmVersion minIidmVersionIncluded, IidmVersion maxIidmVersionExcluded) {
            this.versionInfo = new VersionInfo(xsdResourcePath, namespaceUri, "cdlbn", versionNumbers,
                    minIidmVersionIncluded, maxIidmVersionExcluded, CgmesBoundaryLineBoundaryNode.NAME);
        }

        Version(String xsdResourcePath, String namespaceUri, VersionNumbers versionNumbers, IidmVersion minIidmVersionIncluded, IidmVersion maxIidmVersionExcluded, String serializationName) {
            this.versionInfo = new VersionInfo(xsdResourcePath, namespaceUri, "cdlbn", versionNumbers,
                    minIidmVersionIncluded, maxIidmVersionExcluded, serializationName);
        }

        @Override
        public VersionInfo getVersionInfo() {
            return versionInfo;
        }
    }

    public CgmesBoundaryLineBoundaryNodeSerDe() {
        super(CgmesBoundaryLineBoundaryNode.NAME, CgmesBoundaryLineBoundaryNode.class, Version.values());
    }

    @Override
    public Version getVersion(IidmVersion networkVersion) {
        if (networkVersion.compareTo(IidmVersion.V_1_16) < 0) {
            return Version.V_1_0;
        } else {
            return Version.V_1_1;
        }
    }

    @Override
    public void write(CgmesBoundaryLineBoundaryNode extension, SerializerContext context) {
        context.getWriter().writeBooleanAttribute("isHvdc", extension.isHvdc());
        context.getWriter().writeStringAttribute("lineEnergyIdentificationCodeEic", extension.getLineEnergyIdentificationCodeEic().orElse(null));
    }

    @Override
    public CgmesBoundaryLineBoundaryNode read(BoundaryLine extendable, DeserializerContext context) {
        boolean isHvdc = context.getReader().readBooleanAttribute("isHvdc");
        String lineEnergyIdentificationCodeEic = context.getReader().readStringAttribute("lineEnergyIdentificationCodeEic");
        context.getReader().readEndNode();
        extendable.newExtension(CgmesBoundaryLineBoundaryNodeAdder.class).setHvdc(isHvdc).setLineEnergyIdentificationCodeEic(lineEnergyIdentificationCodeEic).add();
        return extendable.getExtension(CgmesBoundaryLineBoundaryNode.class);
    }
}
