/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency.contingency.list.criterion;

import com.powsybl.iidm.network.*;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class CountryCriterion implements Criterion {

    private final Country country;
    private final Country country1;
    private final Country country2;

    public CountryCriterion(String country, String country1, String country2) {
        this.country = country != null ? Country.valueOf(country) : null;
        this.country1 = country1 != null ? Country.valueOf(country1) : null;
        this.country2 = country2 != null ? Country.valueOf(country2) : null;
    }

    @Override
    public CriterionType getType() {
        return CriterionType.COUNTRY;
    }

    @Override
    public boolean filter(Identifiable<?> identifiable, IdentifiableType type) {
        switch (type) {
            case LINE:
                return filterBranch(((Line) identifiable).getTerminal1(), ((Line) identifiable).getTerminal2());
            case HVDC_LINE:
                return filterBranch(((HvdcLine) identifiable).getConverterStation1().getTerminal(),
                        ((HvdcLine) identifiable).getConverterStation2().getTerminal());
            case DANGLING_LINE:
            case GENERATOR:
            case SWITCH:
            case LOAD:
            case SHUNT_COMPENSATOR:
            case STATIC_VAR_COMPENSATOR:
            case BUSBAR_SECTION:
                return filterInjection(((Injection) identifiable).getTerminal());
            case TWO_WINDINGS_TRANSFORMER:
                return filterSubstation(((TwoWindingsTransformer) identifiable).getNullableSubstation());
            case THREE_WINDINGS_TRANSFORMER:
                return filterSubstation(((ThreeWindingsTransformer) identifiable).getNullableSubstation());
            default:
                return false;
        }
    }

    private boolean filterSubstation(Substation substation) {
        if (substation == null) {
            return false;
        }
        Country injectionCountry = substation.getCountry().orElse(null);
        if (injectionCountry == null) {
            return false;
        }
        return country == null || injectionCountry == country;
    }

    private boolean filterInjection(Terminal terminal) {
        Substation substation = terminal.getVoltageLevel().getSubstation().orElse(null);
        if (substation == null) {
            return false;
        }
        Country injectionCountry = substation.getCountry().orElse(null);
        if (injectionCountry == null) {
            return false;
        }
        return country == null || injectionCountry == country;
    }

    private boolean filterBranch(Terminal terminal1, Terminal terminal2) {
        Substation substation1 = terminal1.getVoltageLevel().getSubstation().orElse(null);
        Substation substation2 = terminal2.getVoltageLevel().getSubstation().orElse(null);
        if (substation1 == null || substation2 == null) {
            return false;
        }
        Country countrySide1 = substation1.getCountry().orElse(null);
        Country countrySide2 = substation2.getCountry().orElse(null);
        if (countrySide1 == null || countrySide2 == null) {
            return false;
        }
        if (country1 == null && country2 == null) {
            return true;
        } else if (country1 == null) {
            return countrySide2 == country2 || countrySide1 == country2;
        } else if (country2 == null) {
            return countrySide2 == country1 || countrySide1 == country1;
        } else {
            return (countrySide1 == country1 && countrySide2 == country2) ||
                    (countrySide2 == country1 && countrySide1 == country2);
        }
    }

    public Country getCountry() {
        return country;
    }

    public Country getCountry1() {
        return country1;
    }

    public Country getCountry2() {
        return country2;
    }
}
