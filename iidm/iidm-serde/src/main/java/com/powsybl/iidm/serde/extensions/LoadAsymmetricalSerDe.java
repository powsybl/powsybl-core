/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
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
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.extensions.LoadAsymmetrical;
import com.powsybl.iidm.network.extensions.LoadAsymmetricalAdder;
import com.powsybl.iidm.network.extensions.LoadConnectionType;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
@AutoService(ExtensionSerDe.class)
public class LoadAsymmetricalSerDe extends AbstractExtensionSerDe<Load, LoadAsymmetrical> {

    public LoadAsymmetricalSerDe() {
        super("loadAsymmetrical", "network", LoadAsymmetrical.class,
                "loadAsymmetrical_V1_0.xsd", "http://www.powsybl.org/schema/iidm/ext/load_asymmetrical/1_0",
                "las");
    }

    @Override
    public void write(LoadAsymmetrical loadAsym, SerializerContext context) {
        context.getWriter().writeEnumAttribute("connectionType", loadAsym.getConnectionType());
        context.getWriter().writeDoubleAttribute("deltaPa", loadAsym.getDeltaPa(), 0);
        context.getWriter().writeDoubleAttribute("deltaQa", loadAsym.getDeltaQa(), 0);
        context.getWriter().writeDoubleAttribute("deltaPb", loadAsym.getDeltaPb(), 0);
        context.getWriter().writeDoubleAttribute("deltaQb", loadAsym.getDeltaQb(), 0);
        context.getWriter().writeDoubleAttribute("deltaPc", loadAsym.getDeltaPc(), 0);
        context.getWriter().writeDoubleAttribute("deltaQc", loadAsym.getDeltaQc(), 0);
    }

    @Override
    public LoadAsymmetrical read(Load load, DeserializerContext context) {
        LoadConnectionType connectionType = context.getReader().readEnumAttribute("connectionType", LoadConnectionType.class);
        double deltaPa = context.getReader().readDoubleAttribute("deltaPa", 0);
        double deltaQa = context.getReader().readDoubleAttribute("deltaQa", 0);
        double deltaPb = context.getReader().readDoubleAttribute("deltaPb", 0);
        double deltaQb = context.getReader().readDoubleAttribute("deltaQb", 0);
        double deltaPc = context.getReader().readDoubleAttribute("deltaPc", 0);
        double deltaQc = context.getReader().readDoubleAttribute("deltaQc", 0);
        context.getReader().readEndNode();
        return load.newExtension(LoadAsymmetricalAdder.class)
                .withConnectionType(connectionType)
                .withDeltaPa(deltaPa)
                .withDeltaQa(deltaQa)
                .withDeltaPb(deltaPb)
                .withDeltaQb(deltaQb)
                .withDeltaPc(deltaPc)
                .withDeltaQc(deltaQc)
                .add();
    }
}
