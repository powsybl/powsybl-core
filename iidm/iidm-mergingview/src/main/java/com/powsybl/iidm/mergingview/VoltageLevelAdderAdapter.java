/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.VoltageLevelAdder;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
class VoltageLevelAdderAdapter extends AbstractIdentifiableAdderAdapter<VoltageLevelAdder> implements VoltageLevelAdder {

    VoltageLevelAdderAdapter(final VoltageLevelAdder delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    @Override
    public VoltageLevel add() {
        checkAndSetUniqueId();
        return getIndex().getVoltageLevel(getDelegate().add());
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public VoltageLevelAdder setNominalV(final double nominalV) {
        getDelegate().setNominalV(nominalV);
        return this;
    }

    @Override
    public VoltageLevelAdder setLowVoltageLimit(final double lowVoltageLimit) {
        getDelegate().setLowVoltageLimit(lowVoltageLimit);
        return this;
    }

    @Override
    public VoltageLevelAdder setHighVoltageLimit(final double highVoltageLimit) {
        getDelegate().setHighVoltageLimit(highVoltageLimit);
        return this;
    }

    @Override
    public VoltageLevelAdder setTopologyKind(final String topologyKind) {
        getDelegate().setTopologyKind(topologyKind);
        return this;
    }

    @Override
    public VoltageLevelAdder setTopologyKind(final TopologyKind topologyKind) {
        getDelegate().setTopologyKind(topologyKind);
        return this;
    }
}
