/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency.json;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.powsybl.contingency.Contingency;
import com.powsybl.contingency.ContingencyElement;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ContingencyJsonModule extends SimpleModule {

    public ContingencyJsonModule() {
        addSerializer(ContingencyElement.class, new ContingencyElementSerializer());

        addDeserializer(ContingencyElement.class, new ContingencyElementDeserializer());
        addDeserializer(Contingency.class, new ContingencyDeserializer());
    }
}
