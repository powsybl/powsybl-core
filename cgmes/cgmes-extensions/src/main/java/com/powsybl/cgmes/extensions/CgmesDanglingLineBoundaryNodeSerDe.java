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
import com.powsybl.iidm.network.BoundaryLine;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */

// TODO: Manage versioning to chang DanglingLine to BoundaryLine

@AutoService(ExtensionSerDe.class)
public class CgmesDanglingLineBoundaryNodeSerDe extends AbstractExtensionSerDe<BoundaryLine, CgmesBoundaryLineBoundaryNode> {

    public CgmesDanglingLineBoundaryNodeSerDe() {
        super("cgmesDanglingLineBoundaryNode", "network", CgmesBoundaryLineBoundaryNode.class,
                "cgmesDanglingLineBoundaryNode.xsd",
                "http://www.powsybl.org/schema/iidm/ext/cgmes_dangling_line_boundary_node/1_0", "cdlbn");
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
