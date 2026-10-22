/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.limitscaling.computation;

import com.powsybl.iidm.network.DetectionKind;
import com.powsybl.iidm.network.LimitType;
import com.powsybl.iidm.network.LoadingLimits;
import com.powsybl.iidm.network.limitmodification.result.*;
import com.powsybl.security.limitscaling.result.*;

import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

/**
 * <p>{@link AbstractLimitsScaler} implementation responsible for computing scaled limits of type {@link LoadingLimits}.</p>
 *
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public class DefaultLimitsScaler extends AbstractLimitsScaler<LoadingLimits> {

    public DefaultLimitsScaler(LoadingLimits originalLimits, String limitsGroupId) {
        super(originalLimits, limitsGroupId);
    }

    @Override
    protected LimitsContainer<LoadingLimits> scale() {
        LoadingLimits originalLimits = getOriginalLimits();
        AbstractScaledLoadingLimits scaledLoadingLimits;
        if (originalLimits.getDetectionKind() == DetectionKind.HIGH) {
            double scaledPermanentLimit = applyScaling(originalLimits.getPermanentLimit(), getPermanentLimitScaling());
            scaledLoadingLimits = initHigh(originalLimits.getLimitType(), scaledPermanentLimit,
                originalLimits.getPermanentLimit(), getPermanentLimitScaling(), originalLimits.getPermanentLimitName());
        } else {
            scaledLoadingLimits = initLow(originalLimits.getLimitType());
        }

        // Compute the temporary limits:
        // A temporary limit L1 should be ignored (not created) if there exists another temporary limit L2
        // such as: acceptableDuration(L2) < acceptableDuration(L1) AND scaledValue(L2) <= scaledValue(L1)
        List<LoadingLimits.TemporaryLimit> temporaryLimits = originalLimits.getTemporaryLimits().stream()
                .sorted(Comparator.comparing(LoadingLimits.TemporaryLimit::getAcceptableDuration)).toList();
        double previousRetainedScaledValue = Double.NaN;
        for (LoadingLimits.TemporaryLimit tl : temporaryLimits) { // iterate in ascending order of the durations
            double scaling = getTemporaryLimitScaling(tl.getAcceptableDuration());
            double tlScaledValue = applyScaling(tl.getValue(), scaling);
            if (Double.isNaN(previousRetainedScaledValue) || tlScaledValue < previousRetainedScaledValue) {
                previousRetainedScaledValue = tlScaledValue;
                scaledLoadingLimits.addTemporaryLimit(tl.getName(), tlScaledValue, tl.getAcceptableDuration(),
                        tl.isFictitious(), tl.getValue(), scaling);
            }
        }
        return new DefaultScaledLimitsContainer(scaledLoadingLimits, originalLimits, getLimitsGroupId());
    }

    @Override
    public IntStream getTemporaryLimitsAcceptableDurationStream() {
        return getOriginalLimits().getTemporaryLimits().stream().mapToInt(LoadingLimits.TemporaryLimit::getAcceptableDuration);
    }

    private AbstractScaledLoadingLimits initHigh(LimitType type, double permanentLimit,
                                                 double originalPermanentLimit,
                                                 double permanentLimitScaling,
                                                 String permanentLimitName) {
        return switch (type) {
            case ACTIVE_POWER -> new ScaledActivePowerLimits(permanentLimit, originalPermanentLimit, permanentLimitScaling, permanentLimitName);
            case APPARENT_POWER -> new ScaledApparentPowerLimits(permanentLimit, originalPermanentLimit, permanentLimitScaling, permanentLimitName);
            case CURRENT -> new ScaledCurrentLimits(permanentLimit, originalPermanentLimit, permanentLimitScaling, permanentLimitName);
            default -> throw new IllegalArgumentException(
                    String.format("Unsupported limits type for scalings (%s)", type));
        };
    }

    private AbstractScaledLoadingLimits initLow(LimitType type) {
        return switch (type) {
            case ACTIVE_POWER -> new ScaledActivePowerLimits();
            case APPARENT_POWER -> new ScaledApparentPowerLimits();
            case CURRENT -> new ScaledCurrentLimits();
            default -> throw new IllegalArgumentException(
                String.format("Unsupported limits type for scalings (%s)", type));
        };
    }
}
