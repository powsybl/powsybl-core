/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.shortcircuit;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtendable;

import java.util.List;

import static com.powsybl.shortcircuit.ShortCircuitConstants.*;

/**
 * @author Coline Piloquet <coline.piloquet@rte-france.com>
 */
public class ShortCircuitParametersBuilder extends AbstractExtendable<ShortCircuitParametersBuilder> {

    private boolean withLimitViolations = DEFAULT_WITH_LIMIT_VIOLATIONS;
    private boolean withFortescueResult = DEFAULT_WITH_FORTESCUE_RESULT;
    private boolean withFeederResult = DEFAULT_WITH_FEEDER_RESULT;
    private StudyType studyType = DEFAULT_STUDY_TYPE;
    private boolean withVoltageResult = DEFAULT_WITH_VOLTAGE_RESULT;
    private double minVoltageDropProportionalThreshold = DEFAULT_MIN_VOLTAGE_DROP_PROPORTIONAL_THRESHOLD;
    private double subTransientCoefficient = DEFAULT_SUB_TRANSIENT_COEFFICIENT;
    private boolean withLoads = DEFAULT_WITH_LOADS;
    private boolean withShuntCompensators = DEFAULT_WITH_SHUNT_COMPENSATORS;
    private boolean withVSCConverterStations = DEFAULT_WITH_VSC_CONVERTER_STATIONS;
    private boolean withNeutralPosition = DEFAULT_WITH_NEUTRAL_POSITION;
    private InitialVoltageProfileMode initialVoltageProfileMode = DEFAULT_INITIAL_VOLTAGE_PROFILE_MODE;
    private List<VoltageRangeData> voltageRangeData = null;

    public ShortCircuitParameters build() {
        if (initialVoltageProfileMode == InitialVoltageProfileMode.CONFIGURED && voltageRangeData == null) {
            throw new PowsyblException("Configured initial voltage profile but nominal voltage ranges with associated coefficients are missing.");
        }
        return new ShortCircuitParameters()
                .setWithLimitViolations(withLimitViolations)
                .setWithFortescueResult(withFortescueResult)
                .setStudyType(studyType)
                .setSubTransientCoefficient(subTransientCoefficient)
                .setWithFeederResult(withFeederResult)
                .setWithVoltageResult(withVoltageResult)
                .setMinVoltageDropProportionalThreshold(minVoltageDropProportionalThreshold)
                .setWithLoads(withLoads)
                .setWithShuntCompensators(withShuntCompensators)
                .setWithVSCConverterStations(withVSCConverterStations)
                .setWithNeutralPosition(withNeutralPosition)
                .setInitialVoltageProfileMode(initialVoltageProfileMode)
                .setVoltageRangeData(voltageRangeData);

    }

    public ShortCircuitParametersBuilder withLimitViolations(boolean withLimitViolations) {
        this.withLimitViolations = withLimitViolations;
        return this;
    }

    public ShortCircuitParametersBuilder withFortescueResult(boolean withFortescueResult) {
        this.withFortescueResult = withFortescueResult;
        return this;
    }

    public ShortCircuitParametersBuilder withStudyType(StudyType studyType) {
        this.studyType = studyType;
        return this;
    }

    public ShortCircuitParametersBuilder withFeederResult(boolean withFeederResult) {
        this.withFeederResult = withFeederResult;
        return this;
    }

    public ShortCircuitParametersBuilder withVoltageResult(boolean withVoltageResult) {
        this.withVoltageResult = withVoltageResult;
        return this;
    }

    public ShortCircuitParametersBuilder withMinVoltageDropProportionalThreshold(double minVoltageDropProportionalThreshold) {
        this.minVoltageDropProportionalThreshold = minVoltageDropProportionalThreshold;
        return this;
    }

    public ShortCircuitParametersBuilder withSubTransientCoefficient(double subTransientCoefficient) {
        this.subTransientCoefficient = subTransientCoefficient;
        return this;
    }

    public ShortCircuitParametersBuilder withLoads(boolean withLoads) {
        this.withLoads = withLoads;
        return this;
    }

    public ShortCircuitParametersBuilder withShuntCompensators(boolean withShuntCompensators) {
        this.withShuntCompensators = withShuntCompensators;
        return this;
    }

    public ShortCircuitParametersBuilder withVSCConverterStations(boolean withVSCConverterStations) {
        this.withVSCConverterStations = withVSCConverterStations;
        return this;
    }

    public ShortCircuitParametersBuilder withNeutralPosition(boolean withNeutralPosition) {
        this.withNeutralPosition = withNeutralPosition;
        return this;
    }

    public ShortCircuitParametersBuilder withInitialVoltageProfileMode(InitialVoltageProfileMode initialVoltageProfileMode) {
        this.initialVoltageProfileMode = initialVoltageProfileMode;
        return this;
    }

    public ShortCircuitParametersBuilder withVoltageRangeData(List<VoltageRangeData> voltageRangeData) {
        this.voltageRangeData = voltageRangeData;
        return this;
    }
}
