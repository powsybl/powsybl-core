/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.extensions.TwoWindingsTransformerToBeEstimated;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
class TwoWindingsTransformerToBeEstimatedImpl extends AbstractExtension<TwoWindingsTransformer> implements TwoWindingsTransformerToBeEstimated {

    private final Set<TapChanger> tapChangers = new HashSet<>();

    TwoWindingsTransformerToBeEstimatedImpl(Set<TapChanger> tapChangers) {
        this.tapChangers.addAll(Objects.requireNonNull(tapChangers));
    }

    @Override
    public Set<TapChanger> getTapChangers() {
        return Collections.unmodifiableSet(tapChangers);
    }

    @Override
    public boolean tobeEstimated(TapChanger tapChanger) {
        return tapChangers.contains(tapChanger);
    }

    @Override
    public TwoWindingsTransformerToBeEstimated addTapChanger(TapChanger tapChanger) {
        tapChangers.add(Objects.requireNonNull(tapChanger));
        return this;
    }

    @Override
    public TwoWindingsTransformerToBeEstimated removeTapChanger(TapChanger tapChanger) {
        tapChangers.add(Objects.requireNonNull(tapChanger));
        return this;
    }
}
