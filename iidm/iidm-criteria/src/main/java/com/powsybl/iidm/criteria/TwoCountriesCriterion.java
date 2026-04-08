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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
public class TwoCountriesCriterion implements Criterion {

    private final List<Country> countries1;
    private final List<Country> countries2;

    public TwoCountriesCriterion(List<Country> countries) {
        this(countries, Collections.emptyList());
    }

    public TwoCountriesCriterion(List<Country> countries1, List<Country> countries2) {
        Objects.requireNonNull(countries1);
        Objects.requireNonNull(countries2);
        this.countries1 = ImmutableList.copyOf(countries1);
        this.countries2 = ImmutableList.copyOf(countries2);
    }

    @Override
    public CriterionType getType() {
        return CriterionType.TWO_COUNTRY;
    }

    @Override
    public boolean filter(Identifiable<?> identifiable, IdentifiableType type) {
        List<Country> countries = getCountries(identifiable, type);
        return countries.size() == 2 && filterWithCountries(countries.get(0), countries.get(1));
    }

    protected static List<Country> getCountries(Identifiable<?> identifiable, IdentifiableType type) {
        return switch (type) {
            case LINE, TIE_LINE -> getCountries(((Branch<?>) identifiable).getTerminal1(), ((Branch<?>) identifiable).getTerminal2());
            case HVDC_LINE -> getCountries(((HvdcLine) identifiable).getConverterStation1().getTerminal(),
                    ((HvdcLine) identifiable).getConverterStation2().getTerminal());
            default -> List.of();
        };
    }

    @Override
    public boolean filter(NetworkElement networkElement) {
        Country countrySide1 = networkElement.getCountry1().orElse(null);
        Country countrySide2 = networkElement.getCountry2().orElse(null);
        return filterWithCountries(countrySide1, countrySide2);
    }

    private boolean filterWithCountries(Country countrySide1, Country countrySide2) {
        if (countrySide1 == null && !countries1.isEmpty() || countrySide2 == null && !countries2.isEmpty()) {
            return false;
        }
        return countries1.isEmpty() && countries2.isEmpty()
                || countries1.isEmpty() && (countries2.contains(countrySide2) || countries2.contains(countrySide1))
                || countries2.isEmpty() && (countries1.contains(countrySide2) || countries1.contains(countrySide1))
                || countries1.contains(countrySide1) && countries2.contains(countrySide2)
                || countries1.contains(countrySide2) && countries2.contains(countrySide1);
    }

    private static List<Country> getCountries(Terminal terminal1, Terminal terminal2) {
        return Arrays.asList(getCountry(terminal1), getCountry(terminal2));
    }

    private static Country getCountry(Terminal terminal) {
        return terminal.getVoltageLevel().getSubstation().flatMap(Substation::getCountry).orElse(null);
    }

    public List<Country> getCountries1() {
        return countries1;
    }

    public List<Country> getCountries2() {
        return countries2;
    }
}
