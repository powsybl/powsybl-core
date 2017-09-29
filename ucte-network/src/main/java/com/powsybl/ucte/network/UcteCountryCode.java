/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ucte.network;

import java.util.Objects;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
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
        switch (ucteCode) {
            case 'O': return AT;
            case 'A': return AL;
            case 'B': return BE;
            case 'V': return BG;
            case 'W': return BA;
            case '3': return BY;
            case 'S': return CH;
            case 'C': return CZ;
            case 'D': return DE;
            case 'K': return DK;
            case 'E': return ES;
            case 'F': return FR;
            case '5': return GB;
            case 'G': return GR;
            case 'M': return HU;
            case 'H': return HR;
            case 'I': return IT;
            case '1': return LU;
            case '6': return LT;
            case '2': return MA;
            case '7': return MD;
            case 'Y': return MK;
            case '9': return NO;
            case 'N': return NL;
            case 'P': return PT;
            case 'Z': return PL;
            case 'R': return RO;
            case '4': return RU;
            case '8': return SE;
            case 'Q': return SK;
            case 'L': return SI;
            case 'T': return TR;
            case 'U': return UA;
            case '0': return ME;
            case 'J': return RS;
            case 'X': return XX;
            default: throw new IllegalArgumentException("Unknown UCTE country code " + ucteCode);
        }
    }

}
