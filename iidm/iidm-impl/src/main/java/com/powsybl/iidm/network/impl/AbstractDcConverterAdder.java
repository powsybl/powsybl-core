/**
 * Copyright (c) 2025, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;

import java.util.Objects;
import java.util.Optional;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
abstract class AbstractDcConverterAdder<T extends AbstractDcConverterAdder<T>> extends AbstractIdentifiableAdder<T> {

    protected String dcNode1Id;
    protected boolean dcConnected1 = true;
    protected String dcNode2Id;
    protected boolean dcConnected2 = true;

    private Integer node1;

    private String bus1;

    private String connectableBus1;

    protected VoltageLevelExt voltageLevel;

    private Integer node2;

    private String bus2;

    private String connectableBus2;

    protected double idleLoss = 0;
    protected double switchingLoss = 0;
    protected double resistiveLoss = 0;

    AbstractDcConverterAdder(VoltageLevelExt voltageLevel) {
        this.voltageLevel = voltageLevel;
    }

    protected NetworkImpl getNetwork() {
        return voltageLevel.getNetwork();
    }

    public T setDcNode1Id(String dcNode1Id) {
        this.dcNode1Id = Objects.requireNonNull(dcNode1Id);
        return (T) this;
    }

    public T setDcConnected1(boolean dcConnected1) {
        this.dcConnected1 = dcConnected1;
        return (T) this;
    }

    public T setDcNode2Id(String dcNode2Id) {
        this.dcNode2Id = Objects.requireNonNull(dcNode2Id);
        return (T) this;
    }

    public T setDcConnected2(boolean dcConnected2) {
        this.dcConnected2 = dcConnected2;
        return (T) this;
    }

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

    protected TerminalExt checkAndGetTerminal1() {
        return new TerminalBuilder(voltageLevel.getNetworkRef(), this, ThreeSides.ONE)
                .setNode(node1)
                .setBus(bus1)
                .setConnectableBus(connectableBus1)
                .build();
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

    protected Optional<TerminalExt> checkAndGetTerminal2() {
        if (bus2 != null || node2 != null) {
            return Optional.of(new TerminalBuilder(voltageLevel.getNetworkRef(), this, ThreeSides.TWO)
                    .setNode(node2)
                    .setBus(bus2)
                    .setConnectableBus(connectableBus2)
                    .build());
        }
        return Optional.empty();
    }

    protected String checkAndGetDcNode1() {
        return dcNode1Id;
    }

    protected String checkAndGetDcNode2() {
        return dcNode2Id;
    }

    public T setIdleLoss(double idleLoss) {
        this.idleLoss = idleLoss;
        return (T) this;
    }

    public T setSwitchingLoss(double switchingLoss) {
        this.switchingLoss = switchingLoss;
        return (T) this;
    }

    public T setResistiveLoss(double resistiveLoss) {
        this.resistiveLoss = resistiveLoss;
        return (T) this;
    }

    protected void checkAndAdd(AbstractDcConverter<?> dcConverter) {
        TerminalExt terminal1 = checkAndGetTerminal1();
        Optional<TerminalExt> terminal2 = checkAndGetTerminal2();
        dcConverter.addTerminal(terminal1);
        voltageLevel.getTopologyModel().attach(terminal1, false);
        terminal2.ifPresent(terminal -> {
            dcConverter.addTerminal(terminal);
            voltageLevel.getTopologyModel().attach(terminal, false);
        });
        DcNode dcNode1 = getNetwork().getDcNode(checkAndGetDcNode1());
        DcNode dcNode2 = getNetwork().getDcNode(checkAndGetDcNode2());
        DcTerminalImpl dcTerminal1 = new DcTerminalImpl(voltageLevel.getNetworkRef(), dcNode1, dcConnected1);
        DcTerminalImpl dcTerminal2 = new DcTerminalImpl(voltageLevel.getNetworkRef(), dcNode2, dcConnected2);
        dcConverter.addDcTerminal(dcTerminal1);
        dcConverter.addDcTerminal(dcTerminal2);
        getNetwork().getIndex().checkAndAdd(dcConverter);
        getNetwork().getListeners().notifyCreation(dcConverter);
    }
}
