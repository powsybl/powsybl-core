/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ieeecdf.model;

import com.univocity.parsers.annotations.Convert;
import com.univocity.parsers.annotations.FixedWidth;
import com.univocity.parsers.annotations.Parsed;

import java.time.LocalDate;

/**
 * <p>
 * @see <a href="https://labs.ece.uw.edu/pstca/formats/cdf.txt">https://labs.ece.uw.edu/pstca/formats/cdf.txt</a>
 * </p>
 *
 * <pre>
 * Columns  2- 9   Date, in format DD/MM/YY with leading zeros. If no date provided, use 0b/0b/0b where b is blank.
 * Columns 11-30   Originator's name (A)
 * Columns 32-37   MVA Base (F*)
 * Columns 39-42   Year (I)
 * Column  44      Season (S - Summer, W - Winter)
 * Column  46-73   Case identification (A)
 * </pre>
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class IeeeCdfTitle {

    public enum Season {
        SUMMER,
        WINTER
    }

    /**
     * Date
     */
    @FixedWidth(from = 1, to = 10)
    @Parsed
    @Convert(conversionClass = LocalDateConversion.class)
    private LocalDate date;

    /**
     * Originator's name
     */
    @FixedWidth(from = 10, to = 30)
    @Parsed
    private String originatorName;

    /**
     * MVA Base
     */
    @FixedWidth(from = 31, to = 37)
    @Parsed
    private double mvaBase;

    /**
     * Year
     */
    @FixedWidth(from = 38, to = 42)
    @Parsed
    private int year;

    /**
     *  Season
     */
    @FixedWidth(from = 43, to = 44)
    @Parsed
    @Convert(conversionClass = SeasonConversion.class)
    private Season season;

    /**
     * Case identification
     */
    @FixedWidth(from = 45, to = 73)
    @Parsed
    private String caseIdentification;

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getOriginatorName() {
        return originatorName;
    }

    public void setOriginatorName(String originatorName) {
        this.originatorName = originatorName;
    }

    public double getMvaBase() {
        return mvaBase;
    }

    public void setMvaBase(double mvaBase) {
        this.mvaBase = mvaBase;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public Season getSeason() {
        return season;
    }

    public void setSeason(Season season) {
        this.season = season;
    }

    public String getCaseIdentification() {
        return caseIdentification;
    }

    public void setCaseIdentification(String caseIdentification) {
        this.caseIdentification = caseIdentification;
    }
}
