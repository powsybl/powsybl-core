/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.contingency.dsl;

import com.google.auto.service.AutoService;
import com.powsybl.contingency.contingency.list.ContingencyList;
import com.powsybl.contingency.ContingencyListLoader;

import java.io.InputStream;

/**
 * @author Mathieu Bague {@literal <mathieu.bague@rte-france.com>}
 */
@AutoService(ContingencyListLoader.class)
public class GroovyContingencyListLoader implements ContingencyListLoader {

    @Override
    public String getFormat() {
        return "groovy";
    }

    @Override
    public ContingencyList load(String name, InputStream stream) {
        return new GroovyContingencyList(name, stream);
    }
}
