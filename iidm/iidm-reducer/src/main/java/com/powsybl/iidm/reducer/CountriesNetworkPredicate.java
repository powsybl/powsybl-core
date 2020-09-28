/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.reducer;

import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.VoltageLevel;

import java.util.*;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
public class CountriesNetworkPredicate implements NetworkPredicate {
    private final Set<Country> keptCountries = new HashSet<>();

    public static CountriesNetworkPredicate of(Country... keptCountries) {
        Objects.requireNonNull(keptCountries);
        return new CountriesNetworkPredicate(Arrays.asList(keptCountries));
    }

    public CountriesNetworkPredicate(Collection<Country> keptCountries) {
        Objects.requireNonNull(keptCountries);
        this.keptCountries.addAll(keptCountries);
    }

    @Override
    public boolean test(Substation substation) {
        Optional<Country> optionalCountry = substation.getCountry();
        return optionalCountry.isPresent() && keptCountries.contains(optionalCountry.get());
    }

    @Override
    public boolean test(VoltageLevel voltageLevel) {
        Optional<Country> optionalCountry = voltageLevel.getSubstation().getCountry();
        return optionalCountry.isPresent() && keptCountries.contains(optionalCountry.get());
    }
}
