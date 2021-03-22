/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.comparator;

import java.util.Comparator;
import java.util.Objects;

import com.powsybl.security.LimitViolation;
import com.powsybl.security.LimitViolationType;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class LimitViolationComparator implements Comparator<LimitViolation> {

    @Override
    public int compare(LimitViolation violation1, LimitViolation violation2) {
        Objects.requireNonNull(violation1);
        Objects.requireNonNull(violation2);
        if (violation1.getSubjectId().compareTo(violation2.getSubjectId()) == 0) {
            if (violation1.getLimitType().compareTo(violation2.getLimitType()) == 0) {
                if (LimitViolationType.CURRENT == violation1.getLimitType()) {
                    return violation1.getSide().compareTo(violation2.getSide());
                }
                return 0;
            }
            return violation1.getLimitType().compareTo(violation2.getLimitType());
        }
        return violation1.getSubjectId().compareTo(violation2.getSubjectId());
    }

}
