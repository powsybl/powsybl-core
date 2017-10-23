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
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Objects;

class IdentifierScalable extends AbstractScalable {

    private final String id;

    private Scalable scalable;
    private String networkId;

    public IdentifierScalable(String id) {
        this.id = Objects.requireNonNull(id);
    }

    private void initScalable(Network n) {
        Objects.requireNonNull(n);

        // re-init if network changed
        if (scalable == null || !StringUtils.equals(n.getId(), networkId)) {
            Identifiable identifiable = n.getIdentifiable(id);
            if (identifiable instanceof Generator) {
                scalable = new GeneratorScalable(id);
                networkId = n.getId();
            } else {
                throw new PowsyblException("Unable to create a scalable from " + identifiable.getClass());
            }
        }
    }

    @Override
    public float initialValue(Network n) {
        initScalable(n);
        return scalable.initialValue(n);
    }

    @Override
    public void reset(Network n) {
        initScalable(n);
        scalable.reset(n);
    }

    @Override
    public float maximumValue(Network n) {
        initScalable(n);
        return scalable.maximumValue(n);
    }

    @Override
    public void listGenerators(Network n, List<Generator> generators, List<String> notFoundGenerators) {
        initScalable(n);
        scalable.listGenerators(n, generators, notFoundGenerators);
    }

    @Override
    public float scale(Network n, float asked) {
        initScalable(n);
        return scalable.scale(n, asked);
    }
}
