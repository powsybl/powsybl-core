/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.util.trove.TBooleanArrayList;
import com.powsybl.iidm.network.Battery;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Injection;
import com.powsybl.iidm.network.extensions.ActivePowerControl;
import com.powsybl.iidm.network.impl.AbstractMultiVariantIdentifiableExtension;
import com.powsybl.iidm.network.impl.NetworkImpl;
import gnu.trove.list.array.TDoubleArrayList;
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
    private final TBooleanArrayList participate;

    private final TDoubleArrayList droop;
    private final TDoubleArrayList participationFactor;

    private final TDoubleArrayList minTargetP;
    private final TDoubleArrayList maxTargetP;

    private final List<TDoubleArrayList> allTDoubleArrayLists;

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
        int variantArraySize = getVariantManagerHolder().getVariantManager().getVariantArraySize();
        this.participate = new TBooleanArrayList(variantArraySize);
        this.droop = new TDoubleArrayList(variantArraySize);
        this.participationFactor = new TDoubleArrayList(variantArraySize);
        this.minTargetP = new TDoubleArrayList(variantArraySize);
        this.maxTargetP = new TDoubleArrayList(variantArraySize);
        this.allTDoubleArrayLists = List.of(this.droop, this.participationFactor, this.minTargetP, this.maxTargetP);
        double checkedMinTargetP = checkTargetPLimit(minTargetP, "minTargetP", component);
        double checkedMaxTargetP = checkTargetPLimit(maxTargetP, "maxTargetP", component);
        for (int i = 0; i < variantArraySize; i++) {
            this.participate.add(participate);
            this.droop.add(droop);
            this.participationFactor.add(participationFactor);
            this.minTargetP.add(checkedMinTargetP);
            this.maxTargetP.add(checkedMaxTargetP);
        }
        checkLimitOrder(minTargetP, maxTargetP);
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

    private double checkTargetPLimit(double targetPLimit, String name, T injection) {
        PLimits pLimits = getPLimits(injection);

        if (!Double.isNaN(targetPLimit) && (targetPLimit < pLimits.minP || targetPLimit > pLimits.maxP)) {
            throw new PowsyblException(String.format("%s value (%s) is not between minP and maxP for component %s",
                    name,
                    targetPLimit,
                    injection.getId()));
        }

        return targetPLimit;
    }

    private void checkLimitOrder(double minTargetP, double maxTargetP) {
        if (!Double.isNaN(minTargetP) && !Double.isNaN(maxTargetP)
                && minTargetP > maxTargetP) {
            throw new PowsyblException("invalid targetP limits [" + minTargetP + ", " + maxTargetP + "]");
        }
    }

    public boolean isParticipate() {
        return participate.get(getVariantIndex());
    }

    public void setParticipate(boolean participate) {
        int variantIndex = getVariantIndex();
        boolean oldParticipate = this.participate.get(variantIndex);
        if (oldParticipate != participate) {
            this.participate.set(variantIndex, participate);
            NetworkImpl network = (NetworkImpl) getExtendable().getNetwork();
            String variantId = getVariantManagerHolder().getVariantManager().getWorkingVariantId();
            network.getListeners().notifyExtensionUpdate(this, "participate", variantId, oldParticipate, participate);
        }
    }

    public double getDroop() {
        return droop.get(getVariantIndex());
    }

    public void setDroop(double droop) {
        int variantIndex = getVariantIndex();
        double oldDroop = this.droop.get(variantIndex);
        if (oldDroop != droop) {
            this.droop.set(variantIndex, droop);
            NetworkImpl network = (NetworkImpl) getExtendable().getNetwork();
            String variantId = getVariantManagerHolder().getVariantManager().getWorkingVariantId();
            network.getListeners().notifyExtensionUpdate(this, "droop", variantId, oldDroop, droop);
        }
    }

    public double getParticipationFactor() {
        return participationFactor.get(getVariantIndex());
    }

    public void setParticipationFactor(double participationFactor) {
        this.participationFactor.set(getVariantIndex(), participationFactor);
    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        participate.ensureCapacity(participate.size() + number);
        allTDoubleArrayLists.forEach(dl -> dl.ensureCapacity(dl.size() + number));
        for (int i = 0; i < number; ++i) {
            participate.add(participate.get(sourceIndex));
            allTDoubleArrayLists.forEach(dl -> dl.add(dl.get(sourceIndex)));
        }
    }

    @Override
    public void reduceVariantArraySize(int number) {
        participate.remove(participate.size() - number, number);
        allTDoubleArrayLists.forEach(dl -> dl.remove(dl.size() - number, number));
    }

    @Override
    public void deleteVariantArrayElement(int index) {
        // Does nothing
    }

    @Override
    public void allocateVariantArrayElement(int[] indexes, int sourceIndex) {
        for (int index : indexes) {
            participate.set(index, participate.get(sourceIndex));
            allTDoubleArrayLists.forEach(dl -> dl.set(index, dl.get(sourceIndex)));
        }
    }

    @Override
    public OptionalDouble getMinTargetP() {
        double result = minTargetP.get(getVariantIndex());
        return Double.isNaN(result) ? OptionalDouble.empty() : OptionalDouble.of(withinPMinMax(result, getExtendable()));
    }

    @Override
    public void setMinTargetP(double minTargetP) {
        checkLimitOrder(minTargetP, maxTargetP.get(getVariantIndex()));
        this.minTargetP.set(getVariantIndex(), checkTargetPLimit(minTargetP, "minTargetP", getExtendable()));
    }

    @Override
    public OptionalDouble getMaxTargetP() {
        double result = maxTargetP.get(getVariantIndex());
        return Double.isNaN(result) ? OptionalDouble.empty() : OptionalDouble.of(withinPMinMax(result, getExtendable()));
    }

    @Override
    public void setMaxTargetP(double maxTargetP) {
        checkLimitOrder(minTargetP.get(getVariantIndex()), maxTargetP);
        this.maxTargetP.set(getVariantIndex(), checkTargetPLimit(maxTargetP, "maxTargetP", getExtendable()));
    }
}
