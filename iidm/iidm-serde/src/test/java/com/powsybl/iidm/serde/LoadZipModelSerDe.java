/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
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
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.test.LoadZipModel;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
@AutoService(ExtensionSerDe.class)
public class LoadZipModelSerDe extends AbstractExtensionSerDe<Load, LoadZipModel> {

    public LoadZipModelSerDe() {
        super("loadZipModel", "network", LoadZipModel.class, "loadZipModel.xsd",
                "http://www.itesla_project.eu/schema/iidm/ext/loadzipmodel/1_0", "extZip");
    }

    @Override
    public void write(LoadZipModel zipModel, SerializerContext context) {
        context.getWriter().writeDoubleAttribute("a1", zipModel.getA1());
        context.getWriter().writeDoubleAttribute("a2", zipModel.getA2());
        context.getWriter().writeDoubleAttribute("a3", zipModel.getA3());
        context.getWriter().writeDoubleAttribute("a4", zipModel.getA4());
        context.getWriter().writeDoubleAttribute("a5", zipModel.getA5());
        context.getWriter().writeDoubleAttribute("a6", zipModel.getA6());
        context.getWriter().writeDoubleAttribute("v0", zipModel.getV0());
    }

    @Override
    public LoadZipModel read(Load load, DeserializerContext context) {
        double a1 = context.getReader().readDoubleAttribute("a1");
        double a2 = context.getReader().readDoubleAttribute("a2");
        double a3 = context.getReader().readDoubleAttribute("a3");
        double a4 = context.getReader().readDoubleAttribute("a4");
        double a5 = context.getReader().readDoubleAttribute("a5");
        double a6 = context.getReader().readDoubleAttribute("a6");
        double v0 = context.getReader().readDoubleAttribute("v0");
        context.getReader().readEndNode();
        return new LoadZipModel(load, a1, a2, a3, a4, a5, a6, v0);
    }
}
