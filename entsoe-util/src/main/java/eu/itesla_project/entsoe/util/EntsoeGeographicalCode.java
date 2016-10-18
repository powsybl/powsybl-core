/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.entsoe.util;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import eu.itesla_project.iidm.network.Country;

import java.util.Collection;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public enum EntsoeGeographicalCode {
    AT(Country.AT),
    BE(Country.BE),
    CH(Country.CH),
    CZ(Country.CZ),
    D1(Country.DE),
    D2(Country.DE),
    D4(Country.DE),
    D7(Country.DE),
    D8(Country.DE),
    FR(Country.FR),
    HR(Country.HR),
    IT(Country.IT),
    NL(Country.NL),
    PL(Country.PL),
    SI(Country.SI),
    NO(Country.NO), // not a real UCTE geo code but necessary to work with nordic
    UX(null);

    private static Multimap<Country, EntsoeGeographicalCode> COUNTRY_TO_GEOGRAPHICAL_CODES;

    private static final Lock LOCK = new ReentrantLock();

    public static Collection<EntsoeGeographicalCode> forCountry(Country country) {
        LOCK.lock();
        try {
            if (COUNTRY_TO_GEOGRAPHICAL_CODES == null) {
                COUNTRY_TO_GEOGRAPHICAL_CODES = HashMultimap.create();
                for (EntsoeGeographicalCode geographicalCode : EntsoeGeographicalCode.values()) {
                    COUNTRY_TO_GEOGRAPHICAL_CODES.put(geographicalCode.getCountry(), geographicalCode);
                }
            }
        } finally {
            LOCK.unlock();
        }
        return COUNTRY_TO_GEOGRAPHICAL_CODES.get(country);
    }

    private final Country country;

    EntsoeGeographicalCode(Country country) {
        this.country = country;
    }

    public Country getCountry() {
        return country;
    }
}
