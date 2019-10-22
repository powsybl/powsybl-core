/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.Terminal.BusView;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class TerminalBusViewAdapter extends AbstractAdapter<Terminal.BusView> implements Terminal.BusView {

    protected TerminalBusViewAdapter(final BusView delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    // -------------------------------
    // Not implemented methods -------
    // -------------------------------
    @Override
    public BusAdapter getBus() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public BusAdapter getConnectableBus() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

}
