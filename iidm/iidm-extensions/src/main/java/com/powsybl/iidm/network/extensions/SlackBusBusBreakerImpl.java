/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.VoltageLevel;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class SlackBusBusBreakerImpl extends AbstractExtension<VoltageLevel> implements SlackBus {

    private final VoltageLevel voltageLevel;
    private final String busId;

    private final BusBreakerViewImpl busBreakerView;
    private final BusViewImpl busView;

    private final class BusBreakerViewImpl implements BusBreakerView {
        @Override
        public Bus getBus() {
            return voltageLevel.getBusBreakerView().getBus(busId);
        }
    }

    private final class BusViewImpl implements BusView {
        @Override
        public Bus getBus() {
            return voltageLevel.getBusView().getMergedBus(busId);
        }
    }

    SlackBusBusBreakerImpl(String busId, VoltageLevel voltageLevel) {
        super(voltageLevel);
        this.voltageLevel = voltageLevel;
        this.busId = busId;
        this.busBreakerView = new BusBreakerViewImpl();
        this.busView = new BusViewImpl();
    }

    @Override
    public NodeBreakerView getNodeBreakerView() {
        throw new PowsyblException("Not supported: cannot access to the node breaker view from a slackBus defined from the bus breaker view");
    }

    @Override
    public BusBreakerView getBusBreakerView() {
        return busBreakerView;
    }

    @Override
    public BusView getBusView() {
        return busView;
    }

}
