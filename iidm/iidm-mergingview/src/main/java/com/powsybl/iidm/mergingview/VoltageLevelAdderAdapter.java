/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import java.util.Objects;

import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.VoltageLevelAdder;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
class VoltageLevelAdderAdapter implements VoltageLevelAdder {

    private final VoltageLevelAdder delegate;

    private final MergingViewIndex index;

    VoltageLevelAdderAdapter(final VoltageLevelAdder delegate, final MergingViewIndex index) {
        this.delegate = Objects.requireNonNull(delegate, "delegate is null");
        this.index = Objects.requireNonNull(index, "merging view index is null");
    }

    @Override
    public VoltageLevelAdapter add() {
        return index.getVoltageLevel(delegate.add());
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public VoltageLevelAdderAdapter setId(final String id) {
        delegate.setId(id);
        return this;
    }

    @Override
    public VoltageLevelAdderAdapter setEnsureIdUnicity(final boolean ensureIdUnicity) {
        delegate.setEnsureIdUnicity(ensureIdUnicity);
        return this;
    }

    @Override
    public VoltageLevelAdderAdapter setName(final String name) {
        delegate.setName(name);
        return this;
    }

    @Override
    public VoltageLevelAdderAdapter setNominalV(final double nominalV) {
        delegate.setNominalV(nominalV);
        return this;
    }

    @Override
    public VoltageLevelAdderAdapter setLowVoltageLimit(final double lowVoltageLimit) {
        delegate.setLowVoltageLimit(lowVoltageLimit);
        return this;
    }

    @Override
    public VoltageLevelAdderAdapter setHighVoltageLimit(final double highVoltageLimit) {
        delegate.setHighVoltageLimit(highVoltageLimit);
        return this;
    }

    @Override
    public VoltageLevelAdderAdapter setTopologyKind(final String topologyKind) {
        delegate.setTopologyKind(topologyKind);
        return this;
    }

    @Override
    public VoltageLevelAdderAdapter setTopologyKind(final TopologyKind topologyKind) {
        delegate.setTopologyKind(topologyKind);
        return this;
    }
}
