/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.BusbarSection;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class BusbarSectionAdapter extends AbstractInjectionAdapter<BusbarSection> implements BusbarSection {

    BusbarSectionAdapter(final BusbarSection delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public double getV() {
        return getDelegate().getV();
    }

    @Override
    public double getAngle() {
        return getDelegate().getAngle();
    }
}
