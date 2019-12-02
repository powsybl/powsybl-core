/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.Terminal.BusBreakerView;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class TerminalBusBreakerViewAdapter extends AbstractAdapter<Terminal.BusBreakerView> implements Terminal.BusBreakerView {

    TerminalBusBreakerViewAdapter(final BusBreakerView delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public Bus getBus() {
        return getIndex().getBus(getDelegate().getBus());
    }

    @Override
    public Bus getConnectableBus() {
        return getIndex().getBus(getDelegate().getConnectableBus());
    }

    @Override
    public void setConnectableBus(final String busId) {
        getDelegate().setConnectableBus(busId);
    }
}
