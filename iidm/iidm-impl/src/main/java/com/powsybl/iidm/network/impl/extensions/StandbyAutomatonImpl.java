/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.commons.util.fastutil.ExtendedBooleanArrayList;
import com.powsybl.commons.util.fastutil.ExtendedDoubleArrayList;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.ValidationException;
import com.powsybl.iidm.network.extensions.StandbyAutomaton;
import com.powsybl.iidm.network.impl.AbstractMultiVariantIdentifiableExtension;
import com.powsybl.iidm.network.impl.StaticVarCompensatorImpl;
import com.powsybl.iidm.network.util.NetworkReports;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jérémy Labous {@literal <jlabous at silicom.fr>}
 */
public class StandbyAutomatonImpl extends AbstractMultiVariantIdentifiableExtension<StaticVarCompensator> implements StandbyAutomaton {

    private static final Logger LOGGER = LoggerFactory.getLogger(StandbyAutomatonImpl.class);

    private double b0;
    private final ExtendedBooleanArrayList standby;
    private final ExtendedDoubleArrayList lowVoltageSetpoint;
    private final ExtendedDoubleArrayList highVoltageSetpoint;
    private final ExtendedDoubleArrayList lowVoltageThreshold;
    private final ExtendedDoubleArrayList highVoltageThreshold;

    private static double checkB0(StaticVarCompensatorImpl svc, double b0) {
        if (Double.isNaN(b0)) {
            throw new ValidationException(svc, "b0 is invalid");
        }
        return b0;
    }

    private static void checkVoltageConfig(StaticVarCompensatorImpl svc, double lowVoltageSetpoint, double highVoltageSetpoint,
                                           double lowVoltageThreshold, double highVoltageThreshold,
                                           boolean standby) {
        ReportNode reportNode = svc.getNetwork().getReportNodeContext().getReportNode();
        if (Double.isNaN(lowVoltageSetpoint)) {
            throw new ValidationException(svc, String.format("low voltage setpoint (%s) is invalid", lowVoltageSetpoint));
        }
        if (Double.isNaN(highVoltageSetpoint)) {
            throw new ValidationException(svc, String.format("high voltage setpoint (%s) is invalid", highVoltageSetpoint));
        }
        if (Double.isNaN(lowVoltageThreshold)) {
            throw new ValidationException(svc, String.format("low voltage threshold (%s) is invalid", lowVoltageThreshold));
        }
        if (Double.isNaN(highVoltageThreshold)) {
            throw new ValidationException(svc, String.format("high voltage threshold (%s) is invalid", highVoltageThreshold));
        }
        if (lowVoltageThreshold >= highVoltageThreshold) {
            if (standby) {
                throw new ValidationException(svc,
                        String.format("Inconsistent low (%s) and high (%s) voltage thresholds",
                                lowVoltageThreshold,
                                highVoltageThreshold));
            } else {
                LOGGER.warn("Inconsistent low {} and high ({}) voltage thresholds for StaticVarCompensator {}",
                        lowVoltageSetpoint, lowVoltageThreshold, svc.getId());
                NetworkReports.svcVoltageThresholdInvalid(reportNode, svc.getId(), lowVoltageThreshold, highVoltageThreshold);
            }
        }

        if (lowVoltageSetpoint < lowVoltageThreshold) {
            LOGGER.warn("Invalid low voltage setpoint {} < threshold {} for StaticVarCompensator {}",
                lowVoltageSetpoint, lowVoltageThreshold, svc.getId());
            NetworkReports.svcLowVoltageSetpointInvalid(reportNode, svc.getId(), lowVoltageSetpoint, lowVoltageThreshold);
        }

        if (highVoltageSetpoint > highVoltageThreshold) {
            LOGGER.warn("Invalid high voltage setpoint {} > threshold {} for StaticVarCompensator {}",
                highVoltageSetpoint, highVoltageThreshold, svc.getId());
            NetworkReports.svcHighVoltageSetpointInvalid(reportNode, svc.getId(), highVoltageSetpoint, highVoltageThreshold);
        }
    }

    public StandbyAutomatonImpl(StaticVarCompensatorImpl svc, double b0, boolean standby, double lowVoltageSetpoint, double highVoltageSetpoint,
                                double lowVoltageThreshold, double highVoltageThreshold) {
        super(svc);
        int variantArraySize = getVariantManagerHolder().getVariantManager().getVariantArraySize();
        checkVoltageConfig(svc, lowVoltageSetpoint, highVoltageSetpoint, lowVoltageThreshold, highVoltageThreshold, standby);
        this.b0 = checkB0(svc, b0);
        this.standby = new ExtendedBooleanArrayList(variantArraySize, standby);
        this.lowVoltageSetpoint = new ExtendedDoubleArrayList(variantArraySize, lowVoltageSetpoint);
        this.highVoltageSetpoint = new ExtendedDoubleArrayList(variantArraySize, highVoltageSetpoint);
        this.lowVoltageThreshold = new ExtendedDoubleArrayList(variantArraySize, lowVoltageThreshold);
        this.highVoltageThreshold = new ExtendedDoubleArrayList(variantArraySize, highVoltageThreshold);
    }

    @Override
    public boolean isStandby() {
        return standby.getBoolean(getVariantIndex());
    }

    @Override
    public StandbyAutomatonImpl setStandby(boolean standby) {
        checkVoltageConfig((StaticVarCompensatorImpl) getExtendable(),
                lowVoltageSetpoint.getDouble(getVariantIndex()), highVoltageSetpoint.getDouble(getVariantIndex()),
                lowVoltageThreshold.getDouble(getVariantIndex()), highVoltageThreshold.getDouble(getVariantIndex()),
                standby);
        this.standby.set(getVariantIndex(), standby);
        return this;
    }

    @Override
    public double getB0() {
        return b0;
    }

    @Override
    public StandbyAutomatonImpl setB0(double b0) {
        this.b0 = checkB0((StaticVarCompensatorImpl) getExtendable(), b0);
        return this;
    }

    @Override
    public double getHighVoltageSetpoint() {
        return highVoltageSetpoint.getDouble(getVariantIndex());
    }

    @Override
    public StandbyAutomatonImpl setHighVoltageSetpoint(double highVoltageSetpoint) {
        checkVoltageConfig((StaticVarCompensatorImpl) getExtendable(), lowVoltageSetpoint.getDouble(getVariantIndex()), highVoltageSetpoint,
            lowVoltageThreshold.getDouble(getVariantIndex()), highVoltageThreshold.getDouble(getVariantIndex()), standby.getBoolean(getVariantIndex()));
        this.highVoltageSetpoint.set(getVariantIndex(), highVoltageSetpoint);
        return this;
    }

    @Override
    public double getHighVoltageThreshold() {
        return highVoltageThreshold.getDouble(getVariantIndex());
    }

    @Override
    public StandbyAutomatonImpl setHighVoltageThreshold(double highVoltageThreshold) {
        checkVoltageConfig((StaticVarCompensatorImpl) getExtendable(), lowVoltageSetpoint.getDouble(getVariantIndex()), highVoltageSetpoint.getDouble(getVariantIndex()),
            lowVoltageThreshold.getDouble(getVariantIndex()), highVoltageThreshold, standby.getBoolean(getVariantIndex()));
        this.highVoltageThreshold.set(getVariantIndex(), highVoltageThreshold);
        return this;
    }

    @Override
    public double getLowVoltageSetpoint() {
        return lowVoltageSetpoint.getDouble(getVariantIndex());
    }

    @Override
    public StandbyAutomatonImpl setLowVoltageSetpoint(double lowVoltageSetpoint) {
        checkVoltageConfig((StaticVarCompensatorImpl) getExtendable(), lowVoltageSetpoint, highVoltageSetpoint.getDouble(getVariantIndex()),
            lowVoltageThreshold.getDouble(getVariantIndex()), highVoltageThreshold.getDouble(getVariantIndex()), standby.getBoolean(getVariantIndex()));
        this.lowVoltageSetpoint.set(getVariantIndex(), lowVoltageSetpoint);
        return this;
    }

    @Override
    public double getLowVoltageThreshold() {
        return lowVoltageThreshold.getDouble(getVariantIndex());
    }

    @Override
    public StandbyAutomatonImpl setLowVoltageThreshold(double lowVoltageThreshold) {
        checkVoltageConfig((StaticVarCompensatorImpl) getExtendable(), lowVoltageSetpoint.getDouble(getVariantIndex()), highVoltageSetpoint.getDouble(getVariantIndex()),
            lowVoltageThreshold, highVoltageThreshold.getDouble(getVariantIndex()), standby.getBoolean(getVariantIndex()));
        this.lowVoltageThreshold.set(getVariantIndex(), lowVoltageThreshold);
        return this;
    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        standby.growAndFill(number, standby.getBoolean(sourceIndex));
        lowVoltageSetpoint.growAndFill(number, lowVoltageSetpoint.getDouble(sourceIndex));
        highVoltageSetpoint.growAndFill(number, highVoltageSetpoint.getDouble(sourceIndex));
        lowVoltageThreshold.growAndFill(number, lowVoltageThreshold.getDouble(sourceIndex));
        highVoltageThreshold.growAndFill(number, highVoltageThreshold.getDouble(sourceIndex));
    }

    @Override
    public void reduceVariantArraySize(int number) {
        standby.removeElements(number);
        lowVoltageSetpoint.removeElements(number);
        highVoltageSetpoint.removeElements(number);
        lowVoltageThreshold.removeElements(number);
        highVoltageThreshold.removeElements(number);
    }

    @Override
    public void deleteVariantArrayElement(int i) {
        // Does nothing
    }

    @Override
    public void allocateVariantArrayElement(int[] indexes, int sourceIndex) {
        for (int index : indexes) {
            standby.set(index, standby.getBoolean(sourceIndex));
            lowVoltageSetpoint.set(index, lowVoltageSetpoint.getDouble(sourceIndex));
            highVoltageSetpoint.set(index, highVoltageSetpoint.getDouble(sourceIndex));
            lowVoltageThreshold.set(index, lowVoltageThreshold.getDouble(sourceIndex));
            highVoltageThreshold.set(index, highVoltageThreshold.getDouble(sourceIndex));
        }
    }
}
