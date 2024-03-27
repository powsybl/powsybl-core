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
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.extensions.CoordinatedReactiveControl;
import com.powsybl.iidm.network.extensions.CoordinatedReactiveControlAdder;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
@AutoService(ExtensionSerDe.class)
public class CoordinatedReactiveControlSerDe extends AbstractExtensionSerDe<Generator, CoordinatedReactiveControl> {

    public CoordinatedReactiveControlSerDe() {
        super("coordinatedReactiveControl", "network", CoordinatedReactiveControl.class, "coordinatedReactiveControl.xsd",
                "http://www.powsybl.org/schema/iidm/ext/coordinated_reactive_control/1_0", "crc");
    }

    @Override
    public void write(CoordinatedReactiveControl coordinatedReactiveControl, SerializerContext context) {
        context.getWriter().writeDoubleAttribute("qPercent", coordinatedReactiveControl.getQPercent());
    }

    @Override
    public CoordinatedReactiveControl read(Generator extendable, DeserializerContext context) {
        double qPercent = context.getReader().readDoubleAttribute("qPercent");
        context.getReader().readEndNode();
        return extendable.newExtension(CoordinatedReactiveControlAdder.class)
                .withQPercent(qPercent)
                .add();
    }
}
