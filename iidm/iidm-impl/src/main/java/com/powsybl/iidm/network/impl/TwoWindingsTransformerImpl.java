/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.ConnectableType;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.TwoWindingsTransformer;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class TwoWindingsTransformerImpl extends AbstractBranch<TwoWindingsTransformer> implements TwoWindingsTransformer, RatioTapChangerParent {

    private final SubstationImpl substation;

    private double r;

    private double x;

    private double g;

    private double b;

    private double ratedU1;

    private double ratedU2;

    private RatioTapChangerImpl ratioTapChanger;

    private PhaseTapChangerImpl phaseTapChanger;

    TwoWindingsTransformerImpl(String id, String name,
            SubstationImpl substation,
            double r, double x, double g, double b, double ratedU1, double ratedU2) {
        super(id, name);
        this.substation = substation;
        this.r = r;
        this.x = x;
        this.g = g;
        this.b = b;
        this.ratedU1 = ratedU1;
        this.ratedU2 = ratedU2;
    }

    @Override
    public ConnectableType getType() {
        return ConnectableType.TWO_WINDINGS_TRANSFORMER;
    }

    @Override
    public SubstationImpl getSubstation() {
        return substation;
    }

    @Override
    public double getR() {
        return r;
    }

    @Override
    public TwoWindingsTransformerImpl setR(double r) {
        ValidationUtil.checkR(this, r);
        double oldValue = this.r;
        this.r = r;
        notifyUpdate("r", oldValue, r);
        return this;
    }

    @Override
    public double getX() {
        return x;
    }

    @Override
    public TwoWindingsTransformerImpl setX(double x) {
        ValidationUtil.checkX(this, x);
        double oldValue = this.x;
        this.x = x;
        notifyUpdate("x", oldValue, x);
        return this;
    }

    @Override
    public double getG() {
        return g;
    }

    @Override
    public TwoWindingsTransformerImpl setG(double g) {
        ValidationUtil.checkG(this, g);
        double oldValue = this.g;
        this.g = g;
        notifyUpdate("g", oldValue, g);
        return this;
    }

    @Override
    public double getB() {
        return b;
    }

    @Override
    public TwoWindingsTransformerImpl setB(double b) {
        ValidationUtil.checkB(this, b);
        double oldValue = this.b;
        this.b = b;
        notifyUpdate("b", oldValue, b);
        return this;
    }

    @Override
    public double getRatedU1() {
        return ratedU1;
    }

    @Override
    public TwoWindingsTransformerImpl setRatedU1(double ratedU1) {
        ValidationUtil.checkRatedU1(this, ratedU1);
        double oldValue = this.ratedU1;
        this.ratedU1 = ratedU1;
        notifyUpdate("ratedU1", oldValue, ratedU1);
        return this;
    }

    @Override
    public double getRatedU2() {
        return ratedU2;
    }

    @Override
    public TwoWindingsTransformerImpl setRatedU2(double ratedU2) {
        ValidationUtil.checkRatedU2(this, ratedU2);
        double oldValue = this.ratedU2;
        this.ratedU2 = ratedU2;
        notifyUpdate("ratedU2", oldValue, ratedU2);
        return this;
    }

    @Override
    public RatioTapChangerAdderImpl newRatioTapChanger() {
        return new RatioTapChangerAdderImpl(this);
    }

    @Override
    public RatioTapChangerImpl getRatioTapChanger() {
        return ratioTapChanger;
    }

    @Override
    public PhaseTapChangerAdderImpl newPhaseTapChanger() {
        return new PhaseTapChangerAdderImpl(this);
    }

    @Override
    public PhaseTapChangerImpl getPhaseTapChanger() {
        return phaseTapChanger;
    }

    @Override
    public NetworkImpl getNetwork() {
        return substation.getNetwork();
    }

    @Override
    public void setRatioTapChanger(RatioTapChangerImpl ratioTapChanger) {
        this.ratioTapChanger = ratioTapChanger;
    }

    void setPhaseTapChanger(PhaseTapChangerImpl phaseTapChanger) {
        this.phaseTapChanger = phaseTapChanger;
    }

    @Override
    public void extendStateArraySize(int initStateArraySize, int number, int sourceIndex) {
        super.extendStateArraySize(initStateArraySize, number, sourceIndex);
        if (ratioTapChanger != null) {
            ratioTapChanger.extendStateArraySize(initStateArraySize, number, sourceIndex);
        }
        if (phaseTapChanger != null) {
            phaseTapChanger.extendStateArraySize(initStateArraySize, number, sourceIndex);
        }
    }

    @Override
    public void reduceStateArraySize(int number) {
        super.reduceStateArraySize(number);
        if (ratioTapChanger != null) {
            ratioTapChanger.reduceStateArraySize(number);
        }
        if (phaseTapChanger != null) {
            phaseTapChanger.reduceStateArraySize(number);
        }
    }

    @Override
    public void deleteStateArrayElement(int index) {
        super.deleteStateArrayElement(index);
        if (ratioTapChanger != null) {
            ratioTapChanger.deleteStateArrayElement(index);
        }
        if (phaseTapChanger != null) {
            phaseTapChanger.deleteStateArrayElement(index);
        }
    }

    @Override
    public void allocateStateArrayElement(int[] indexes, int sourceIndex) {
        super.allocateStateArrayElement(indexes, sourceIndex);
        if (ratioTapChanger != null) {
            ratioTapChanger.allocateStateArrayElement(indexes, sourceIndex);
        }
        if (phaseTapChanger != null) {
            phaseTapChanger.allocateStateArrayElement(indexes, sourceIndex);
        }
    }

    public Identifiable getTransformer() {
        return this;
    }

    @Override
    public String getTapChangerAttribute() {
        return "ratioTapChanger";
    }

    @Override
    protected String getTypeDescription() {
        return "2 windings transformer";
    }
}
