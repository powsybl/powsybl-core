/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.extensions.ThreeWindingsTransformerToBeEstimated;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
class ThreeWindingsTransformerToBeEstimatedImpl extends AbstractExtension<ThreeWindingsTransformer> implements ThreeWindingsTransformerToBeEstimated {

    private final Set<TapChanger> tapChangers = new HashSet<>();

    ThreeWindingsTransformerToBeEstimatedImpl(Set<TapChanger> tapChangers) {
        this.tapChangers.addAll(Objects.requireNonNull(tapChangers));
    }

    @Override
    public Set<TapChanger> getTapChangers() {
        return Collections.unmodifiableSet(tapChangers);
    }

    @Override
    public boolean toBeEstimated(TapChanger tapChanger) {
        return tapChangers.contains(tapChanger);
    }

    @Override
    public ThreeWindingsTransformerToBeEstimated addTapChanger(TapChanger tapChanger) {
        tapChangers.add(Objects.requireNonNull(tapChanger));
        return this;
    }

    @Override
    public ThreeWindingsTransformerToBeEstimated removeTapChanger(TapChanger tapChanger) {
        tapChangers.remove(Objects.requireNonNull(tapChanger));
        return this;
    }
}
