/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.VoltageLevel;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public interface SlackBus extends Extension<VoltageLevel> {

    @Override
    default String getName() {
        return "slackBus";
    }

    interface NodeBreakerView {
        int getNode();
    }

    interface BusBreakerView {
        Bus getBus();
    }

    interface BusView {
        Bus getBus();
    }

    /**
     * Shortcut to getBusView().getBus()
     * @return the corresponding bus in the bus view
     */
    default Bus get() {
        return getBusView().getBus();
    }

    NodeBreakerView getNodeBreakerView();

    BusBreakerView getBusBreakerView();

    BusView getBusView();
}
