/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dynamicsimulation.dsl;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

import com.powsybl.dynamicsimulation.Curves;
import com.powsybl.iidm.network.Network;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
public class GroovyDslCurvesProvider extends AbstractDslCurvesProvider {

    protected GroovyDslCurvesProvider(final Path path) {
        super(path);
    }

    protected GroovyDslCurvesProvider(final InputStream input) {
        super(input);
    }

    @Override
    public List<Curves> getCurves(Network network) {
        return new CurvesDslLoader(script).load(network);
    }

}
