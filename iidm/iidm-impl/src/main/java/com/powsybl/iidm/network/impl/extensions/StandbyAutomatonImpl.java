/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.util.trove.TBooleanArrayList;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.extensions.StandbyAutomaton;
import com.powsybl.iidm.network.impl.AbstractMultiVariantIdentifiableExtension;
import gnu.trove.list.array.TDoubleArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jérémy Labous {@literal <jlabous at silicom.fr>}
 */
public class StandbyAutomatonImpl extends AbstractMultiVariantIdentifiableExtension<StaticVarCompensator> implements StandbyAutomaton {

    private static final Logger LOGGER = LoggerFactory.getLogger(StandbyAutomatonImpl.class);

    private double b0;
    private final TBooleanArrayList standby;
    private final TDoubleArrayList lowVoltageSetpoint;
    private final TDoubleArrayList highVoltageSetpoint;
    private final TDoubleArrayList lowVoltageThreshold;
    private final TDoubleArrayList highVoltageThreshold;

    private static double checkB0(double b0) {
        if (Double.isNaN(b0)) {
            throw new IllegalArgumentException("b0 is invalid");
        }
        return b0;
    }

    private static void checkVoltageConfig(double lowVoltageSetpoint, double highVoltageSetpoint,
                                           double lowVoltageThreshold, double highVoltageThreshold,
                                           StaticVarCompensator staticVarCompensator) {
        if (Double.isNaN(lowVoltageSetpoint)) {
            throw new IllegalArgumentException(
                String.format("lowVoltageSetpoint (%s) is invalid for StaticVarCompensator %s",
                    lowVoltageSetpoint,
                    staticVarCompensator.getId()));
        }
        if (Double.isNaN(highVoltageSetpoint)) {
            throw new IllegalArgumentException(
                String.format("highVoltageSetpoint (%s) is invalid for StaticVarCompensator %s",
                    highVoltageSetpoint,
                    staticVarCompensator.getId()));
        }
        if (Double.isNaN(lowVoltageThreshold)) {
            throw new IllegalArgumentException(
                String.format("lowVoltageThreshold (%s) is invalid for StaticVarCompensator %s",
                    lowVoltageThreshold,
                    staticVarCompensator.getId()));
        }
        if (Double.isNaN(highVoltageThreshold)) {
            throw new IllegalArgumentException(
                String.format("highVoltageThreshold (%s) is invalid for StaticVarCompensator %s",
                    highVoltageThreshold,
                    staticVarCompensator.getId()));
        }
        if (lowVoltageThreshold >= highVoltageThreshold) {
            throw new IllegalArgumentException(
                String.format("Inconsistent low (%s) and high (%s) voltage thresholds for StaticVarCompensator %s",
                    lowVoltageThreshold,
                    highVoltageThreshold,
                    staticVarCompensator.getId()));
        }
        if (lowVoltageSetpoint < lowVoltageThreshold) {
            LOGGER.warn("Invalid low voltage setpoint {} < threshold {} for StaticVarCompensator {}",
                lowVoltageSetpoint, lowVoltageThreshold,
                staticVarCompensator.getId());
        }
        if (highVoltageSetpoint > highVoltageThreshold) {
            LOGGER.warn("Invalid high voltage setpoint {} > threshold {} for StaticVarCompensator {}",
                highVoltageSetpoint, highVoltageThreshold,
                staticVarCompensator.getId());
        }
    }

    public StandbyAutomatonImpl(StaticVarCompensator svc, double b0, boolean standby, double lowVoltageSetpoint, double highVoltageSetpoint,
                                double lowVoltageThreshold, double highVoltageThreshold) {
        super(svc);
        int variantArraySize = getVariantManagerHolder().getVariantManager().getVariantArraySize();
        checkVoltageConfig(lowVoltageSetpoint, highVoltageSetpoint, lowVoltageThreshold, highVoltageThreshold, svc);
        this.b0 = checkB0(b0);
        this.standby = new TBooleanArrayList(variantArraySize);
        this.lowVoltageSetpoint = new TDoubleArrayList(variantArraySize);
        this.highVoltageSetpoint = new TDoubleArrayList(variantArraySize);
        this.lowVoltageThreshold = new TDoubleArrayList(variantArraySize);
        this.highVoltageThreshold = new TDoubleArrayList(variantArraySize);
        for (int i = 0; i < variantArraySize; i++) {
            this.standby.add(standby);
            this.lowVoltageSetpoint.add(lowVoltageSetpoint);
            this.highVoltageSetpoint.add(highVoltageSetpoint);
            this.lowVoltageThreshold.add(lowVoltageThreshold);
            this.highVoltageThreshold.add(highVoltageThreshold);
        }
    }

    @Override
    public boolean isStandby() {
        return standby.get(getVariantIndex());
    }

    @Override
    public StandbyAutomatonImpl setStandby(boolean standby) {
        this.standby.set(getVariantIndex(), standby);
        return this;
    }

    @Override
    public double getB0() {
        return b0;
    }

    @Override
    public StandbyAutomatonImpl setB0(double b0) {
        this.b0 = checkB0(b0);
        return this;
    }

    @Override
    public double getHighVoltageSetpoint() {
        return highVoltageSetpoint.get(getVariantIndex());
    }

    @Override
    public StandbyAutomatonImpl setHighVoltageSetpoint(double highVoltageSetpoint) {
        checkVoltageConfig(lowVoltageSetpoint.get(getVariantIndex()), highVoltageSetpoint,
            lowVoltageThreshold.get(getVariantIndex()), highVoltageThreshold.get(getVariantIndex()),
            this.getExtendable());
        this.highVoltageSetpoint.set(getVariantIndex(), highVoltageSetpoint);
        return this;
    }

    @Override
    public double getHighVoltageThreshold() {
        return highVoltageThreshold.get(getVariantIndex());
    }

    @Override
    public StandbyAutomatonImpl setHighVoltageThreshold(double highVoltageThreshold) {
        checkVoltageConfig(lowVoltageSetpoint.get(getVariantIndex()), highVoltageSetpoint.get(getVariantIndex()),
            lowVoltageThreshold.get(getVariantIndex()), highVoltageThreshold,
            this.getExtendable());
        this.highVoltageThreshold.set(getVariantIndex(), highVoltageThreshold);
        return this;
    }

    @Override
    public double getLowVoltageSetpoint() {
        return lowVoltageSetpoint.get(getVariantIndex());
    }

    @Override
    public StandbyAutomatonImpl setLowVoltageSetpoint(double lowVoltageSetpoint) {
        checkVoltageConfig(lowVoltageSetpoint, highVoltageSetpoint.get(getVariantIndex()),
            lowVoltageThreshold.get(getVariantIndex()), highVoltageThreshold.get(getVariantIndex()),
            this.getExtendable());
        this.lowVoltageSetpoint.set(getVariantIndex(), lowVoltageSetpoint);
        return this;
    }

    @Override
    public double getLowVoltageThreshold() {
        return lowVoltageThreshold.get(getVariantIndex());
    }

    @Override
    public StandbyAutomatonImpl setLowVoltageThreshold(double lowVoltageThreshold) {
        checkVoltageConfig(lowVoltageSetpoint.get(getVariantIndex()), highVoltageSetpoint.get(getVariantIndex()),
            lowVoltageThreshold, highVoltageThreshold.get(getVariantIndex()),
            this.getExtendable());
        this.lowVoltageThreshold.set(getVariantIndex(), lowVoltageThreshold);
        return this;
    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        standby.ensureCapacity(standby.size() + number);
        lowVoltageSetpoint.ensureCapacity(lowVoltageSetpoint.size() + number);
        highVoltageSetpoint.ensureCapacity(highVoltageSetpoint.size() + number);
        lowVoltageThreshold.ensureCapacity(lowVoltageThreshold.size() + number);
        highVoltageThreshold.ensureCapacity(highVoltageThreshold.size() + number);
        for (int i = 0; i < number; i++) {
            standby.add(standby.get(sourceIndex));
            lowVoltageSetpoint.add(lowVoltageSetpoint.get(sourceIndex));
            highVoltageSetpoint.add(highVoltageSetpoint.get(sourceIndex));
            lowVoltageThreshold.add(lowVoltageThreshold.get(sourceIndex));
            highVoltageThreshold.add(highVoltageThreshold.get(sourceIndex));
        }
    }

    @Override
    public void reduceVariantArraySize(int number) {
        standby.remove(standby.size() - number, number);
        lowVoltageSetpoint.remove(lowVoltageSetpoint.size() - number, number);
        highVoltageSetpoint.remove(highVoltageSetpoint.size() - number, number);
        lowVoltageThreshold.remove(lowVoltageThreshold.size() - number, number);
        highVoltageThreshold.remove(highVoltageThreshold.size() - number, number);
    }

    @Override
    public void deleteVariantArrayElement(int i) {
        // Does nothing
    }

    @Override
    public void allocateVariantArrayElement(int[] indexes, int sourceIndex) {
        for (int index : indexes) {
            standby.set(index, standby.get(sourceIndex));
            lowVoltageSetpoint.set(index, lowVoltageSetpoint.get(sourceIndex));
            highVoltageSetpoint.set(index, highVoltageSetpoint.get(sourceIndex));
            lowVoltageThreshold.set(index, lowVoltageThreshold.get(sourceIndex));
            highVoltageThreshold.set(index, highVoltageThreshold.get(sourceIndex));
        }
    }
}
