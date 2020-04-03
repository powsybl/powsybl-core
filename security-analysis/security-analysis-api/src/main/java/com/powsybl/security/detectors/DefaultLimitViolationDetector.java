/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.detectors;

import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.util.LimitViolationUtils;
import com.powsybl.security.LimitViolation;
import com.powsybl.security.LimitViolationType;
import com.powsybl.security.Security;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Implements the default behaviour for limit violation detection.
 *
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public class DefaultLimitViolationDetector extends AbstractContingencyBlindDetector {

    private final float limitReduction;
    private final Set<Security.CurrentLimitType> currentLimitTypes;

    public DefaultLimitViolationDetector(float limitReduction, Collection<Security.CurrentLimitType> currentLimitTypes) {
        if (limitReduction <= 0) {
            throw new IllegalArgumentException("Bad limit reduction " + limitReduction);
        }
        this.limitReduction = limitReduction;
        this.currentLimitTypes = EnumSet.copyOf(Objects.requireNonNull(currentLimitTypes));
    }

    public DefaultLimitViolationDetector(Collection<Security.CurrentLimitType> currentLimitTypes) {
        this(1.0f, currentLimitTypes);
    }

    public DefaultLimitViolationDetector() {
        this(EnumSet.allOf(Security.CurrentLimitType.class));
    }

    private void checkPermanentLimit(Branch branch, Branch.Side side, double value, Consumer<LimitViolation> consumer) {

        if (LimitViolationUtils.checkPermanentLimit(branch, side, limitReduction, value)) {
            consumer.accept(new LimitViolation(branch.getId(),
                    ((Branch<?>) branch).getOptionalName().orElse(null),
                    LimitViolationType.CURRENT,
                    null,
                    Integer.MAX_VALUE,
                    branch.getCurrentLimits(side).getPermanentLimit(),
                    limitReduction,
                    value,
                    side));
        }
    }

    @Override
    public void checkCurrent(Branch branch, Branch.Side side, double value, Consumer<LimitViolation> consumer) {

        Branch.Overload overload = LimitViolationUtils.checkTemporaryLimits(branch, side, limitReduction, value);

        if (currentLimitTypes.contains(Security.CurrentLimitType.TATL) && (overload != null)) {
            consumer.accept(new LimitViolation(branch.getId(),
                    ((Branch<?>) branch).getOptionalName().orElse(null),
                    LimitViolationType.CURRENT,
                    overload.getPreviousLimitName(),
                    overload.getTemporaryLimit().getAcceptableDuration(),
                    overload.getPreviousLimit(),
                    limitReduction,
                    value,
                    side));
        } else if (currentLimitTypes.contains(Security.CurrentLimitType.PATL)) {
            checkPermanentLimit(branch, side, value, consumer);
        }
    }

    @Override
    public void checkVoltage(Bus bus, double value, Consumer<LimitViolation> consumer) {
        VoltageLevel vl = bus.getVoltageLevel();
        if (!Double.isNaN(vl.getLowVoltageLimit()) && value <= vl.getLowVoltageLimit()) {
            consumer.accept(new LimitViolation(vl.getId(), vl.getOptionalName().orElse(null), LimitViolationType.LOW_VOLTAGE,
                    vl.getLowVoltageLimit(), limitReduction, value));
        }

        if (!Double.isNaN(vl.getHighVoltageLimit()) && value >= vl.getHighVoltageLimit()) {
            consumer.accept(new LimitViolation(vl.getId(), vl.getOptionalName().orElse(null), LimitViolationType.HIGH_VOLTAGE,
                    vl.getHighVoltageLimit(), limitReduction, value));
        }
    }

}
