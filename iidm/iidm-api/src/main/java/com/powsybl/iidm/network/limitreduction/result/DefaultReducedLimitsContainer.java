/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.limitreduction.result;

import com.powsybl.iidm.network.LoadingLimits;

import java.util.Optional;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public class DefaultReducedLimitsContainer extends AbstractReducedLimitsContainer<AbstractReducedLoadingLimits, LoadingLimits> {

    public DefaultReducedLimitsContainer(AbstractReducedLoadingLimits limits, LoadingLimits originalLimits) {
        super(limits, originalLimits);
    }

    public Details getDetailsForPermanentLimit() {
        return new Details(getLimits().getOriginalPermanentLimit(), getLimits().getReductionAppliedOnPermanentLimit());
    }

    public Details getDetailsForTemporaryLimit(int acceptableDuration) {
        return Optional.ofNullable(getLimits().getTemporaryLimit(acceptableDuration))
                .map(AbstractReducedLoadingLimits.ReducedTemporaryLimit.class::cast)
                .map(l -> new Details(l.getOriginalValue(), l.getAppliedReduction()))
                .orElse(null);
    }
}
