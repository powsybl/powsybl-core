/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package com.powsybl.contingency.dsl;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.contingency.Contingency;

/**
 * @author Paul Bui-Quang <paul.buiquang at rte-france.com>
 */
public class ProbabilityContingencyExtension extends AbstractExtension<Contingency> {
    private final Double probabilityBase;
    private final String probabilityTimeSeriesRef;

    public ProbabilityContingencyExtension(Double probabilityBase, String probabilityTimeSeriesRef) {
        this.probabilityBase = probabilityBase;
        this.probabilityTimeSeriesRef = probabilityTimeSeriesRef;
    }

    @Override
    public String getName() {
        return "probability";
    }

    public Double getProbabilityBase() {
        return probabilityBase;
    }

    public String getProbabilityTimeSeriesRef() {
        return probabilityTimeSeriesRef;
    }
}
