/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.ValidationException;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
abstract class AbstractBranchAdder<T extends AbstractBranchAdder<T>> extends AbstractIdentifiableAdder<T> {

    private Integer node1;

    private String bus1;

    private Terminal.ConnectionStatus connectionStatus1;

    private String voltageLevelId1;

    private Integer node2;

    private String bus2;

    private Terminal.ConnectionStatus connectionStatus2;

    private String voltageLevelId2;

    public T setNode1(int node1) {
        this.node1 = node1;
        return (T) this;
    }

    public T setBus1(String bus1) {
        this.bus1 = bus1;
        return (T) this;
    }

    public T setConnectionStatus1(Terminal.ConnectionStatus connectionStatus1) {
        this.connectionStatus1 = connectionStatus1;
        return (T) this;
    }

    public T setVoltageLevel1(String voltageLevelId1) {
        this.voltageLevelId1 = voltageLevelId1;
        return (T) this;
    }

    protected TerminalExt checkAndGetTerminal1() {
        return new TerminalBuilder(getNetwork().getRef(), this)
                .setNode(node1)
                .setBus(bus1)
                .setConnectionStatus(connectionStatus1)
                .build();
    }

    protected VoltageLevelExt checkAndGetVoltageLevel1() {
        if (voltageLevelId1 == null) {
            throw new ValidationException(this,  "first voltage level is not set");
        }
        VoltageLevelExt voltageLevel1 = getNetwork().getVoltageLevel(voltageLevelId1);
        if (voltageLevel1 == null) {
            throw new ValidationException(this, "first voltage level '"
                    + voltageLevelId1 + "' not found");
        }
        return voltageLevel1;
    }

    public T setNode2(int node2) {
        this.node2 = node2;
        return (T) this;
    }

    public T setBus2(String bus2) {
        this.bus2 = bus2;
        return (T) this;
    }

    public T setConnectionStatus2(Terminal.ConnectionStatus connectionStatus2) {
        this.connectionStatus2 = connectionStatus2;
        return (T) this;
    }

    public T setVoltageLevel2(String voltageLevelId2) {
        this.voltageLevelId2 = voltageLevelId2;
        return (T) this;
    }

    protected TerminalExt checkAndGetTerminal2() {
        return new TerminalBuilder(getNetwork().getRef(), this)
                .setNode(node2)
                .setBus(bus2)
                .setConnectionStatus(connectionStatus2)
                .build();
    }

    protected VoltageLevelExt checkAndGetVoltageLevel2() {
        if (voltageLevelId2 == null) {
            throw new ValidationException(this, "second voltage level is not set");
        }
        VoltageLevelExt voltageLevel2 = getNetwork().getVoltageLevel(voltageLevelId2);
        if (voltageLevel2 == null) {
            throw new ValidationException(this, "second voltage level '"
                    + voltageLevelId2 + "' not found");
        }
        return voltageLevel2;
    }

}
