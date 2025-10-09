/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.extensions.GeneratorEntsoeCategory;
import com.powsybl.iidm.network.extensions.GeneratorEntsoeCategoryAdder;

/**
 * @author Jérémy Labous {@literal <jlabous at silicom.fr>}
 */
public class GeneratorEntsoeCategoryAdderImpl extends AbstractExtensionAdder<Generator, GeneratorEntsoeCategory>
        implements GeneratorEntsoeCategoryAdder {

    private int code;

    public GeneratorEntsoeCategoryAdderImpl(Generator generator) {
        super(generator);
    }

    @Override
    protected GeneratorEntsoeCategory createExtension(Generator extendable) {
        return new GeneratorEntsoeCategoryImpl(extendable, code);
    }

    @Override
    public GeneratorEntsoeCategoryAdder withCode(int code) {
        this.code = code;
        return this;
    }
}
