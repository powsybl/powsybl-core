/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security;

import com.powsybl.commons.extensions.AbstractExtendable;
import com.powsybl.iidm.network.*;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class LimitViolation extends AbstractExtendable<LimitViolation> {

    private final String subjectId;

    private final LimitViolationType limitType;

    private final double limit;

    private final String limitName;

    private final int acceptableDuration;

    private final float limitReduction;

    private final double value;

    private final Branch.Side side;

    public LimitViolation(String subjectId, LimitViolationType limitType, String limitName, int acceptableDuration,
                          double limit, float limitReduction, double value, Branch.Side side) {
        this.subjectId = Objects.requireNonNull(subjectId);
        this.limitType = Objects.requireNonNull(limitType);
        this.limitName = limitName;
        this.acceptableDuration = acceptableDuration;
        this.limit = limit;
        this.limitReduction = limitReduction;
        this.value = value;
        this.side = checkSide(limitType, side);
    }

    public LimitViolation(String subjectId, LimitViolationType limitType, double limit, float limitReduction, double value) {
        this(subjectId, limitType, null, Integer.MAX_VALUE, limit, limitReduction, value, null);
    }

    public String getSubjectId() {
        return subjectId;
    }

    public LimitViolationType getLimitType() {
        return limitType;
    }

    public double getLimit() {
        return limit;
    }

    public String getLimitName() {
        return limitName;
    }

    public int getAcceptableDuration() {
        return acceptableDuration;
    }

    public float getLimitReduction() {
        return limitReduction;
    }

    public double getValue() {
        return value;
    }

    public Branch.Side getSide() {
        return side;
    }

    private static Branch.Side checkSide(LimitViolationType limitType, Branch.Side side) {
        if (limitType == LimitViolationType.CURRENT) {
            return Objects.requireNonNull(side);
        } else {
            return null;
        }
    }
}
