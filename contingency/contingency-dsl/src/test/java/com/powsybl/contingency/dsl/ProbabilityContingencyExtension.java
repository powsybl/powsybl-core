/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package com.powsybl.contingency.dsl;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.contingency.Contingency;

/**
 * @author Paul Bui-Quang <paul.buiquang at rte-france.com>
 */
public class ProbabilityContingencyExtension implements Extension<Contingency> {
    private Contingency contingency;

    private Double probabilityBase = 1.0;
    private String probabilityTimeSeriesRef;

    @Override
    public String getName() {
        return "probability";
    }

    @Override
    public Contingency getExtendable() {
        return contingency;
    }

    @Override
    public void setExtendable(Contingency extendable) {
        this.contingency = extendable;
    }

    public double getProbabilityBase() {
        return probabilityBase;
    }

    public String getProbabilityTimeSeriesRef() {
        return probabilityTimeSeriesRef;
    }
}
