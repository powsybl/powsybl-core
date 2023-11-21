/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.serializer.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.AbstractExtensionSerializer;
import com.powsybl.commons.extensions.ExtensionSerializer;
import com.powsybl.commons.io.ReaderContext;
import com.powsybl.commons.io.WriterContext;
import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.extensions.BusbarSectionPosition;
import com.powsybl.iidm.network.extensions.BusbarSectionPositionAdder;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
@AutoService(ExtensionSerializer.class)
public class BusbarSectionPositionSerializer extends AbstractExtensionSerializer<BusbarSection, BusbarSectionPosition> {

    public BusbarSectionPositionSerializer() {
        super("busbarSectionPosition", "network", BusbarSectionPosition.class,
                "busbarSectionPosition.xsd", "http://www.itesla_project.eu/schema/iidm/ext/busbarsectionposition/1_0",
                "bbsp");
    }

    @Override
    public void write(BusbarSectionPosition busbarSectionPosition, WriterContext context) {
        context.getWriter().writeIntAttribute("busbarIndex", busbarSectionPosition.getBusbarIndex());
        context.getWriter().writeIntAttribute("sectionIndex", busbarSectionPosition.getSectionIndex());
    }

    @Override
    public BusbarSectionPosition read(BusbarSection busbarSection, ReaderContext context) {
        int busbarIndex = context.getReader().readIntAttribute("busbarIndex");
        int sectionIndex = context.getReader().readIntAttribute("sectionIndex");
        context.getReader().readEndNode();
        return busbarSection.newExtension(BusbarSectionPositionAdder.class)
            .withBusbarIndex(busbarIndex)
            .withSectionIndex(sectionIndex)
            .add();
    }
}
