/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.AbstractExtensionSerializer;
import com.powsybl.commons.extensions.ExtensionSerializer;
import com.powsybl.commons.io.ReaderContext;
import com.powsybl.commons.io.WriterContext;
import com.powsybl.iidm.network.TieLine;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
@AutoService(ExtensionSerializer.class)
public class CgmesLineBoundaryNodeSerializer extends AbstractExtensionSerializer<TieLine, CgmesLineBoundaryNode> {

    public CgmesLineBoundaryNodeSerializer() {
        super("cgmesLineBoundaryNode", "network", CgmesLineBoundaryNode.class,
                "cgmesLineBoundaryNode.xsd",
                "http://www.powsybl.org/schema/iidm/ext/cgmes_line_boundary_node/1_0", "clbn");
    }

    @Override
    public void write(CgmesLineBoundaryNode extension, WriterContext context) {
        context.getWriter().writeBooleanAttribute("isHvdc", extension.isHvdc());
        extension.getLineEnergyIdentificationCodeEic().ifPresent(lineEnergyIdentificationCodeEic ->
                context.getWriter().writeStringAttribute("lineEnergyIdentificationCodeEic", lineEnergyIdentificationCodeEic));
    }

    @Override
    public CgmesLineBoundaryNode read(TieLine extendable, ReaderContext context) {
        boolean isHvdc = context.getReader().readBooleanAttribute("isHvdc");
        String lineEnergyIdentificationCodeEic = context.getReader().readStringAttribute("lineEnergyIdentificationCodeEic");
        context.getReader().readEndNode();
        extendable.newExtension(CgmesLineBoundaryNodeAdder.class).setHvdc(isHvdc).setLineEnergyIdentificationCodeEic(lineEnergyIdentificationCodeEic).add();
        return extendable.getExtension(CgmesLineBoundaryNode.class);
    }
}
