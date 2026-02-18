/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
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
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.extensions.LineMutualCoupling;
import com.powsybl.iidm.network.extensions.LineMutualCouplingAdder;

/**
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
@AutoService(ExtensionSerDe.class)
public class LineMutualCouplingSerDe extends AbstractExtensionSerDe<Line, LineMutualCoupling> {

    public LineMutualCouplingSerDe() {
        super("lineMutualCoupling", "network", LineMutualCoupling.class,
                "lineMutualCoupling_V1_0.xsd", "http://www.powsybl.org/schema/iidm/ext/line_mutual_coupling/1_0",
                "lmc");
    }

    @Override
    public void write(LineMutualCoupling lineMutualCoupling, SerializerContext context) {
        context.getWriter().writeDoubleAttribute("r", lineMutualCoupling.getR(), Double.NaN);
        context.getWriter().writeDoubleAttribute("x", lineMutualCoupling.getX(), Double.NaN);
    }

    @Override
    public LineMutualCoupling read(Line line, DeserializerContext context) {
        double r = context.getReader().readDoubleAttribute("r");
        double x = context.getReader().readDoubleAttribute("x");
        context.getReader().readEndNode();
        return line.newExtension(LineMutualCouplingAdder.class)
                .withR(r)
                .withX(x)
                .add();
    }

}
