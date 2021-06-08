/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.extensions.Discrete;
import com.powsybl.iidm.network.extensions.DiscreteAdder;
import com.powsybl.iidm.network.extensions.Discretes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
class DiscretesImpl<I extends Identifiable<I>> extends AbstractExtension<I> implements Discretes<I> {

    private final List<Discrete> discretes = new ArrayList<>();

    DiscretesImpl<I> addDiscrete(DiscreteImpl discrete) {
        discretes.add(discrete);
        return this;
    }

    @Override
    public Collection<Discrete> getDiscretes() {
        return Collections.unmodifiableList(discretes);
    }

    @Override
    public Discrete getDiscrete(String id) {
        return discretes.stream()
                .filter(d -> d.getId() != null && d.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public DiscreteAdder newDiscrete() {
        return new DiscreteAdderImpl(this);
    }
}
