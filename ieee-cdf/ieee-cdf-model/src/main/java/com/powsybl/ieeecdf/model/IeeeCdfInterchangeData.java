/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ieeecdf.model;

import com.univocity.parsers.annotations.FixedWidth;
import com.univocity.parsers.annotations.Parsed;

/**
 * <p>
 * @see <a href="https://labs.ece.uw.edu/pstca/formats/cdf.txt">https://labs.ece.uw.edu/pstca/formats/cdf.txt</a>
 * </p>
 *
 * <pre>
 * Columns  1- 2   Area number (I) no zeros! *
 * Columns  4- 7   Interchange slack bus number (I) *
 * Columns  9-20   Alternate swing bus name (A)
 * Columns 21-28   Area interchange export, MW (F) (+ = out) *
 * Columns 30-35   Area interchange tolerance, MW (F) *
 * Columns 38-43   Area code (abbreviated name) (A) *
 * Columns 46-75   Area name (A)
 * </pre>
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class IeeeCdfInterchangeData {

    /**
     * Area number
     */
    @FixedWidth(from = 0, to = 2)
    @Parsed
    private int areaNumber;

    /**
     * Interchange slack bus number
     */
    @FixedWidth(from = 3, to = 7)
    @Parsed
    private int interchangeSlackBusNumber;

    /**
     * Alternate swing bus name
     */
    @FixedWidth(from = 8, to = 20)
    @Parsed
    private String alternateSwingBusName;

    /**
     * Area interchange export, MW
     */
    @FixedWidth(from = 20, to = 28)
    @Parsed
    private double areaInterchangeExport;

    /**
     * Area interchange tolerance, MW
     */
    @FixedWidth(from = 29, to = 35)
    @Parsed
    private double areaInterchangeTolerance;

    /**
     * Area code (abbreviated name)
     */
    @FixedWidth(from = 37, to = 43)
    @Parsed
    private String areaCode;

    /**
     * Area name
     */
    @FixedWidth(from = 45, to = 75)
    @Parsed
    private String areaName;

    public int getAreaNumber() {
        return areaNumber;
    }

    public void setAreaNumber(int areaNumber) {
        this.areaNumber = areaNumber;
    }

    public int getInterchangeSlackBusNumber() {
        return interchangeSlackBusNumber;
    }

    public void setInterchangeSlackBusNumber(int interchangeSlackBusNumber) {
        this.interchangeSlackBusNumber = interchangeSlackBusNumber;
    }

    public String getAlternateSwingBusName() {
        return alternateSwingBusName;
    }

    public void setAlternateSwingBusName(String alternateSwingBusName) {
        this.alternateSwingBusName = alternateSwingBusName;
    }

    public double getAreaInterchangeExport() {
        return areaInterchangeExport;
    }

    public void setAreaInterchangeExport(double areaInterchangeExport) {
        this.areaInterchangeExport = areaInterchangeExport;
    }

    public double getAreaInterchangeTolerance() {
        return areaInterchangeTolerance;
    }

    public void setAreaInterchangeTolerance(double areaInterchangeTolerance) {
        this.areaInterchangeTolerance = areaInterchangeTolerance;
    }

    public String getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }
}
