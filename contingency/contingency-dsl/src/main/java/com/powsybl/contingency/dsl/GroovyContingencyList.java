/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.contingency.dsl;

import com.powsybl.contingency.Contingency;
import com.powsybl.contingency.ContingencyList;
import com.powsybl.dsl.GroovyScripts;
import com.powsybl.iidm.network.Network;
import groovy.lang.GroovyCodeSource;

import java.io.InputStream;
import java.util.List;
import java.util.Objects;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public class GroovyContingencyList implements ContingencyList {

    private final String name;

    private final GroovyCodeSource codeSource;

    GroovyContingencyList(String name, InputStream stream) {
        this.name = Objects.requireNonNull(name);
        this.codeSource = GroovyScripts.load(stream);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<Contingency> getContingencies(Network network) {
        return new ContingencyDslLoader(codeSource).load(network);
    }
}
