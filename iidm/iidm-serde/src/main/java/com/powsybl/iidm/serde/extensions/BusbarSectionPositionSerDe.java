/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.AbstractExtensionSerDe;
import com.powsybl.commons.extensions.ExtensionSerDe;
import com.powsybl.commons.io.DeserializerContext;
import com.powsybl.commons.io.SerializerContext;
import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.extensions.BusbarSectionPosition;
import com.powsybl.iidm.network.extensions.BusbarSectionPositionAdder;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
@AutoService(ExtensionSerDe.class)
public class BusbarSectionPositionSerDe extends AbstractExtensionSerDe<BusbarSection, BusbarSectionPosition> {

    public BusbarSectionPositionSerDe() {
        super("busbarSectionPosition", "network", BusbarSectionPosition.class,
                "busbarSectionPosition.xsd", "http://www.itesla_project.eu/schema/iidm/ext/busbarsectionposition/1_0",
                "bbsp");
    }

    @Override
    public void write(BusbarSectionPosition busbarSectionPosition, SerializerContext context) {
        context.getWriter().writeIntAttribute("busbarIndex", busbarSectionPosition.getBusbarIndex());
        context.getWriter().writeIntAttribute("sectionIndex", busbarSectionPosition.getSectionIndex());
    }

    @Override
    public BusbarSectionPosition read(BusbarSection busbarSection, DeserializerContext context) {
        int busbarIndex = context.getReader().readIntAttribute("busbarIndex");
        int sectionIndex = context.getReader().readIntAttribute("sectionIndex");
        context.getReader().readEndNode();
        return busbarSection.newExtension(BusbarSectionPositionAdder.class)
            .withBusbarIndex(busbarIndex)
            .withSectionIndex(sectionIndex)
            .add();
    }
}
