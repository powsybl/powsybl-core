/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 *
 */
package com.powsybl.iidm.network.util;

import com.powsybl.iidm.network.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public final class ExchangesUtil {

    private ExchangesUtil() {
        // static utility class
    }

    /**
     * Compute the exchanges between zones
     * @param network: the network for which we want to compute the zone exchanges
     * @param zones: key is the zone name, value is the set of voltage levels belonging to the zone
     * @param referenceZone: the zone for which we want to compute the exchanges
     * @return a list of ZoneExchange objects representing the exchanges between the reference zone and the other zones
     */
    public static List<ZoneExchange> compute(Network network, Map<String, Set<String>> zones, String referenceZone) {
        //TODO
        return null;
    }

    /**
     * Compute the exchanges between countries
     * @param network: the network for which we want to compute the country exchanges
     * @param referenceCountry: the country for which we want to compute the exchanges
     * @return a list of CountryExchange objects representing the exchanges between the reference country and the other countries
     */
    public static List<CountryExchange> compute(Network network, Country referenceCountry) {
        List<CountryExchange> countryExchanges = new ArrayList<>();
        Set<Country> countries = network.getVoltageLevelStream()
                .map(vl -> vl.getSubstation().flatMap(Substation::getCountry).orElse(null))
                .filter(c -> c != referenceCountry)
                .collect(Collectors.toSet());

        for (Country otherCountry : countries) {
            double totalP = 0;

            List<Line> lines = network.getLineStream()
                    .filter(l -> isBetween(l, referenceCountry, otherCountry))
                    .toList();
            totalP += lines.stream().mapToDouble(l -> getBranchActivePower(referenceCountry, l.getTerminal1(), l.getTerminal2())).sum();

            List<TieLine> tieLines = network.getTieLineStream()
                    .filter(tl -> isBetween(tl, referenceCountry, otherCountry))
                    .toList();
            totalP += tieLines.stream().mapToDouble(tl -> getBranchActivePower(referenceCountry, tl.getTerminal1(), tl.getTerminal2())).sum();

            List<HvdcLine> hvdcLines = network.getHvdcLineStream()
                    .filter(h -> isBetween(h, referenceCountry, otherCountry))
                    .toList();
            totalP += hvdcLines.stream().mapToDouble(h -> getBranchActivePower(referenceCountry, h.getConverterStation1().getTerminal(), h.getConverterStation2().getTerminal())).sum();

            countryExchanges.add(new CountryExchange(referenceCountry, otherCountry, totalP));
        }

        return countryExchanges;
    }

    private static boolean isBetween(Branch<?> branch, Country c1, Country c2) {
        Country country1 = branch.getTerminal1().getVoltageLevel().getSubstation().flatMap(Substation::getCountry).orElse(null);
        Country country2 = branch.getTerminal2().getVoltageLevel().getSubstation().flatMap(Substation::getCountry).orElse(null);
        return country1 == c1 && country2 == c2
                || country1 == c2 && country2 == c1;
    }

    private static boolean isBetween(HvdcLine hvdcLine, Country c1, Country c2) {
        Country country1 = hvdcLine.getConverterStation1().getTerminal().getVoltageLevel().getSubstation().flatMap(Substation::getCountry).orElse(null);
        Country country2 = hvdcLine.getConverterStation2().getTerminal().getVoltageLevel().getSubstation().flatMap(Substation::getCountry).orElse(null);
        return country1 == c1 && country2 == c2
                || country1 == c2 && country2 == c1;
    }

    private static double getBranchActivePower(Country referenceCountry, Terminal terminal1, Terminal terminal2) {
        Country countryTerminal1 = terminal1.getVoltageLevel().getSubstation()
                .flatMap(Substation::getCountry).orElse(null);
        double p = (referenceCountry == countryTerminal1 ? terminal1 : terminal2).getP();
        return Double.isNaN(p) ? 0.0 : p;
    }
}
