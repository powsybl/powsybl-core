/**
 * Copyright (c) 2025, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.ref.Ref;
import com.powsybl.iidm.network.*;
import gnu.trove.list.array.TDoubleArrayList;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
abstract class AbstractAcDcConverter<I extends AcDcConverter<I>> extends AbstractConnectable<I> implements AcDcConverter<I>, MultiVariantObject {

    public static final String IDLE_LOSS = "idleLoss";
    public static final String SWITCHING_LOSS = "switchingLoss";
    public static final String RESISTIVE_LOSS = "resistiveLoss";
    public static final String PCC_TERMINAL = "pccTerminal";
    public static final String CONTROL_MODE = "controlMode";
    public static final String TARGET_P = "targetP";
    public static final String TARGET_VDC = "targetVdc";
    protected final List<DcTerminalImpl> dcTerminals = new ArrayList<>();

    private double idleLoss;
    private double switchingLoss;
    private double resistiveLoss;

    private final RegulatingPoint pccRegulatingPoint;

    // attributes depending on the variant

    private final TDoubleArrayList targetP;

    private final TDoubleArrayList targetVdc;

    AbstractAcDcConverter(Ref<NetworkImpl> ref, String id, String name, boolean fictitious,
                          double idleLoss, double switchingLoss, double resistiveLoss,
                          TerminalExt pccTerminal, ControlMode controlMode, double targetP, double targetVdc) {
        super(ref, id, name, fictitious);
        this.idleLoss = idleLoss;
        this.switchingLoss = switchingLoss;
        this.resistiveLoss = resistiveLoss;
        int variantArraySize = ref.get().getVariantManager().getVariantArraySize();
        this.targetP = new TDoubleArrayList(variantArraySize);
        this.targetVdc = new TDoubleArrayList(variantArraySize);
        pccRegulatingPoint = new RegulatingPoint(id, () -> null, variantArraySize, controlMode.ordinal(), ControlMode.V_DC.ordinal(), false);
        pccRegulatingPoint.setRegulatingTerminal(pccTerminal);
        for (int i = 0; i < variantArraySize; i++) {
            this.targetP.add(targetP);
            this.targetVdc.add(targetVdc);
        }
    }

    @Override
    public Terminal getTerminal1() {
        ValidationUtil.checkAccessOfRemovedEquipment(this.id, this.removed, "terminal1");
        return getTerminals().get(0);
    }

    @Override
    public Optional<Terminal> getTerminal2() {
        ValidationUtil.checkAccessOfRemovedEquipment(this.id, this.removed, "terminal2");
        if (terminals.size() > 1) {
            return Optional.of(terminals.get(1));
        }
        return Optional.empty();
    }

    @Override
    public TwoSides getSide(Terminal terminal) {
        Objects.requireNonNull(terminal);
        if (getTerminal1() == terminal) {
            return TwoSides.ONE;
        } else if (getTerminal2().orElse(null) == terminal) {
            return TwoSides.TWO;
        } else {
            throw new IllegalStateException("The terminal is not connected to this AC/DC converter");
        }
    }

    @Override
    public Terminal getTerminal(TwoSides side) {
        Objects.requireNonNull(side);
        ValidationUtil.checkAccessOfRemovedEquipment(this.id, this.removed, "terminal");
        if (side == TwoSides.ONE) {
            return getTerminal1();
        } else if (side == TwoSides.TWO) {
            return getTerminal2().orElseThrow(() -> new IllegalStateException("This AC/DC converter does not have a second AC Terminal"));
        }
        throw new IllegalStateException("Unexpected side: " + side);
    }

    @Override
    public DcTerminal getDcTerminal1() {
        ValidationUtil.checkAccessOfRemovedEquipment(this.id, this.removed, "dcTerminal1");
        return this.dcTerminals.get(0);
    }

    @Override
    public DcTerminal getDcTerminal2() {
        ValidationUtil.checkAccessOfRemovedEquipment(this.id, this.removed, "dcTerminal2");
        return this.dcTerminals.get(1);
    }

    @Override
    public TwoSides getSide(DcTerminal dcTerminal) {
        Objects.requireNonNull(dcTerminal);
        if (getDcTerminal1() == dcTerminal) {
            return TwoSides.ONE;
        } else if (getDcTerminal2() == dcTerminal) {
            return TwoSides.TWO;
        } else {
            throw new IllegalStateException("The DC terminal is not connected to this AC/DC converter");
        }
    }

    @Override
    public DcTerminal getDcTerminal(TwoSides side) {
        Objects.requireNonNull(side);
        ValidationUtil.checkAccessOfRemovedEquipment(this.id, this.removed, "terminal");
        if (side == TwoSides.ONE) {
            return this.dcTerminals.get(0);
        } else if (side == TwoSides.TWO) {
            return this.dcTerminals.get(1);
        }
        throw new IllegalStateException("Unexpected side: " + side);
    }

    @Override
    public List<DcTerminal> getDcTerminals() {
        ValidationUtil.checkAccessOfRemovedEquipment(this.id, this.removed, "dcTerminals");
        return List.copyOf(dcTerminals);
    }

    void addDcTerminal(DcTerminalImpl dcTerminal) {
        this.dcTerminals.add(dcTerminal);
        dcTerminal.setDcConnectable(this);
    }

    @Override
    public double getIdleLoss() {
        ValidationUtil.checkAccessOfRemovedEquipment(this.id, this.removed, IDLE_LOSS);
        return this.idleLoss;
    }

    @Override
    public I setIdleLoss(double idleLoss) {
        ValidationUtil.checkModifyOfRemovedEquipment(this.id, this.removed, IDLE_LOSS);
        ValidationUtil.checkDoubleParamPositive(this, idleLoss, IDLE_LOSS);
        double oldValue = this.idleLoss;
        this.idleLoss = idleLoss;
        getNetwork().getListeners().notifyUpdate(this, IDLE_LOSS, oldValue, idleLoss);
        return self();
    }

    @Override
    public double getSwitchingLoss() {
        ValidationUtil.checkAccessOfRemovedEquipment(this.id, this.removed, SWITCHING_LOSS);
        return this.switchingLoss;
    }

    @Override
    public I setSwitchingLoss(double switchingLoss) {
        ValidationUtil.checkModifyOfRemovedEquipment(this.id, this.removed, SWITCHING_LOSS);
        ValidationUtil.checkDoubleParamPositive(this, switchingLoss, SWITCHING_LOSS);
        double oldValue = this.switchingLoss;
        this.switchingLoss = switchingLoss;
        getNetwork().getListeners().notifyUpdate(this, SWITCHING_LOSS, oldValue, switchingLoss);
        return self();
    }

    @Override
    public double getResistiveLoss() {
        ValidationUtil.checkAccessOfRemovedEquipment(this.id, this.removed, RESISTIVE_LOSS);
        return this.resistiveLoss;
    }

    @Override
    public I setResistiveLoss(double resistiveLoss) {
        ValidationUtil.checkModifyOfRemovedEquipment(this.id, this.removed, RESISTIVE_LOSS);
        ValidationUtil.checkDoubleParamPositive(this, resistiveLoss, RESISTIVE_LOSS);
        double oldValue = this.resistiveLoss;
        this.resistiveLoss = resistiveLoss;
        getNetwork().getListeners().notifyUpdate(this, RESISTIVE_LOSS, oldValue, resistiveLoss);
        return self();
    }

    @Override
    public I setPccTerminal(Terminal pccTerminal) {
        ValidationUtil.checkModifyOfRemovedEquipment(this.id, this.removed, PCC_TERMINAL);
        // todo checks
        Terminal oldValue = pccRegulatingPoint.getRegulatingTerminal();
        pccRegulatingPoint.setRegulatingTerminal((TerminalExt) pccTerminal);
        notifyUpdate(PCC_TERMINAL, oldValue, pccRegulatingPoint.getRegulatingTerminal());
        return self();
    }

    @Override
    public Terminal getPccTerminal() {
        ValidationUtil.checkAccessOfRemovedEquipment(this.id, this.removed, PCC_TERMINAL);
        return pccRegulatingPoint.getRegulatingTerminal();
    }

    @Override
    public I setControlMode(ControlMode controlMode) {
        Objects.requireNonNull(controlMode);
        ValidationUtil.checkModifyOfRemovedEquipment(this.id, this.removed, CONTROL_MODE);
        NetworkImpl n = getNetwork();
        // todo checks
        int variantIndex = n.getVariantIndex();
        int oldValueOrdinal = pccRegulatingPoint.setRegulationMode(variantIndex, controlMode.ordinal());
        String variantId = n.getVariantManager().getVariantId(variantIndex);
        notifyUpdate(CONTROL_MODE, variantId, ControlMode.values()[oldValueOrdinal], controlMode);
        return self();
    }

    @Override
    public ControlMode getControlMode() {
        ValidationUtil.checkAccessOfRemovedEquipment(this.id, this.removed, CONTROL_MODE);
        int variantIndex = getNetwork().getVariantIndex();
        return ControlMode.values()[pccRegulatingPoint.getRegulationMode(variantIndex)];
    }

    @Override
    public I setTargetP(double targetP) {
        ValidationUtil.checkModifyOfRemovedEquipment(this.id, this.removed, TARGET_P);
        // todo: validation
        NetworkImpl n = getNetwork();
        int variantIndex = n.getVariantIndex();
        double oldValue = this.targetP.set(variantIndex, targetP);
        String variantId = n.getVariantManager().getVariantId(variantIndex);
        n.invalidateValidationLevel();
        notifyUpdate(TARGET_P, variantId, oldValue, targetP);
        return self();
    }

    @Override
    public double getTargetP() {
        ValidationUtil.checkAccessOfRemovedEquipment(this.id, this.removed, TARGET_P);
        return targetP.get(getNetwork().getVariantIndex());
    }

    @Override
    public I setTargetVdc(double targetVdc) {
        ValidationUtil.checkModifyOfRemovedEquipment(this.id, this.removed, TARGET_VDC);
        // todo: validation
        NetworkImpl n = getNetwork();
        int variantIndex = n.getVariantIndex();
        double oldValue = this.targetVdc.set(variantIndex, targetVdc);
        String variantId = n.getVariantManager().getVariantId(variantIndex);
        n.invalidateValidationLevel();
        notifyUpdate(TARGET_VDC, variantId, oldValue, targetVdc);
        return self();
    }

    @Override
    public double getTargetVdc() {
        ValidationUtil.checkAccessOfRemovedEquipment(this.id, this.removed, TARGET_VDC);
        return targetVdc.get(getNetwork().getVariantIndex());
    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        super.extendVariantArraySize(initVariantArraySize, number, sourceIndex);
        targetP.ensureCapacity(targetP.size() + number);
        targetVdc.ensureCapacity(targetVdc.size() + number);
        for (int i = 0; i < number; i++) {
            targetP.add(targetP.get(sourceIndex));
            targetVdc.add(targetVdc.get(sourceIndex));
        }

        for (DcTerminalImpl t : dcTerminals) {
            t.extendVariantArraySize(initVariantArraySize, number, sourceIndex);
        }
        pccRegulatingPoint.extendVariantArraySize(initVariantArraySize, number, sourceIndex);
    }

    @Override
    public void reduceVariantArraySize(int number) {
        super.reduceVariantArraySize(number);
        targetP.remove(targetP.size() - number, number);
        targetVdc.remove(targetVdc.size() - number, number);

        for (DcTerminalImpl t : dcTerminals) {
            t.reduceVariantArraySize(number);
        }
        pccRegulatingPoint.reduceVariantArraySize(number);
    }

    @Override
    public void deleteVariantArrayElement(int index) {
        super.deleteVariantArrayElement(index);

        for (DcTerminalImpl t : dcTerminals) {
            t.deleteVariantArrayElement(index);
        }
        pccRegulatingPoint.deleteVariantArrayElement(index);
    }

    @Override
    public void allocateVariantArrayElement(int[] indexes, int sourceIndex) {
        super.allocateVariantArrayElement(indexes, sourceIndex);
        for (int index : indexes) {
            targetP.set(index, targetP.get(sourceIndex));
            targetVdc.set(index, targetVdc.get(sourceIndex));
        }

        for (DcTerminalImpl t : dcTerminals) {
            t.allocateVariantArrayElement(indexes, sourceIndex);
        }
        pccRegulatingPoint.allocateVariantArrayElement(indexes, sourceIndex);
    }

    @Override
    public void remove() {
        dcTerminals.forEach(DcTerminalImpl::remove);
        pccRegulatingPoint.remove();
        super.remove();
    }

    protected abstract I self();
}
