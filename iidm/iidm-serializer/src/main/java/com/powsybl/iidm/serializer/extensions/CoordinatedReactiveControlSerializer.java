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
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.extensions.CoordinatedReactiveControl;
import com.powsybl.iidm.network.extensions.CoordinatedReactiveControlAdder;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
@AutoService(ExtensionSerializer.class)
public class CoordinatedReactiveControlSerializer extends AbstractExtensionSerializer<Generator, CoordinatedReactiveControl> {

    public CoordinatedReactiveControlSerializer() {
        super("coordinatedReactiveControl", "network", CoordinatedReactiveControl.class, "coordinatedReactiveControl.xsd",
                "http://www.powsybl.org/schema/iidm/ext/coordinated_reactive_control/1_0", "crc");
    }

    @Override
    public void write(CoordinatedReactiveControl coordinatedReactiveControl, WriterContext context) {
        context.getWriter().writeDoubleAttribute("qPercent", coordinatedReactiveControl.getQPercent());
    }

    @Override
    public CoordinatedReactiveControl read(Generator extendable, ReaderContext context) {
        double qPercent = context.getReader().readDoubleAttribute("qPercent");
        context.getReader().readEndNode();
        return extendable.newExtension(CoordinatedReactiveControlAdder.class)
                .withQPercent(qPercent)
                .add();
    }
}
