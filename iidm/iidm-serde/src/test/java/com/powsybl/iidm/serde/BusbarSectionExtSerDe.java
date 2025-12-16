/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.AbstractExtensionSerDe;
import com.powsybl.commons.extensions.ExtensionSerDe;
import com.powsybl.commons.io.DeserializerContext;
import com.powsybl.commons.io.SerializerContext;
import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.test.BusbarSectionExt;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
@AutoService(ExtensionSerDe.class)
public class BusbarSectionExtSerDe extends AbstractExtensionSerDe<BusbarSection, BusbarSectionExt> {

    public BusbarSectionExtSerDe() {
        super("busbarSectionExt", "network", BusbarSectionExt.class, "busbarSectionExt.xsd",
                "http://www.itesla_project.eu/schema/iidm/ext/busbarSectionExt/1_0", "bbse");
    }

    @Override
    public void write(BusbarSectionExt busbarSectionExt, SerializerContext context) {
        // this method is abstract
    }

    @Override
    public BusbarSectionExt read(BusbarSection busbarSection, DeserializerContext context) {
        context.getReader().readEndNode();
        var bbsExt = new BusbarSectionExt(busbarSection);
        busbarSection.addExtension(BusbarSectionExt.class, bbsExt);
        return bbsExt;
    }
}
