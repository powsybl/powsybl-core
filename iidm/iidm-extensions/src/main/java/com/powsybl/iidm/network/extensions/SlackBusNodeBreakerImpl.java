/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.VoltageLevel;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class SlackBusNodeBreakerImpl extends AbstractExtension<VoltageLevel> implements SlackBus {

    private final Terminal terminal;
    private final NodeBreakerViewImpl nodeBreakerView;
    private final BusBreakerViewImpl busBreakerView;
    private final BusViewImpl busView;

    private final class NodeBreakerViewImpl implements SlackBus.NodeBreakerView {
        @Override
        public int getNode() {
            return terminal.getNodeBreakerView().getNode();
        }
    }

    private final class BusBreakerViewImpl implements SlackBus.BusBreakerView {
        @Override
        public Bus getBus() {
            return terminal.getBusBreakerView().getBus();
        }
    }

    private final class BusViewImpl implements SlackBus.BusView {
        @Override
        public Bus getBus() {
            return terminal.getBusView().getBus();
        }
    }

    SlackBusNodeBreakerImpl(Terminal terminal, VoltageLevel vl) {
        super(vl);
        this.terminal = terminal;
        this.busBreakerView = new BusBreakerViewImpl();
        this.nodeBreakerView = new NodeBreakerViewImpl();
        this.busView = new BusViewImpl();
    }

    @Override
    public NodeBreakerView getNodeBreakerView() {
        return nodeBreakerView;
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
