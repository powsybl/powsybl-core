/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.criteria.translation;

import com.powsybl.iidm.criteria.NetworkElementCriterion;
import com.powsybl.iidm.network.Country;

import java.util.Optional;

/**
 * <p>Interface that should implement objects representing network elements in order for them to be processable by
 * the {@link NetworkElementCriterion} inheriting criterion classes.</p>
 * @author Sophie Frasnedo {@literal <sophie.frasnedo at rte-france.com>}
 */
public interface NetworkElement {

    String getId();

    Optional<Country> getCountry1();

    Optional<Country> getCountry2();

    Optional<Country> getCountry();

    Optional<Double> getNominalVoltage1();

    Optional<Double> getNominalVoltage2();

    Optional<Double> getNominalVoltage3();

    Optional<Double> getNominalVoltage();

    boolean isValidFor(NetworkElementCriterion.NetworkElementCriterionType networkElementCriterionType);
}
