/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.afs;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Country;
import com.powsybl.security.LimitViolation;

import java.util.Objects;
import java.util.Set;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SubjectInfoExtension implements Extension<LimitViolation> {

    private LimitViolation limitViolation;

    private final Set<Double> nominalVoltages;

    private final Set<Country> countries;

    public SubjectInfoExtension(Set<Double> nominalVoltages, Set<Country> countries) {
        this.nominalVoltages = Objects.requireNonNull(nominalVoltages);
        this.countries = Objects.requireNonNull(countries);
    }

    @Override
    public String getName() {
        return "SubjectInfo";
    }

    @Override
    public LimitViolation getExtendable() {
        return limitViolation;
    }

    @Override
    public void setExtendable(LimitViolation limitViolation) {
        this.limitViolation = limitViolation;
    }

    public Set<Double> getNominalVoltages() {
        return nominalVoltages;
    }

    public Set<Country> getCountries() {
        return countries;
    }
}
