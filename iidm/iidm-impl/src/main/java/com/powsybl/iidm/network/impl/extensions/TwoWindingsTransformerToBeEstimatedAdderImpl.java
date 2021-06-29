/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.extensions.TwoWindingsTransformerToBeEstimated;
import com.powsybl.iidm.network.extensions.TwoWindingsTransformerToBeEstimatedAdder;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
class TwoWindingsTransformerToBeEstimatedAdderImpl extends AbstractExtensionAdder<TwoWindingsTransformer, TwoWindingsTransformerToBeEstimated>
        implements TwoWindingsTransformerToBeEstimatedAdder {

    private final Set<TwoWindingsTransformerToBeEstimated.TapChanger> tapChangers = new HashSet<>();

    protected TwoWindingsTransformerToBeEstimatedAdderImpl(TwoWindingsTransformer extendable) {
        super(extendable);
    }

    @Override
    protected TwoWindingsTransformerToBeEstimated createExtension(TwoWindingsTransformer extendable) {
        if (tapChangers.contains(null)) {
            throw new PowsyblException("A null element has been passed to be estimated");
        }
        return new TwoWindingsTransformerToBeEstimatedImpl(tapChangers);
    }

    @Override
    public TwoWindingsTransformerToBeEstimatedAdder withTapChanger(TwoWindingsTransformerToBeEstimated.TapChanger tapChanger) {
        tapChangers.add(tapChanger);
        return this;
    }
}
