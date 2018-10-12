/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.afs;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Country;
import com.powsybl.security.LimitViolation;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SubjectInfoExtension extends AbstractExtension<LimitViolation> {

    private final Set<Country> countries;

    private final Set<Double> nominalVoltages;

    public SubjectInfoExtension(Country country, double nominalVoltage) {
        this(Collections.singleton(Objects.requireNonNull(country)), Collections.singleton(nominalVoltage));
    }

    public SubjectInfoExtension(Set<Country> countries, Set<Double> nominalVoltages) {
        this.countries = Objects.requireNonNull(countries);
        this.nominalVoltages = Objects.requireNonNull(nominalVoltages);
    }

    @Override
    public String getName() {
        return "SubjectInfo";
    }

    public Set<Country> getCountries() {
        return countries;
    }

    public Set<Double> getNominalVoltages() {
        return nominalVoltages;
    }
}
