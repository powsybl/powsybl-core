/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.criteria;

import com.google.common.collect.ImmutableList;
import com.powsybl.iidm.criteria.translation.NetworkElement;
import com.powsybl.iidm.network.*;

import java.util.List;
import java.util.Objects;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
public class SingleCountryCriterion implements Criterion {

    private final List<Country> countries;

    public SingleCountryCriterion(List<Country> countries) {
        Objects.requireNonNull(countries);
        this.countries = ImmutableList.copyOf(countries);
    }

    @Override
    public CriterionType getType() {
        return CriterionType.SINGLE_COUNTRY;
    }

    @Override
    public boolean filter(Identifiable<?> identifiable, IdentifiableType type) {
        Country country = getCountry(identifiable, type);
        return filterWithCountry(country);
    }

    @Override
    public boolean filter(NetworkElement networkElement) {
        return filterWithCountry(networkElement.getCountry().orElse(null));
    }

    public List<Country> getCountries() {
        return countries;
    }

    protected static Country getCountry(Identifiable<?> identifiable, IdentifiableType type) {
        // TODO DcConverter
        return switch (type) {
            case DANGLING_LINE, GENERATOR, LOAD, SHUNT_COMPENSATOR, STATIC_VAR_COMPENSATOR, BUSBAR_SECTION, BATTERY, HVDC_CONVERTER_STATION ->
                    getCountry(((Injection<?>) identifiable).getTerminal().getVoltageLevel());
            case SWITCH -> getCountry(((Switch) identifiable).getVoltageLevel());
            case TWO_WINDINGS_TRANSFORMER ->
                    getCountry(((TwoWindingsTransformer) identifiable).getNullableSubstation());
            case THREE_WINDINGS_TRANSFORMER ->
                    getCountry(((ThreeWindingsTransformer) identifiable).getNullableSubstation());
            default -> null;
        };
    }

    private static Country getCountry(Substation substation) {
        if (substation == null) {
            return null;
        }
        return substation.getCountry().orElse(null);
    }

    private static Country getCountry(VoltageLevel voltageLevel) {
        return voltageLevel.getSubstation().map(SingleCountryCriterion::getCountry).orElse(null);
    }

    private boolean filterWithCountry(Country country) {
        if (country == null && !countries.isEmpty()) {
            return false;
        }
        return countries.isEmpty() || countries.contains(country);
    }
}
