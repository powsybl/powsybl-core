/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.AbstractExtensionXmlSerializer;
import com.powsybl.commons.extensions.ExtensionXmlSerializer;
import com.powsybl.commons.extensions.XmlReaderContext;
import com.powsybl.commons.extensions.XmlWriterContext;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.extensions.CoordinatedReactiveControl;
import com.powsybl.iidm.network.extensions.CoordinatedReactiveControlAdder;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
@AutoService(ExtensionXmlSerializer.class)
public class CoordinatedReactiveControlXmlSerializer extends AbstractExtensionXmlSerializer<Generator, CoordinatedReactiveControl> {

    public CoordinatedReactiveControlXmlSerializer() {
        super("coordinatedReactiveControl", "network", CoordinatedReactiveControl.class, false, "coordinatedReactiveControl.xsd",
                "http://www.powsybl.org/schema/iidm/ext/coordinated_reactive_control/1_0", "crc");
    }

    @Override
    public void write(CoordinatedReactiveControl coordinatedReactiveControl, XmlWriterContext context) {
        context.getWriter().writeDoubleAttribute("qPercent", coordinatedReactiveControl.getQPercent());
    }

    @Override
    public CoordinatedReactiveControl read(Generator extendable, XmlReaderContext context) {
        double qPercent = context.getReader().readDoubleAttribute("qPercent");
        return extendable.newExtension(CoordinatedReactiveControlAdder.class)
                .withQPercent(qPercent)
                .add();
    }
}
