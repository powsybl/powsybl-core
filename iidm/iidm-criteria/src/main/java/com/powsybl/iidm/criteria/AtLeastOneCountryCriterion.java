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
 * <p>Criterion checking that one of the sides of the network element belongs to a country defined in a list.</p>
 * <p>When {@link #filter(NetworkElement, ThreeSides)}} is called with a non-null <code>side</code>, only the country
 * on this particular side is checked.</p>
 * @author Olivier Perrin {@literal <olivier.perrin@rte-france.com>}
 */
public class AtLeastOneCountryCriterion implements Criterion {
    private final List<Country> countries;

    public AtLeastOneCountryCriterion(List<Country> countries) {
        Objects.requireNonNull(countries);
        this.countries = ImmutableList.copyOf(countries);
    }

    @Override
    public CriterionType getType() {
        return CriterionType.AT_LEAST_ONE_COUNTRY;
    }

    @Override
    public boolean filter(Identifiable<?> identifiable, IdentifiableType type) {
        return filterWithCountries(getCountriesToCheck(identifiable, type));
    }

    @Override
    public boolean filter(NetworkElement networkElement) {
        return filter(networkElement, null);
    }

    @Override
    public boolean filter(NetworkElement networkElement, ThreeSides side) {
        return filterWithCountries(getCountriesToCheck(networkElement, side));
    }

    public List<Country> getCountries() {
        return countries;
    }

    private List<Country> getCountriesToCheck(Identifiable<?> identifiable, IdentifiableType type) {
        if (type == IdentifiableType.LINE || type == IdentifiableType.HVDC_LINE || type == IdentifiableType.TIE_LINE) {
            return TwoCountriesCriterion.getCountries(identifiable, type);
        } else {
            return Collections.singletonList(SingleCountryCriterion.getCountry(identifiable, type));
        }
    }

    private List<Country> getCountriesToCheck(NetworkElement networkElement, ThreeSides side) {
        return side != null ?
                Collections.singletonList(networkElement.getCountry(side).orElse(null)) :
                Arrays.asList(networkElement.getCountry1().orElse(null),
                        networkElement.getCountry2().orElse(null),
                        networkElement.getCountry3().orElse(null));
    }

    private boolean filterWithCountries(List<Country> countriesToCheck) {
        return countries.isEmpty() || countriesToCheck.stream().filter(Objects::nonNull).anyMatch(countries::contains);
    }
}
