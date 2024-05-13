/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.ThreeSides;
import com.powsybl.iidm.network.ValidationException;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
abstract class AbstractBranchAdder<T extends AbstractBranchAdder<T>> extends AbstractIdentifiableAdder<T> {

    private Integer node1;

    private String bus1;

    private String connectableBus1;

    private String voltageLevelId1;

    private Integer node2;

    private String bus2;

    private String connectableBus2;

    private String voltageLevelId2;

    public T setNode1(int node1) {
        this.node1 = node1;
        return (T) this;
    }

    public T setBus1(String bus1) {
        this.bus1 = bus1;
        return (T) this;
    }

    public T setConnectableBus1(String connectableBus1) {
        this.connectableBus1 = connectableBus1;
        return (T) this;
    }

    public T setVoltageLevel1(String voltageLevelId1) {
        this.voltageLevelId1 = voltageLevelId1;
        return (T) this;
    }

    protected TerminalExt checkAndGetTerminal1() {
        VoltageLevelExt voltageLevel = checkAndGetVoltageLevel1();
        return new TerminalBuilder(voltageLevel.getNetworkRef(), this, ThreeSides.ONE)
                .setNode(node1)
                .setBus(bus1)
                .setConnectableBus(connectableBus1)
                .build();
    }

    protected VoltageLevelExt checkAndGetVoltageLevel1() {
        if (voltageLevelId1 == null) {
            String defaultVoltageLevelId1 = checkAndGetDefaultVoltageLevelId(connectableBus1);
            if (defaultVoltageLevelId1 == null) {
                throw new ValidationException(this, "first voltage level is not set and has no default value");
            } else {
                voltageLevelId1 = defaultVoltageLevelId1;
            }
        }
        VoltageLevelExt voltageLevel1 = getNetwork().getVoltageLevel(voltageLevelId1);
        if (voltageLevel1 == null) {
            throw new ValidationException(this, getNotFoundMessage("first voltage level", voltageLevelId1));
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

    public T setConnectableBus2(String connectableBus2) {
        this.connectableBus2 = connectableBus2;
        return (T) this;
    }

    public T setVoltageLevel2(String voltageLevelId2) {
        this.voltageLevelId2 = voltageLevelId2;
        return (T) this;
    }

    protected TerminalExt checkAndGetTerminal2() {
        VoltageLevelExt voltageLevel = checkAndGetVoltageLevel2();
        return new TerminalBuilder(voltageLevel.getNetworkRef(), this, ThreeSides.TWO)
                .setNode(node2)
                .setBus(bus2)
                .setConnectableBus(connectableBus2)
                .build();
    }

    protected VoltageLevelExt checkAndGetVoltageLevel2() {
        if (voltageLevelId2 == null) {
            String defaultVoltageLevelId2 = checkAndGetDefaultVoltageLevelId(connectableBus2);
            if (defaultVoltageLevelId2 == null) {
                throw new ValidationException(this, "second voltage level is not set and has no default value");
            } else {
                voltageLevelId2 = defaultVoltageLevelId2;
            }
        }
        VoltageLevelExt voltageLevel2 = getNetwork().getVoltageLevel(voltageLevelId2);
        if (voltageLevel2 == null) {
            throw new ValidationException(this, getNotFoundMessage("second voltage level", voltageLevelId2));
        }
        return voltageLevel2;
    }

    private String checkAndGetDefaultVoltageLevelId(String connectableBus) {
        if (connectableBus == null) {
            return null;
        }
        BusExt busExt = (BusExt) getNetwork().getBusBreakerView().getBus(connectableBus);
        if (busExt == null) {
            throw new ValidationException(this, getNotFoundMessage("bus", connectableBus));
        }
        return busExt.getVoltageLevel().getId();
    }

    protected void checkConnectableBuses() {
        if (connectableBus1 == null && bus1 != null) {
            connectableBus1 = bus1;
        }
        if (connectableBus2 == null && bus2 != null) {
            connectableBus2 = bus2;
        }
    }

    private static String getNotFoundMessage(String type, String id) {
        return type + " '" + id + "' not found";
    }
}
