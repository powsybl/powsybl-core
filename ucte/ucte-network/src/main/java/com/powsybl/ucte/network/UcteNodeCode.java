/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ucte.network;

import com.google.common.base.Strings;

import java.util.Objects;
import java.util.Optional;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class UcteNodeCode {

    private UcteCountryCode ucteCountryCode;
    private String geographicalSpot;
    private UcteVoltageLevelCode voltageLevelCode;
    private Character busbar;

    public UcteNodeCode(UcteCountryCode ucteCountryCode, String geographicalSpot, UcteVoltageLevelCode voltageLevelCode, Character busbar) {
        this.ucteCountryCode = Objects.requireNonNull(ucteCountryCode);
        this.geographicalSpot = Objects.requireNonNull(geographicalSpot);
        this.voltageLevelCode = Objects.requireNonNull(voltageLevelCode);
        this.busbar = busbar;
    }

    /**
     * Gets UCTE country code.
     * @return UCTE country code
     */
    public UcteCountryCode getUcteCountryCode() {
        return ucteCountryCode;
    }

    /**
     * Sets UCTE country code.
     * @param ucteCountryCode UCTE country code
     */
    public void setUcteCountryCode(UcteCountryCode ucteCountryCode) {
        this.ucteCountryCode = Objects.requireNonNull(ucteCountryCode);
    }

    /**
     * Gets short description of the geographical spot.
     * @return short description of the geographical spot
     */
    public String getGeographicalSpot() {
        return geographicalSpot;
    }

    /**
     * Sets short description of the geographical spot.
     * @param geographicalSpot short description of the geographical spot
     */
    public void setGeographicalSpot(String geographicalSpot) {
        this.geographicalSpot = Objects.requireNonNull(geographicalSpot);
    }

    /**
     * Gets voltage level.
     * @return voltage level
     */
    public UcteVoltageLevelCode getVoltageLevelCode() {
        return voltageLevelCode;
    }

    /**
     * Sets voltage level.
     * @param voltageLevelCode voltage level
     */
    public void setVoltageLevelCode(UcteVoltageLevelCode voltageLevelCode) {
        this.voltageLevelCode = voltageLevelCode;
    }

    /**
     * Gets letter or figure for differentiating bus bars (optional).
     * @return letter or figure for differentiating bus bars
     */
    public Character getBusbar() {
        return busbar;
    }

    /**
     * Sets letter or figure for differentiating bus bars (optional).
     * @param busbar letter or figure for differentiating bus bars
     */
    public void setBusbar(Character busbar) {
        this.busbar = busbar;
    }

    /**
     * Convert a string into a UcteNodeCode if possible, ie the string is compatible with the ucteNodeCode format
     * @param id to convert into a UcteNodeCode
     * @return an Optional that may contain a UcteNodeCode
     */
    public static Optional<UcteNodeCode> parseUcteNodeCode(String id) {
        UcteNodeCode ucteNodeCode = null;
        if (id != null && id.length() == 8 && UcteCountryCode.isUcteCountryCode(id.charAt(0)) && UcteVoltageLevelCode.isVoltageLevel(id.charAt(6))) {
            UcteCountryCode ucteCountryCode = UcteCountryCode.fromUcteCode(id.charAt(0));
            ucteNodeCode = new UcteNodeCode(
                    ucteCountryCode,
                    id.substring(1, 6),
                    UcteVoltageLevelCode.voltageLevelCodeFromChar(id.charAt(6)),
                    id.charAt(7));
        }
        return Optional.ofNullable(ucteNodeCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ucteCountryCode, geographicalSpot, voltageLevelCode, busbar);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UcteNodeCode) {
            UcteNodeCode other = (UcteNodeCode) obj;
            return ucteCountryCode == other.ucteCountryCode
                    && geographicalSpot.equals(other.geographicalSpot)
                    && voltageLevelCode == other.voltageLevelCode
                    && Objects.equals(busbar, other.busbar);
        }
        return false;
    }

    @Override
    public String toString() {
        return ucteCountryCode.getUcteCode() + Strings.padEnd(geographicalSpot, 5, ' ') + voltageLevelCode.ordinal() + (busbar != null ? busbar : ' ');
    }

}
