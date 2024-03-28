/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
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

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
@AutoService(ExtensionSerDe.class)
public class GeneratorEntsoeCategorySerDe extends AbstractExtensionSerDe<Generator, GeneratorEntsoeCategory> {

    public GeneratorEntsoeCategorySerDe() {
        super("entsoeCategory", "network", GeneratorEntsoeCategory.class,
                "generatorEntsoeCategory.xsd", "http://www.itesla_project.eu/schema/iidm/ext/generator_entsoe_category/1_0", "gec");
    }

    @Override
    public void write(GeneratorEntsoeCategory entsoeCategory, SerializerContext context) {
        context.getWriter().writeNodeContent(Integer.toString(entsoeCategory.getCode()));
    }

    @Override
    public GeneratorEntsoeCategory read(Generator generator, DeserializerContext context) {
        int code = Integer.parseInt(context.getReader().readContent());
        return generator.newExtension(GeneratorEntsoeCategoryAdder.class)
                .withCode(code)
                .add();
    }
}
