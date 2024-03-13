/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.criteria;

import com.google.common.collect.ImmutableList;
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
        return switch (type) {
            case DANGLING_LINE, GENERATOR, LOAD, SHUNT_COMPENSATOR, STATIC_VAR_COMPENSATOR, BUSBAR_SECTION, BATTERY ->
                    filterInjection(((Injection<?>) identifiable).getTerminal().getVoltageLevel());
            case SWITCH -> filterInjection(((Switch) identifiable).getVoltageLevel());
            case TWO_WINDINGS_TRANSFORMER ->
                    filterSubstation(((TwoWindingsTransformer) identifiable).getNullableSubstation());
            case THREE_WINDINGS_TRANSFORMER ->
                    filterSubstation(((ThreeWindingsTransformer) identifiable).getNullableSubstation());
            default -> false;
        };
    }

    public List<Country> getCountries() {
        return countries;
    }

    private boolean filterSubstation(Substation substation) {
        if (substation == null) {
            return false;
        }
        Country injectionCountry = substation.getCountry().orElse(null);
        if (injectionCountry == null && !countries.isEmpty()) {
            return false;
        }
        return countries.isEmpty() || countries.contains(injectionCountry);
    }

    private boolean filterInjection(VoltageLevel voltageLevel) {
        Substation substation = voltageLevel.getSubstation().orElse(null);
        if (substation == null) {
            return false;
        }
        Country injectionCountry = substation.getCountry().orElse(null);
        if (injectionCountry == null && !countries.isEmpty()) {
            return false;
        }
        return countries.isEmpty() || countries.contains(injectionCountry);
    }
}
