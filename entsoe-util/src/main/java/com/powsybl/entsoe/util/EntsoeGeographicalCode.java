/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.entsoe.util;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.powsybl.iidm.network.Country;

import java.util.Collection;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public enum EntsoeGeographicalCode {
    // see. http://cimug.ucaiug.org/Groups/Model%20Exchange/UCTE-format.pdf
    AL(Country.AL),
    AT(Country.AT),
    BA(Country.BA),
    BE(Country.BE),
    BG(Country.BG),
    BY(Country.BY),
    CH(Country.CH),
    CZ(Country.CZ),
    DE(Country.DE),
    D1(Country.DE),
    D2(Country.DE),
    D4(Country.DE),
    D7(Country.DE),
    D8(Country.DE),
    DK(Country.DK),
    ES(Country.ES),
    FR(Country.FR),
    GB(Country.GB),
    GR(Country.GR),
    HR(Country.HR),
    HU(Country.HU),
    IT(Country.IT),
    LU(Country.LU),
    LT(Country.LT),
    MA(Country.MA),
    MD(Country.MD),
    ME(Country.ME),
    MK(Country.MK),
    NL(Country.NL),
    NO(Country.NO),
    PL(Country.PL),
    PT(Country.PT),
    RO(Country.RO),
    RS(Country.RS),
    RU(Country.RU),
    SE(Country.SE),
    SK(Country.SK),
    SI(Country.SI),
    TR(Country.TR),
    UA(Country.UA),
    UC(null),
    UX(null);

    private static Multimap<Country, EntsoeGeographicalCode> countryToGeographicalCodes;

    private static final Lock LOCK = new ReentrantLock();

    public static Collection<EntsoeGeographicalCode> forCountry(Country country) {
        LOCK.lock();
        try {
            if (countryToGeographicalCodes == null) {
                countryToGeographicalCodes = HashMultimap.create();
                for (EntsoeGeographicalCode geographicalCode : EntsoeGeographicalCode.values()) {
                    countryToGeographicalCodes.put(geographicalCode.getCountry(), geographicalCode);
                }
            }
        } finally {
            LOCK.unlock();
        }
        return countryToGeographicalCodes.get(country);
    }

    private final Country country;

    EntsoeGeographicalCode(Country country) {
        this.country = country;
    }

    public Country getCountry() {
        return country;
    }
}
