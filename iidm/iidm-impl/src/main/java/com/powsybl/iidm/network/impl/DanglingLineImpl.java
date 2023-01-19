/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.Boundary;
import com.powsybl.iidm.network.impl.util.Ref;
import com.powsybl.iidm.network.util.DanglingLineBoundaryImpl;

import java.util.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class DanglingLineImpl extends AbstractConnectable<DanglingLine> implements DanglingLine {

    private final DanglingLineCharacteristics characteristics;

    private TieLineImpl parent = null;
    private OperationalLimitsHolderImpl operationalLimitsHolder;

    DanglingLineImpl(Ref<NetworkImpl> network, String id, String name, boolean fictitious, double p0, double q0, double r, double x, double g, double b, String ucteXnodeCode, DanglingLineCharacteristics.GenerationImpl generation) {
        super(network, id, name, fictitious);
        operationalLimitsHolder = new OperationalLimitsHolderImpl(this, "limits");
        characteristics = new DanglingLineCharacteristics(this, new DanglingLineBoundaryImpl(this), p0, q0, r, x, g, b, ucteXnodeCode, generation);
    }

    void setParent(TieLineImpl parent, Branch.Side side) {
        this.parent = parent;
        if (side == Branch.Side.ONE) {
            this.operationalLimitsHolder = parent.operationalLimitsHolder1;
        } else if (side == Branch.Side.TWO) {
            this.operationalLimitsHolder = parent.operationalLimitsHolder2;
        }
        addTerminal((TerminalExt) parent.getTerminal(side), false);
    }

    @Override
    public TerminalExt getTerminal() {
        return terminals.get(0);
    }

    @Override
    public Optional<TieLine> getTieLine() {
        return Optional.ofNullable(parent);
    }

    @Override
    public void remove() {
        if (parent != null) {
            throw new UnsupportedOperationException("Parent tie line " + parent.getId() + " should be removed, not the child dangling line");
        }
        super.remove();
    }

    @Override
    protected String getTypeDescription() {
        return parent != null ? "Tie line's dangling line" : "Dangling line";
    }

    @Override
    public boolean isMerged() {
        return parent != null;
    }

    @Override
    public double getP0() {
        return characteristics.getP0();
    }

    @Override
    public DanglingLineImpl setP0(double p0) {
        characteristics.setP0(p0, true);
        return this;
    }

    @Override
    public double getQ0() {
        return characteristics.getQ0();
    }

    @Override
    public DanglingLineImpl setQ0(double q0) {
        characteristics.setQ0(q0, true);
        return this;
    }

    @Override
    public double getR() {
        return characteristics.getR();
    }

    @Override
    public DanglingLineImpl setR(double r) {
        characteristics.setR(r);
        return this;
    }

    @Override
    public double getX() {
        return characteristics.getX();
    }

    @Override
    public DanglingLineImpl setX(double x) {
        characteristics.setX(x);
        return this;
    }

    @Override
    public double getG() {
        return characteristics.getG();
    }

    @Override
    public DanglingLineImpl setG(double g) {
        characteristics.setG(g);
        return this;
    }

    @Override
    public double getB() {
        return characteristics.getB();
    }

    @Override
    public DanglingLineImpl setB(double b) {
        characteristics.setB(b);
        return this;
    }

    @Override
    public String getUcteXnodeCode() {
        return characteristics.getUcteXnodeCode();
    }

    @Override
    public Generation getGeneration() {
        return characteristics.getGeneration();
    }

    @Override
    public Collection<OperationalLimits> getOperationalLimits() {
        return operationalLimitsHolder.getOperationalLimits();
    }

    @Override
    public Optional<CurrentLimits> getCurrentLimits() {
        return operationalLimitsHolder.getOperationalLimits(LimitType.CURRENT, CurrentLimits.class);
    }

    @Override
    public CurrentLimits getNullableCurrentLimits() {
        return operationalLimitsHolder.getNullableOperationalLimits(LimitType.CURRENT, CurrentLimits.class);
    }

    @Override
    public Optional<ActivePowerLimits> getActivePowerLimits() {
        return operationalLimitsHolder.getOperationalLimits(LimitType.ACTIVE_POWER, ActivePowerLimits.class);
    }

    @Override
    public ActivePowerLimits getNullableActivePowerLimits() {
        return operationalLimitsHolder.getNullableOperationalLimits(LimitType.ACTIVE_POWER, ActivePowerLimits.class);
    }

    @Override
    public Optional<ApparentPowerLimits> getApparentPowerLimits() {
        return operationalLimitsHolder.getOperationalLimits(LimitType.APPARENT_POWER, ApparentPowerLimits.class);
    }

    @Override
    public ApparentPowerLimits getNullableApparentPowerLimits() {
        return operationalLimitsHolder.getNullableOperationalLimits(LimitType.APPARENT_POWER, ApparentPowerLimits.class);
    }

    @Override
    public CurrentLimitsAdder newCurrentLimits() {
        return operationalLimitsHolder.newCurrentLimits();
    }

    @Override
    public ActivePowerLimitsAdder newActivePowerLimits() {
        return operationalLimitsHolder.newActivePowerLimits();
    }

    @Override
    public ApparentPowerLimitsAdder newApparentPowerLimits() {
        return operationalLimitsHolder.newApparentPowerLimits();
    }

    @Override
    public Boundary getBoundary() {
        return characteristics.getBoundary();
    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        super.extendVariantArraySize(initVariantArraySize, number, sourceIndex);
        characteristics.extendVariantArraySize(number, sourceIndex);
    }

    @Override
    public void reduceVariantArraySize(int number) {
        super.reduceVariantArraySize(number);
        characteristics.reduceVariantArraySize(number);
    }

    @Override
    public void deleteVariantArrayElement(int index) {
        super.deleteVariantArrayElement(index);
        characteristics.deleteVariantArrayElement(index);
    }

    @Override
    public void allocateVariantArrayElement(int[] indexes, int sourceIndex) {
        super.allocateVariantArrayElement(indexes, sourceIndex);
        characteristics.allocateVariantArrayElement(indexes, sourceIndex);
    }
}
