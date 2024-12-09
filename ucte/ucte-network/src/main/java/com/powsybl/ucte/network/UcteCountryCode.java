/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ucte.network;

import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.VoltageLevel;

import java.util.Objects;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public enum UcteCountryCode {
    AT('O', "Austria"),
    AL('A', "Albania"),
    BE('B', "Belgium"),
    BG('V', "Bulgaria"),
    BA('W', "Bosnia and Herzegovina"),
    BY('3', "Belarus"),
    CH('S', "Switzerland"),
    CZ('C', "Czech Republic"),
    DE('D', "Germany"),
    DK('K', "Denmark"),
    ES('E', "Spain"),
    FR('F', "France"),
    GB('5', "Great Britain"),
    GR('G', "Greece"),
    HU('M', "Hungary"),
    HR('H', "Croatia"),
    IT('I', "Italy"),
    LU('1', "Luxemburg"),
    LT('6', "Lithuania"),
    MA('2', "Morocco"),
    MD('7', "Moldavia"),
    MK('Y', "FYROM"),
    NO('9', "Norway"),
    NL('N', "Netherlands"),
    PT('P', "Portugal"),
    PL('Z', "Poland"),
    RO('R', "Romania"),
    RU('4', "Russia"),
    SE('8', "Sweden"),
    SK('Q', "Slovakia"),
    SI('L', "Slovenia"),
    TR('T', "Turkey"),
    UA('U', "Ukraine"),
    ME('0', "Montenegro"),
    RS('J', "Serbia"),
    KS('_', "Kosovo"),
    XX('X', "Fictitious border node");

    private final char ucteCode;

    private final String prettyName;

    UcteCountryCode(char ucteCode, String prettyName) {
        this.ucteCode = ucteCode;
        this.prettyName = Objects.requireNonNull(prettyName);
    }

    public char getUcteCode() {
        return ucteCode;
    }

    public String getPrettyName() {
        return prettyName;
    }

    public static UcteCountryCode fromUcteCode(char ucteCode) {
        return switch (ucteCode) {
            case 'O' -> AT;
            case 'A' -> AL;
            case 'B' -> BE;
            case 'V' -> BG;
            case 'W' -> BA;
            case '3' -> BY;
            case 'S' -> CH;
            case 'C' -> CZ;
            case 'D' -> DE;
            case 'K' -> DK;
            case 'E' -> ES;
            case 'F' -> FR;
            case '5' -> GB;
            case 'G' -> GR;
            case 'M' -> HU;
            case 'H' -> HR;
            case 'I' -> IT;
            case '1' -> LU;
            case '6' -> LT;
            case '2' -> MA;
            case '7' -> MD;
            case 'Y' -> MK;
            case '9' -> NO;
            case 'N' -> NL;
            case 'P' -> PT;
            case 'Z' -> PL;
            case 'R' -> RO;
            case '4' -> RU;
            case '8' -> SE;
            case 'Q' -> SK;
            case 'L' -> SI;
            case 'T' -> TR;
            case 'U' -> UA;
            case '0' -> ME;
            case 'J' -> RS;
            case '_' -> KS;
            case 'X' -> XX;
            default -> throw new IllegalArgumentException("Unknown UCTE country code " + ucteCode);
        };
    }

    public static boolean isUcteCountryCode(char character) {
        try {
            UcteCountryCode.fromUcteCode(character);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static UcteCountryCode fromVoltagelevel(VoltageLevel voltageLevel) {
        Country country = voltageLevel.getSubstation()
                .flatMap(Substation::getCountry)
                .orElseThrow(() -> new UcteException("No UCTE country found for substation"));
        try {
            return UcteCountryCode.valueOf(country.name());
        } catch (IllegalArgumentException e) {
            throw new UcteException(String.format("No UCTE country found for %s",country.name()));
        }
    }

}
