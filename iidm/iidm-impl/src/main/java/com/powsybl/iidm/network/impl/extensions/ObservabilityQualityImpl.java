/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.iidm.network.extensions.ObservabilityQuality;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class ObservabilityQualityImpl<T> implements ObservabilityQuality<T> {

    private double standardDeviation;

    private boolean redundant = false;

    public ObservabilityQualityImpl(double standardDeviation, Boolean redundant) {
        this.standardDeviation = standardDeviation;
        this.redundant = redundant;
    }

    public ObservabilityQualityImpl(double standardDeviation) {
        this.standardDeviation = standardDeviation;
    }

    @Override
    public double getStandardDeviation() {
        return standardDeviation;
    }

    @Override
    public ObservabilityQuality<T> setStandardDeviation(double standardDeviation) {
        this.standardDeviation = standardDeviation;
        return this;
    }

    @Override
    public boolean isRedundant() {
        return redundant;
    }

    @Override
    public ObservabilityQuality<T> setRedundant(boolean redundant) {
        this.redundant = redundant;
        return this;
    }
}
