/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.BusbarSectionAdder;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class BusbarSectionAdderAdapter extends AbstractIdentifiableAdderAdapter<BusbarSectionAdder> implements BusbarSectionAdder {

    BusbarSectionAdderAdapter(final BusbarSectionAdder delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    @Override
    public BusbarSection add() {
        checkAndSetUniqueId();
        return getIndex().getBusbarSection(getDelegate().add());
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public BusbarSectionAdder setNode(int node) {
        getDelegate().setNode(node);
        return this;
    }
}
