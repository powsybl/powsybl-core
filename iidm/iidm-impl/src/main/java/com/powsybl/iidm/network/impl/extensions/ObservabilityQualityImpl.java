/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.iidm.network.extensions.ObservabilityQuality;

import java.util.Optional;

/**
 * @author Thomas Adam {@literal <tadam at silicom.fr>}
 */
public class ObservabilityQualityImpl<T> implements ObservabilityQuality<T> {

    private double standardDeviation;

    private Boolean redundant = null;

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
    public Optional<Boolean> isRedundant() {
        return Optional.ofNullable(redundant);
    }

    @Override
    public ObservabilityQuality<T> setRedundant(Boolean redundant) {
        this.redundant = redundant;
        return this;
    }
}
