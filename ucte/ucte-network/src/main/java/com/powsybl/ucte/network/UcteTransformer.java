/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ucte.network;

import com.powsybl.commons.report.ReportNode;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class UcteTransformer extends UcteElement {

    private double ratedVoltage1;
    private double ratedVoltage2;
    private double nominalPower;
    private double conductance;

    public UcteTransformer(UcteElementId id, UcteElementStatus status, double resistance, double reactance, double susceptance, Integer currentLimit, String elementName,
                           double ratedVoltage1, double ratedVoltage2, double nominalPower, double conductance) {
        super(id, status, resistance, reactance, susceptance, currentLimit, elementName);
        this.ratedVoltage1 = ratedVoltage1;
        this.ratedVoltage2 = ratedVoltage2;
        this.nominalPower = nominalPower;
        this.conductance = conductance;
    }

   /**
     * Gets element id.
     * <p>Node 1 is non-regulated winding.
     * <p>Node 2 is regulated winding.
     * @return element id
     */
    @Override
    public UcteElementId getId() {
        return super.getId();
    }

    /**
     * Gets rated voltage 1: non-regulated winding (kV).
     * @return rated voltage 1
     */
    public double getRatedVoltage1() {
        return ratedVoltage1;
    }

    /**
     * Sets rated voltage 1: non-regulated winding (kV).
     * @param ratedVoltage1 the rated voltage 1
     */
    public void setRatedVoltage1(double ratedVoltage1) {
        this.ratedVoltage1 = ratedVoltage1;
    }

    /**
     * Gets rated voltage 2: regulated winding (kV).
     * @return rated voltage 2
     */
    public double getRatedVoltage2() {
        return ratedVoltage2;
    }

    /**
     * Sets rated voltage 2: regulated winding (kV).
     * @param ratedVoltage2 rated voltage 2
     */
    public void setRatedVoltage2(double ratedVoltage2) {
        this.ratedVoltage2 = ratedVoltage2;
    }

    /**
     * Gets nominal power (MVA).
     * @return nominal power (MVA)
     */
    public double getNominalPower() {
        return nominalPower;
    }

    /**
     * Sets nominal power (MVA).
     * @param nominalPower nominal power (MVA)
     */
    public void setNominalPower(double nominalPower) {
        this.nominalPower = nominalPower;
    }

    /**
     * Gets conductance G (μS).
     * @return conductance G (μS)
     */
    public double getConductance() {
        return conductance;
    }

    /**
     * Sets conductance G (μS).
     * @param conductance conductance G (μS)
     */
    public void setConductance(double conductance) {
        this.conductance = conductance;
    }

    @Override
    public void fix(ReportNode reportNode) {
        UcteValidation.checkValidTransformerCharacteristics(this, reportNode);
        super.fix(reportNode);
    }
}
