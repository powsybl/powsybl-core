/**
 * Copyright (c) 2025, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.util.DcUtils;

import java.util.Objects;
import java.util.Optional;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
abstract class AbstractAcDcConverterAdder<T extends AbstractAcDcConverterAdder<T>> extends AbstractIdentifiableAdder<T> {

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

    protected double idleLoss = 0.;
    protected double switchingLoss = 0.;
    protected double resistiveLoss = 0.;

    protected TerminalExt pccTerminal;
    protected AcDcConverter.ControlMode controlMode;
    protected double targetP = Double.NaN;
    protected double targetVdc = Double.NaN;

    AbstractAcDcConverterAdder(VoltageLevelExt voltageLevel) {
        this.voltageLevel = voltageLevel;
    }

    protected NetworkImpl getNetwork() {
        return voltageLevel.getNetwork();
    }

    public T setDcNode1(String dcNode1) {
        this.dcNode1Id = Objects.requireNonNull(dcNode1);
        return self();
    }

    public T setDcConnected1(boolean dcConnected1) {
        this.dcConnected1 = dcConnected1;
        return self();
    }

    public T setDcNode2(String dcNode2) {
        this.dcNode2Id = Objects.requireNonNull(dcNode2);
        return self();
    }

    public T setDcConnected2(boolean dcConnected2) {
        this.dcConnected2 = dcConnected2;
        return self();
    }

    public T setNode1(int node1) {
        this.node1 = node1;
        return self();
    }

    public T setBus1(String bus1) {
        this.bus1 = bus1;
        return self();
    }

    public T setConnectableBus1(String connectableBus1) {
        this.connectableBus1 = connectableBus1;
        return self();
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
        return self();
    }

    public T setBus2(String bus2) {
        this.bus2 = bus2;
        return self();
    }

    public T setConnectableBus2(String connectableBus2) {
        this.connectableBus2 = connectableBus2;
        return self();
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

    public T setIdleLoss(double idleLoss) {
        this.idleLoss = idleLoss;
        return self();
    }

    public T setSwitchingLoss(double switchingLoss) {
        this.switchingLoss = switchingLoss;
        return self();
    }

    public T setResistiveLoss(double resistiveLoss) {
        this.resistiveLoss = resistiveLoss;
        return self();
    }

    public T setPccTerminal(Terminal pccTerminal) {
        this.pccTerminal = (TerminalExt) pccTerminal;
        return self();
    }

    public T setControlMode(AcDcConverter.ControlMode controlMode) {
        this.controlMode = controlMode;
        return self();
    }

    public T setTargetP(double targetP) {
        this.targetP = targetP;
        return self();
    }

    public T setTargetVdc(double targetVdc) {
        this.targetVdc = targetVdc;
        return self();
    }

    protected void preCheck() {
        boolean twoAcTerminals = bus2 != null || node2 != null;
        if (controlMode == null) {
            throw new ValidationException(this, "controlMode is not set");
        }
        if (Double.isNaN(targetP)) {
            throw new ValidationException(this, "targetP is not set");
        }
        if (Double.isNaN(targetVdc)) {
            throw new ValidationException(this, "targetVdc is not set");
        }
        if (twoAcTerminals && pccTerminal == null) {
            throw new ValidationException(this, "converter has two AC terminals and pccTerminal is not set");
        }
        if (pccTerminal != null) {
            var c = pccTerminal.getConnectable();
            if (twoAcTerminals && !(c instanceof Branch<?> || c instanceof ThreeWindingsTransformer)) {
                throw new ValidationException(this, "converter has two AC terminals and pccTerminal is not a line or transformer terminal");
            } else if (!twoAcTerminals && !(c instanceof Branch<?> || c instanceof ThreeWindingsTransformer || c instanceof AcDcConverter<?>)) {
                throw new ValidationException(this, "pccTerminal is not a line or transformer or the converter terminal");
            }
            if (c.getParentNetwork() != voltageLevel.getParentNetwork()) {
                throw new ValidationException(this, "pccTerminal is not in the same parent network as the voltage level");
            }
        }
    }

    protected void checkAndAdd(AbstractAcDcConverter<?> dcConverter) {
        TerminalExt terminal1 = checkAndGetTerminal1();
        DcNode dcNode1 = DcUtils.checkAndGetDcNode(getNetwork().getParentNetwork(), this, dcNode1Id, "dcNode1Id");
        DcNode dcNode2 = DcUtils.checkAndGetDcNode(getNetwork().getParentNetwork(), this, dcNode2Id, "dcNode2Id");
        DcUtils.checkSameParentNetwork(voltageLevel.getParentNetwork(), this, dcNode1, dcNode2);
        Optional<TerminalExt> terminal2 = checkAndGetTerminal2();
        if (pccTerminal == null && terminal2.isEmpty()) {
            // default to use terminal1 as pccTerminal, only if converter has only 1 AC Terminal
            dcConverter.setPccTerminal(terminal1);
        }
        dcConverter.addTerminal(terminal1);
        voltageLevel.getTopologyModel().attach(terminal1, false);
        terminal2.ifPresent(terminal -> {
            dcConverter.addTerminal(terminal);
            voltageLevel.getTopologyModel().attach(terminal, false);
        });
        DcTerminalImpl dcTerminal1 = new DcTerminalImpl(voltageLevel.getNetworkRef(), TwoSides.ONE, dcNode1, dcConnected1);
        DcTerminalImpl dcTerminal2 = new DcTerminalImpl(voltageLevel.getNetworkRef(), TwoSides.TWO, dcNode2, dcConnected2);
        dcConverter.addDcTerminal(dcTerminal1);
        dcConverter.addDcTerminal(dcTerminal2);
        getNetwork().getIndex().checkAndAdd(dcConverter);
        getNetwork().getListeners().notifyCreation(dcConverter);
    }

    protected abstract T self();
}
