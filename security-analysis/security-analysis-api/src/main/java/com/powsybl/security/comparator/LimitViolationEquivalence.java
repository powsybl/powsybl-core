/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.comparator;

import java.util.Objects;

import com.google.common.base.Equivalence;
import com.powsybl.security.LimitViolation;
import com.powsybl.security.LimitViolationType;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class LimitViolationEquivalence extends Equivalence<LimitViolation> {

    private final double threshold;

    public LimitViolationEquivalence(double threshold) {
        this.threshold = threshold;
    }

    @Override
    protected boolean doEquivalent(LimitViolation violation1, LimitViolation violation2) {
        return violation1.getSubjectId().equals(violation2.getSubjectId())
               && violation1.getLimitType() == violation2.getLimitType()
               && Math.abs(violation1.getLimit() - violation2.getLimit()) <= threshold
               && (violation1.getLimitName() == null ? violation2.getLimitName() == null : violation1.getLimitName().equals(violation2.getLimitName()))
               && Math.abs(violation1.getAcceptableDuration() - violation2.getAcceptableDuration()) <= threshold
               && Math.abs(violation1.getLimitReduction() - violation2.getLimitReduction()) <= threshold
               && Math.abs(violation1.getValue() - violation2.getValue()) <= threshold
               && (violation1.getLimitType() == LimitViolationType.CURRENT ? violation1.getSide() == violation2.getSide() : true);
    }

    @Override
    protected int doHash(LimitViolation violation) {
        return Objects.hashCode(violation);
    }

}
