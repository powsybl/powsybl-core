/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.AbstractExtensionSerDe;
import com.powsybl.commons.extensions.ExtensionSerDe;
import com.powsybl.commons.io.DeserializerContext;
import com.powsybl.commons.io.SerializerContext;
import com.powsybl.iidm.network.TieLine;
import com.powsybl.iidm.serde.IidmVersion;
import com.powsybl.iidm.serde.NetworkDeserializerContext;
import com.powsybl.iidm.serde.NetworkSerializerContext;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
@AutoService(ExtensionSerDe.class)
public class CgmesLineBoundaryNodeSerDe extends AbstractExtensionSerDe<TieLine, CgmesLineBoundaryNode> {

    public CgmesLineBoundaryNodeSerDe() {
        super("cgmesLineBoundaryNode", "network", CgmesLineBoundaryNode.class,
                "cgmesLineBoundaryNode.xsd",
                "http://www.powsybl.org/schema/iidm/ext/cgmes_line_boundary_node/1_0", "clbn");
    }

    @Override
    public void write(CgmesLineBoundaryNode extension, SerializerContext context) {
        NetworkSerializerContext networkContext = (NetworkSerializerContext) context;
        context.getWriter().writeBooleanAttribute("isHvdc", extension.isHvdc());
        String lineEnergyIdentificationCodeEic = extension.getLineEnergyIdentificationCodeEic().orElse(null);
        String lineEnergyIdentificationCodeEicToWrite = networkContext.anonymizeFromMinimumVersion(lineEnergyIdentificationCodeEic, IidmVersion.V_1_16);
        networkContext.getWriter().writeStringAttribute("lineEnergyIdentificationCodeEic", lineEnergyIdentificationCodeEicToWrite);
    }

    @Override
    public CgmesLineBoundaryNode read(TieLine extendable, DeserializerContext context) {
        NetworkDeserializerContext networkContext = (NetworkDeserializerContext) context;
        boolean isHvdc = context.getReader().readBooleanAttribute("isHvdc");
        String lineEnergyIdentificationCodeEic = networkContext.getReader().readStringAttribute("lineEnergyIdentificationCodeEic");
        String lineEnergyIdentificationCodeEicToRead = networkContext.deanonymizeFromMinimumVersion(lineEnergyIdentificationCodeEic, IidmVersion.V_1_16);
        networkContext.getReader().readEndNode();
        extendable.newExtension(CgmesLineBoundaryNodeAdder.class).setHvdc(isHvdc).setLineEnergyIdentificationCodeEic(lineEnergyIdentificationCodeEicToRead).add();
        return extendable.getExtension(CgmesLineBoundaryNode.class);
    }
}
