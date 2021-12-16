/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.ExtensionAdder;
import com.powsybl.iidm.network.Generator;

/**
 * @author Jérémy Labous <jlabous at silicom.fr>
 */
public interface GeneratorEntsoeCategoryAdder extends ExtensionAdder<Generator, GeneratorEntsoeCategory> {

    @Override
    default Class<GeneratorEntsoeCategory> getExtensionClass() {
        return GeneratorEntsoeCategory.class;
    }

    GeneratorEntsoeCategoryAdder withCode(int code);
}
