/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.util;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;

import java.util.List;
import java.util.Objects;

class ScalableAdapter extends AbstractScalable {

    private final String id;

    public ScalableAdapter(String id) {
        this.id = Objects.requireNonNull(id);
    }

    private Scalable getScalable(Network n) {
        Objects.requireNonNull(n);
        Identifiable identifiable = n.getIdentifiable(id);
        if (identifiable instanceof Generator) {
            return new GeneratorScalable(id);
        } else {
            throw new PowsyblException("Unable to create a scalable from " + identifiable.getClass());
        }
    }

    @Override
    public double initialValue(Network n) {
        return getScalable(n).initialValue(n);
    }

    @Override
    public void reset(Network n) {
        getScalable(n).reset(n);
    }

    @Override
    public double maximumValue(Network n) {
        return getScalable(n).maximumValue(n);
    }

    @Override
    public void listGenerators(Network n, List<Generator> generators, List<String> notFoundGenerators) {
        getScalable(n).listGenerators(n, generators, notFoundGenerators);
    }

    @Override
    public double scale(Network n, double asked) {
        return getScalable(n).scale(n, asked);
    }
}
