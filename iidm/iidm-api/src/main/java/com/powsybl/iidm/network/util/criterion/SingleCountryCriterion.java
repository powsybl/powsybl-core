/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util.criterion;

import com.google.common.collect.ImmutableList;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.util.criterion.translation.NetworkElement;

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
        return switch (type) {
            case DANGLING_LINE, GENERATOR, LOAD, SHUNT_COMPENSATOR, STATIC_VAR_COMPENSATOR, BUSBAR_SECTION, BATTERY ->
                    filterVoltageLevel(((Injection<?>) identifiable).getTerminal().getVoltageLevel());
            case SWITCH -> filterVoltageLevel(((Switch) identifiable).getVoltageLevel());
            case TWO_WINDINGS_TRANSFORMER ->
                    filterSubstation(((TwoWindingsTransformer) identifiable).getNullableSubstation());
            case THREE_WINDINGS_TRANSFORMER ->
                    filterSubstation(((ThreeWindingsTransformer) identifiable).getNullableSubstation());
            default -> false;
        };
    }

    @Override
    public boolean filter(NetworkElement<?> networkElement) {
        return filterWithCountry(networkElement.getCountry());
    }

    public List<Country> getCountries() {
        return countries;
    }

    private boolean filterSubstation(Substation substation) {
        if (substation == null) {
            return false;
        }
        Country country = substation.getCountry().orElse(null);
        return filterWithCountry(country);
    }

    private boolean filterVoltageLevel(VoltageLevel voltageLevel) {
        Substation substation = voltageLevel.getSubstation().orElse(null);
        return filterSubstation(substation);
    }

    private boolean filterWithCountry(Country country) {
        if (country == null && !countries.isEmpty()) {
            return false;
        }
        return countries.isEmpty() || countries.contains(country);
    }
}
