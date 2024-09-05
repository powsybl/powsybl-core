/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
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
import com.powsybl.iidm.network.extensions.GeneratorEntsoeCategory;
import com.powsybl.iidm.network.extensions.GeneratorEntsoeCategoryAdder;
import com.powsybl.iidm.network.extensions.GeneratorTanPhi;
import com.powsybl.iidm.network.extensions.GeneratorTanPhiAdder;

/**
 * @author Sophie Frasnedo {@literal <sophie.frasnedo at rte-france.com>}
 */
@AutoService(ExtensionSerDe.class)
public class GeneratorTanPhiSerDe extends AbstractExtensionSerDe<Generator, GeneratorTanPhi> {

    public GeneratorTanPhiSerDe() {
        super("tanPhi", "network", GeneratorTanPhi.class,
                "generatorTanPhi.xsd", "http://www.itesla_project.eu/schema/iidm/ext/generator_tan_phi/1_0", "gtp");
    }

    @Override
    public void write(GeneratorTanPhi generatorTanPhi, SerializerContext context) {
        context.getWriter().writeDoubleAttribute("tanPhi", generatorTanPhi.getTanPhi());
        context.getWriter().writeBooleanAttribute("tanPhiRegulated", generatorTanPhi.isTanPhiRegulated());
    }

    @Override
    public GeneratorTanPhi read(Generator generator, DeserializerContext context) {
        double tanPhi = context.getReader().readDoubleAttribute("tanPhi");
        boolean tanPhiRegulated = context.getReader().readBooleanAttribute("tanPhiRegulated");
        context.getReader().readEndNode();
        return generator.newExtension(GeneratorTanPhiAdder.class)
                .withTanPhi(tanPhi)
                .withTanPhiRegulated(tanPhiRegulated)
                .add();
    }
}
