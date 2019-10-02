/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.VoltageLevelAdder;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
class VoltageLevelAdderAdapter implements VoltageLevelAdder {

    private final VoltageLevelAdder internal;

    private final MergingViewIndex index;

    VoltageLevelAdderAdapter(final VoltageLevelAdder internal, final MergingViewIndex index) {
        this.internal = internal;
        this.index = index;
    }

    @Override
    public VoltageLevelAdapter add() {
        return index.getVoltageLevel(internal.add());
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public VoltageLevelAdderAdapter setId(final String id) {
        this.internal.setId(id);
        return this;
    }

    @Override
    public VoltageLevelAdderAdapter setEnsureIdUnicity(final boolean ensureIdUnicity) {
        this.internal.setEnsureIdUnicity(ensureIdUnicity);
        return this;
    }

    @Override
    public VoltageLevelAdderAdapter setName(final String name) {
        this.internal.setName(name);
        return this;
    }

    @Override
    public VoltageLevelAdderAdapter setNominalV(final double nominalV) {
        this.internal.setNominalV(nominalV);
        return this;
    }

    @Override
    public VoltageLevelAdderAdapter setLowVoltageLimit(final double lowVoltageLimit) {
        this.internal.setLowVoltageLimit(lowVoltageLimit);
        return this;
    }

    @Override
    public VoltageLevelAdderAdapter setHighVoltageLimit(final double highVoltageLimit) {
        this.internal.setHighVoltageLimit(highVoltageLimit);
        return this;
    }

    @Override
    public VoltageLevelAdderAdapter setTopologyKind(final String topologyKind) {
        this.internal.setTopologyKind(topologyKind);
        return this;
    }

    @Override
    public VoltageLevelAdderAdapter setTopologyKind(final TopologyKind topologyKind) {
        this.internal.setTopologyKind(topologyKind);
        return this;
    }
}
