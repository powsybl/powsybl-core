/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.util.fastutil.ExtendedBooleanArrayList;
import com.powsybl.commons.util.fastutil.ExtendedDoubleArrayList;
import com.powsybl.iidm.network.Battery;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Injection;
import com.powsybl.iidm.network.extensions.ActivePowerControl;
import com.powsybl.iidm.network.impl.AbstractMultiVariantIdentifiableExtension;
import com.powsybl.iidm.network.impl.NetworkImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.OptionalDouble;

/**
 * @author Ghiles Abdellah {@literal <ghiles.abdellah at rte-france.com>}
 */
public class ActivePowerControlImpl<T extends Injection<T>> extends AbstractMultiVariantIdentifiableExtension<T>
        implements ActivePowerControl<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActivePowerControlImpl.class);
    private static final String MIN_TARGET_P = "minTargetP";
    private static final String MAX_TARGET_P = "maxTargetP";

    private final ExtendedBooleanArrayList participate;

    private final ExtendedDoubleArrayList droop;
    private final ExtendedDoubleArrayList participationFactor;

    private final ExtendedDoubleArrayList minTargetP;
    private final ExtendedDoubleArrayList maxTargetP;

    private final List<ExtendedDoubleArrayList> allExtendedDoubleArrayList;

    public ActivePowerControlImpl(T component,
                                  boolean participate,
                                  double droop,
                                  double participationFactor) {
        this(component, participate, droop, participationFactor, Double.NaN, Double.NaN);
    }

    public ActivePowerControlImpl(T component,
                                  boolean participate,
                                  double droop,
                                  double participationFactor,
                                  double minTargetP,
                                  double maxTargetP) {
        super(component);
        checkTargetPLimit(minTargetP, MIN_TARGET_P, component);
        checkTargetPLimit(maxTargetP, MAX_TARGET_P, component);
        checkLimitOrder(minTargetP, maxTargetP);
        int variantArraySize = getVariantManagerHolder().getVariantManager().getVariantArraySize();
        this.participate = new ExtendedBooleanArrayList(variantArraySize, participate);
        this.droop = new ExtendedDoubleArrayList(variantArraySize, droop);
        this.participationFactor = new ExtendedDoubleArrayList(variantArraySize, participationFactor);
        this.minTargetP = new ExtendedDoubleArrayList(variantArraySize, minTargetP);
        this.maxTargetP = new ExtendedDoubleArrayList(variantArraySize, maxTargetP);
        this.allExtendedDoubleArrayList = List.of(this.droop, this.participationFactor, this.minTargetP, this.maxTargetP);
    }

    record PLimits(double minP, double maxP) { }

    private PLimits getPLimits(T injection) {
        double maxP = Double.MAX_VALUE;
        double minP = -Double.MAX_VALUE;
        if (injection instanceof Generator generator) {
            maxP = generator.getMaxP();
            minP = generator.getMinP();
        } else if (injection instanceof Battery battery) {
            maxP = battery.getMaxP();
            minP = battery.getMinP();
        }
        return new PLimits(minP, maxP);
    }

    private double withinPMinMax(double value, T injection) {
        PLimits pLimits = getPLimits(injection);

        if (!Double.isNaN(value) && (value < pLimits.minP || value > pLimits.maxP)) {
            LOGGER.warn("targetP limit is now outside of pMin,pMax for component {}. Returning closest value in [pmin,pMax].",
                        injection.getId());
            return value < pLimits.minP ? pLimits.minP : pLimits.maxP;
        }
        return value;
    }

    private void checkTargetPLimit(double targetPLimit, String name, T injection) {
        PLimits pLimits = getPLimits(injection);

        if (!Double.isNaN(targetPLimit) && (targetPLimit < pLimits.minP || targetPLimit > pLimits.maxP)) {
            throw new PowsyblException(String.format("%s value (%s) is not between minP and maxP for component %s",
                    name,
                    targetPLimit,
                    injection.getId()));
        }
    }

    private static void checkLimitOrder(double minTargetP, double maxTargetP) {
        if (!Double.isNaN(minTargetP) && !Double.isNaN(maxTargetP)
                && minTargetP > maxTargetP) {
            throw new PowsyblException("invalid targetP limits [" + minTargetP + ", " + maxTargetP + "]");
        }
    }

    public boolean isParticipate() {
        return participate.getBoolean(getVariantIndex());
    }

    public void setParticipate(boolean participate) {
        int variantIndex = getVariantIndex();
        boolean oldParticipate = this.participate.getBoolean(variantIndex);
        if (oldParticipate != participate) {
            this.participate.set(variantIndex, participate);
            notifyUpdate("participate", oldParticipate, participate);
        }
    }

    public double getDroop() {
        return droop.getDouble(getVariantIndex());
    }

    public void setDroop(double droop) {
        int variantIndex = getVariantIndex();
        double oldDroop = this.droop.getDouble(variantIndex);
        if (Double.compare(oldDroop, droop) != 0) {
            this.droop.set(variantIndex, droop);
            notifyUpdate("droop", oldDroop, droop);
        }
    }

    public double getParticipationFactor() {
        return participationFactor.getDouble(getVariantIndex());
    }

    public void setParticipationFactor(double participationFactor) {
        int variantIndex = getVariantIndex();
        double oldParticipationFactor = this.participationFactor.get(variantIndex);
        if (Double.compare(oldParticipationFactor, participationFactor) != 0) {
            this.participationFactor.set(variantIndex, participationFactor);
            notifyUpdate("participationFactor", oldParticipationFactor, participationFactor);
        }
    }

    private void notifyUpdate(String attribute, Object oldValue, Object newValue) {
        NetworkImpl network = (NetworkImpl) getExtendable().getNetwork();
        String variantId = getVariantManagerHolder().getVariantManager().getWorkingVariantId();
        network.getListeners().notifyExtensionUpdate(this, attribute, variantId, oldValue, newValue);
    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        participate.growAndFill(number, participate.getBoolean(sourceIndex));
        allExtendedDoubleArrayList.forEach(dl -> dl.growAndFill(number, dl.getDouble(sourceIndex)));
    }

    @Override
    public void reduceVariantArraySize(int number) {
        participate.removeElements(number);
        allExtendedDoubleArrayList.forEach(dl -> dl.removeElements(number));
    }

    @Override
    public void deleteVariantArrayElement(int index) {
        // Does nothing
    }

    @Override
    public void allocateVariantArrayElement(int[] indexes, int sourceIndex) {
        for (int index : indexes) {
            participate.set(index, participate.getBoolean(sourceIndex));
            allExtendedDoubleArrayList.forEach(dl -> dl.set(index, dl.getDouble(sourceIndex)));
        }
    }

    @Override
    public OptionalDouble getMinTargetP() {
        double result = minTargetP.getDouble(getVariantIndex());
        return Double.isNaN(result) ? OptionalDouble.empty() : OptionalDouble.of(withinPMinMax(result, getExtendable()));
    }

    @Override
    public void setMinTargetP(double minTargetP) {
        int variantIndex = getVariantIndex();
        checkTargetPLimit(minTargetP, MIN_TARGET_P, getExtendable());
        checkLimitOrder(minTargetP, maxTargetP.getDouble(variantIndex));
        double oldMinTargetP = this.minTargetP.getDouble(variantIndex);
        if (Double.compare(oldMinTargetP, minTargetP) != 0) {
            this.minTargetP.set(variantIndex, minTargetP);
            notifyUpdate(MIN_TARGET_P, oldMinTargetP, minTargetP);
        }
    }

    @Override
    public OptionalDouble getMaxTargetP() {
        double result = maxTargetP.getDouble(getVariantIndex());
        return Double.isNaN(result) ? OptionalDouble.empty() : OptionalDouble.of(withinPMinMax(result, getExtendable()));
    }

    @Override
    public void setMaxTargetP(double maxTargetP) {
        int variantIndex = getVariantIndex();
        checkTargetPLimit(maxTargetP, MAX_TARGET_P, getExtendable());
        checkLimitOrder(minTargetP.getDouble(variantIndex), maxTargetP);
        double oldMaxTargetP = this.maxTargetP.getDouble(variantIndex);
        if (Double.compare(oldMaxTargetP, maxTargetP) != 0) {
            this.maxTargetP.set(variantIndex, maxTargetP);
            notifyUpdate(MAX_TARGET_P, oldMaxTargetP, maxTargetP);
        }
    }
}
