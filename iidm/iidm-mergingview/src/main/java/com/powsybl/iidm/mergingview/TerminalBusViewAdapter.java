/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.Terminal.BusView;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class TerminalBusViewAdapter extends AbstractAdapter<Terminal.BusView> implements Terminal.BusView {

    TerminalBusViewAdapter(final BusView delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    // -------------------------------
    // Not implemented methods -------
    // -------------------------------
    @Override
    public Bus getBus() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Bus getConnectableBus() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

}
