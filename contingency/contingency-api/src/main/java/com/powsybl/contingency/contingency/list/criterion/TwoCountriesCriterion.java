/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency.contingency.list.criterion;

import com.google.common.collect.ImmutableList;
import com.powsybl.iidm.network.*;

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
        switch (type) {
            case LINE:
                return filterBranch(((Line) identifiable).getTerminal1(), ((Line) identifiable).getTerminal2());
            case HVDC_LINE:
                return filterBranch(((HvdcLine) identifiable).getConverterStation1().getTerminal(),
                        ((HvdcLine) identifiable).getConverterStation2().getTerminal());
            default:
                return false;
        }
    }

    private boolean filterBranch(Terminal terminal1, Terminal terminal2) {
        Substation substation1 = terminal1.getVoltageLevel().getSubstation().orElse(null);
        Substation substation2 = terminal2.getVoltageLevel().getSubstation().orElse(null);
        if (substation1 == null || substation2 == null) {
            return false;
        }
        Country countrySide1 = substation1.getCountry().orElse(null);
        Country countrySide2 = substation2.getCountry().orElse(null);
        if (countrySide1 == null && !countries1.isEmpty() || countrySide2 == null && !countries2.isEmpty()) {
            return false;
        }
        return countries1.isEmpty() && countries2.isEmpty()
                || countries1.isEmpty() && (countries2.contains(countrySide2) || countries2.contains(countrySide1))
                || countries2.isEmpty() && (countries1.contains(countrySide2) || countries1.contains(countrySide1))
                || countries1.contains(countrySide1) && countries2.contains(countrySide2)
                || countries1.contains(countrySide2) && countries2.contains(countrySide1);
    }

    public List<Country> getCountries1() {
        return countries1;
    }

    public List<Country> getCountries2() {
        return countries2;
    }
}
