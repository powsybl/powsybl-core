/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import com.powsybl.iidm.network.*;
import com.powsybl.commons.ref.Ref;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class TwoWindingsTransformerImpl extends AbstractConnectableBranch<TwoWindingsTransformer>
    implements TwoWindingsTransformer, RatioTapChangerParent, PhaseTapChangerParent {

    private final SubstationImpl substation;

    private double r;

    private double x;

    private double g;

    private double b;

    private double ratedU1;

    private double ratedU2;

    private double ratedS;

    private RatioTapChangerImpl ratioTapChanger;

    private PhaseTapChangerImpl phaseTapChanger;

    TwoWindingsTransformerImpl(Ref<NetworkImpl> network, String id, String name, boolean fictitious,
                               SubstationImpl substation,
                               double r, double x, double g, double b, double ratedU1, double ratedU2, double ratedS) {
        super(network, id, name, fictitious);
        this.substation = substation;
        this.r = r;
        this.x = x;
        this.g = g;
        this.b = b;
        this.ratedU1 = ratedU1;
        this.ratedU2 = ratedU2;
        this.ratedS = ratedS;
    }

    @Override
    public Optional<Substation> getSubstation() {
        return Optional.ofNullable(substation);
    }

    @Override
    public Substation getNullableSubstation() {
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
    public double getRatedS() {
        return ratedS;
    }

    @Override
    public TwoWindingsTransformer setRatedS(double ratedS) {
        ValidationUtil.checkRatedS(this, ratedS);
        double oldValue = this.ratedS;
        this.ratedS = ratedS;
        notifyUpdate("ratedS", oldValue, ratedS);
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
    public Optional<RatioTapChanger> getOptionalRatioTapChanger() {
        return Optional.ofNullable(ratioTapChanger);
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
    public Optional<PhaseTapChanger> getOptionalPhaseTapChanger() {
        return Optional.ofNullable(phaseTapChanger);
    }

    @Override
    public Set<TapChanger<?, ?, ?, ?>> getAllTapChangers() {
        Set<TapChanger<?, ?, ?, ?>> tapChangers = new HashSet<>();
        if (ratioTapChanger != null) {
            tapChangers.add(ratioTapChanger);
        }
        if (phaseTapChanger != null) {
            tapChangers.add(phaseTapChanger);
        }
        return tapChangers;
    }

    @Override
    public boolean hasRatioTapChanger() {
        return ratioTapChanger != null;
    }

    @Override
    public boolean hasPhaseTapChanger() {
        return phaseTapChanger != null;
    }

    @Override
    public void setRatioTapChanger(RatioTapChangerImpl ratioTapChanger) {
        RatioTapChangerImpl oldValue = this.ratioTapChanger;
        this.ratioTapChanger = ratioTapChanger;
        notifyUpdate("ratioTapChanger", oldValue, ratioTapChanger);
    }

    public void setPhaseTapChanger(PhaseTapChangerImpl phaseTapChanger) {
        PhaseTapChangerImpl oldValue = this.phaseTapChanger;
        this.phaseTapChanger = phaseTapChanger;
        notifyUpdate("phaseTapChanger", oldValue, phaseTapChanger);
    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        super.extendVariantArraySize(initVariantArraySize, number, sourceIndex);
        if (ratioTapChanger != null) {
            ratioTapChanger.extendVariantArraySize(initVariantArraySize, number, sourceIndex);
        }
        if (phaseTapChanger != null) {
            phaseTapChanger.extendVariantArraySize(initVariantArraySize, number, sourceIndex);
        }
    }

    @Override
    public void reduceVariantArraySize(int number) {
        super.reduceVariantArraySize(number);
        if (ratioTapChanger != null) {
            ratioTapChanger.reduceVariantArraySize(number);
        }
        if (phaseTapChanger != null) {
            phaseTapChanger.reduceVariantArraySize(number);
        }
    }

    @Override
    public void deleteVariantArrayElement(int index) {
        super.deleteVariantArrayElement(index);
        if (ratioTapChanger != null) {
            ratioTapChanger.deleteVariantArrayElement(index);
        }
        if (phaseTapChanger != null) {
            phaseTapChanger.deleteVariantArrayElement(index);
        }
    }

    @Override
    public void allocateVariantArrayElement(int[] indexes, int sourceIndex) {
        super.allocateVariantArrayElement(indexes, sourceIndex);
        if (ratioTapChanger != null) {
            ratioTapChanger.allocateVariantArrayElement(indexes, sourceIndex);
        }
        if (phaseTapChanger != null) {
            phaseTapChanger.allocateVariantArrayElement(indexes, sourceIndex);
        }
    }

    public Identifiable getTransformer() {
        return this;
    }

    @Override
    public String getTapChangerAttribute() {
        return "TapChanger";
    }

    @Override
    protected String getTypeDescription() {
        return "2 windings transformer";
    }

    @Override
    public void remove() {
        if (ratioTapChanger != null) {
            ratioTapChanger.remove();
        }
        if (phaseTapChanger != null) {
            phaseTapChanger.remove();
        }
        super.remove();
    }
}
