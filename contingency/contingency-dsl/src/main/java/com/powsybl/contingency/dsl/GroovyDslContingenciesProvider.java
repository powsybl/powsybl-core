/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency.dsl;

import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.network.Network;
import org.codehaus.groovy.control.customizers.ImportCustomizer;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class GroovyDslContingenciesProvider extends AbstractDslContingenciesProvider {

    public GroovyDslContingenciesProvider(final Path path) {
        super(path);
    }

    public GroovyDslContingenciesProvider(final InputStream input) {
        super(input);
    }

    @Override
    public List<Contingency> getContingencies(Network network, ImportCustomizer imports) {
        return new ContingencyDslLoader(script).load(network, imports);
    }
}
