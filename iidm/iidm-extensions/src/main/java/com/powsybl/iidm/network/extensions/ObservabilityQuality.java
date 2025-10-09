/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.extensions;

import java.util.Optional;

/**
 * Quality information.
 *
 * @author Thomas Adam {@literal <tadam at silicom.fr>}
 */
public interface ObservabilityQuality<T> {

    /**
     * Required value.
     * @return standard deviation value.
     */
    double getStandardDeviation();

    ObservabilityQuality<T> setStandardDeviation(double standardDeviation);

    /**
     * Value optional. Can be empty.
     */
    Optional<Boolean> isRedundant();

    ObservabilityQuality<T> setRedundant(Boolean redundant);
}
