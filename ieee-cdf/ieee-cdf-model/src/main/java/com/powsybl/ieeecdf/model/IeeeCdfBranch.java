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
import com.univocity.parsers.fixed.FieldAlignment;

/**
 * <p>
 * @see <a href="https://labs.ece.uw.edu/pstca/formats/cdf.txt">https://labs.ece.uw.edu/pstca/formats/cdf.txt</a>
 * </p>
 *
 * <pre>
 * Columns  1- 4   Tap bus number (I) *
 *                  For transformers or phase shifters, the side of the model
 *                  the non-unity tap is on
 * Columns  6- 9   Z bus number (I) *
 *                  For transformers and phase shifters, the side of the model
 *                  the device impedance is on.
 * Columns 11-12   Load flow area (I)
 * Columns 13-14   Loss zone (I)
 * Column  17      Circuit (I) * (Use 1 for single lines)
 * Column  19      Type (I) *
 *                  0 - Transmission line
 *                  1 - Fixed tap
 *                  2 - Variable tap for voltage control (TCUL, LTC)
 *                  3 - Variable tap (turns ratio) for MVAR control
 *                  4 - Variable phase angle for MW control (phase shifter)
 * Columns 20-29   Branch resistance R, per unit (F) *
 * Columns 30-40   Branch reactance X, per unit (F) * No zero impedance lines
 * Columns 41-50   Line charging B, per unit (F) * (total line charging, +B)
 * Columns 51-55   Line MVA rating No 1 (I) Left justify!
 * Columns 57-61   Line MVA rating No 2 (I) Left justify!
 * Columns 63-67   Line MVA rating No 3 (I) Left justify!
 * Columns 69-72   Control bus number
 * Column  74      Side (I)
 *                  0 - Controlled bus is one of the terminals
 *                  1 - Controlled bus is near the tap side
 *                  2 - Controlled bus is near the impedance side (Z bus)
 * Columns 77-82   Transformer final turns ratio (F)
 * Columns 84-90   Transformer (phase shifter) final angle (F)
 * Columns 91-97   Minimum tap or phase shift (F)
 * Columns 98-104  Maximum tap or phase shift (F)
 * Columns 106-111 Step size (F)
 * Columns 113-119 Minimum voltage, MVAR or MW limit (F)
 * Columns 120-126 Maximum voltage, MVAR or MW limit (F)
 * </pre>
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class IeeeCdfBranch {

    /**
     * 0 - Transmission line
     * 1 - Fixed tap
     * 2 - Variable tap for voltage control (TCUL, LTC)
     * 3 - Variable tap (turns ratio) for MVAR control
     * 4 - Variable phase angle for MW control (phase shifter)
     */
    public enum Type {
        TRANSMISSION_LINE,
        FIXED_TAP,
        VARIABLE_TAP_FOR_VOLTAVE_CONTROL,
        VARIABLE_TAP_FOR_REACTIVE_POWER_CONTROL,
        VARIABLE_PHASE_ANGLE_FOR_ACTIVE_POWER_CONTROL,
    }

    public enum Side {
        CONTROLLED_BUS_IS_ONE_OF_THE_TERMINALS,
        CONTROLLED_BUS_IS_NEAR_THE_TAP_SIDE,
        CONTROLLED_BUS_IS_NEAR_THE_IMPEDANCE_SIDE
    }

    /**
     * Tap bus number (I) *
     */
    @FixedWidth(from = 0, to = 4, alignment = FieldAlignment.RIGHT)
    @Parsed
    private int tapBusNumber;

    /**
     * Z bus number (I) *
     */
    @FixedWidth(from = 5, to = 9, alignment = FieldAlignment.RIGHT)
    @Parsed
    private int zBusNumber;

    /**
     * Load flow area (I)
     */
    @FixedWidth(from = 10, to = 12, alignment = FieldAlignment.RIGHT)
    @Parsed
    private int area;

    /**
     * Loss zone (I)
     */
    @FixedWidth(from = 13, to = 15, alignment = FieldAlignment.RIGHT)
    @Parsed
    private int lossZone;

    /**
     * Circuit (I) * (Use 1 for single lines)
     */
    @FixedWidth(from = 16, to = 17, alignment = FieldAlignment.RIGHT)
    @Parsed
    private int circuit;

    /**
     * Type (I) *
     */
    @FixedWidth(from = 18, to = 19)
    @Parsed
    @Convert(conversionClass = BranchTypeConversion.class)
    private Type type;

    /**
     * Branch resistance R, per unit (F) *
     */
    @FixedWidth(from = 19, to = 29, alignment = FieldAlignment.RIGHT)
    @Parsed
    private double resistance;

    /**
     * Branch reactance X, per unit (F) * No zero impedance lines
     */
    @FixedWidth(from = 29, to = 40, alignment = FieldAlignment.RIGHT)
    @Parsed
    private double reactance;

    /**
     * Line charging B, per unit (F) * (total line charging, +B)
     */
    @FixedWidth(from = 40, to = 50, alignment = FieldAlignment.RIGHT)
    @Parsed
    private double chargingSusceptance;

    /**
     * Line MVA rating No 1 (I) Left justify!
     */
    @FixedWidth(from = 50, to = 55, alignment = FieldAlignment.RIGHT)
    @Parsed
    private int rating1;

    /**
     * Line MVA rating No 2 (I) Left justify!
     */
    @FixedWidth(from = 56, to = 61, alignment = FieldAlignment.RIGHT)
    @Parsed
    private int rating2;

    /**
     * Line MVA rating No 3 (I) Left justify!
     */
    @FixedWidth(from = 62, to = 67, alignment = FieldAlignment.RIGHT)
    @Parsed
    private int rating3;

    /**
     * Control bus number
     */
    @FixedWidth(from = 68, to = 72, alignment = FieldAlignment.RIGHT)
    @Parsed
    private int controlBusNumber;

    /**
     * Side (I)
     */
    @FixedWidth(from = 73, to = 74, alignment = FieldAlignment.RIGHT)
    @Parsed
    @Convert(conversionClass = BranchSideConversion.class)
    private Side side;

    /**
     * Transformer final turns ratio (F)
     */
    @FixedWidth(from = 76, to = 82)
    @Parsed
    private double finalTurnsRatio;

    /**
     * Transformer (phase shifter) final angle (F)
     */
    @FixedWidth(from = 83, to = 90)
    @Parsed
    private double finalAngle;

    /**
     * Minimum tap or phase shift (F)
     */
    @FixedWidth(from = 90, to = 97)
    @Parsed
    private double minTapOrPhaseShift;

    /**
     * Maximum tap or phase shift (F)
     */
    @FixedWidth(from = 97, to = 104)
    @Parsed
    private double maxTapOrPhaseShift;

    /**
     * Step size (F)
     */
    @FixedWidth(from = 105, to = 112)
    @Parsed
    private double stepSize;

    /**
     * Minimum voltage, MVAR or MW limit (F)
     */
    @FixedWidth(from = 112, to = 118)
    @Parsed
    private double minVoltageActiveOrReactivePowerLimit;

    /**
     * Maximum voltage, MVAR or MW limit (F)
     */
    @FixedWidth(from = 119, to = 126)
    @Parsed
    private double maxVoltageActiveOrReactivePowerLimit;

    /**
     * This parameter does not exist in the specification but is present in 300 buses case.
     */
    @FixedWidth(from = 126, to = 133)
    @Parsed
    private int unused;

    public int getTapBusNumber() {
        return tapBusNumber;
    }

    public void setTapBusNumber(int tapBusNumber) {
        this.tapBusNumber = tapBusNumber;
    }

    public int getzBusNumber() {
        return zBusNumber;
    }

    public void setzBusNumber(int zBusNumber) {
        this.zBusNumber = zBusNumber;
    }

    public int getArea() {
        return area;
    }

    public void setArea(int area) {
        this.area = area;
    }

    public int getLossZone() {
        return lossZone;
    }

    public void setLossZone(int lossZone) {
        this.lossZone = lossZone;
    }

    public int getCircuit() {
        return circuit;
    }

    public void setCircuit(int circuit) {
        this.circuit = circuit;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public double getResistance() {
        return resistance;
    }

    public void setResistance(double resistance) {
        this.resistance = resistance;
    }

    public double getReactance() {
        return reactance;
    }

    public void setReactance(double reactance) {
        this.reactance = reactance;
    }

    public double getChargingSusceptance() {
        return chargingSusceptance;
    }

    public void setChargingSusceptance(double chargingSusceptance) {
        this.chargingSusceptance = chargingSusceptance;
    }

    public int getRating1() {
        return rating1;
    }

    public void setRating1(int rating1) {
        this.rating1 = rating1;
    }

    public int getRating2() {
        return rating2;
    }

    public void setRating2(int rating2) {
        this.rating2 = rating2;
    }

    public int getRating3() {
        return rating3;
    }

    public void setRating3(int rating3) {
        this.rating3 = rating3;
    }

    public int getControlBusNumber() {
        return controlBusNumber;
    }

    public void setControlBusNumber(int controlBusNumber) {
        this.controlBusNumber = controlBusNumber;
    }

    public Side getSide() {
        return side;
    }

    public void setSide(Side side) {
        this.side = side;
    }

    public double getFinalTurnsRatio() {
        return finalTurnsRatio;
    }

    public void setFinalTurnsRatio(double finalTurnsRatio) {
        this.finalTurnsRatio = finalTurnsRatio;
    }

    public double getFinalAngle() {
        return finalAngle;
    }

    public void setFinalAngle(double finalAngle) {
        this.finalAngle = finalAngle;
    }

    public double getMinTapOrPhaseShift() {
        return minTapOrPhaseShift;
    }

    public void setMinTapOrPhaseShift(double minTapOrPhaseShift) {
        this.minTapOrPhaseShift = minTapOrPhaseShift;
    }

    public double getMaxTapOrPhaseShift() {
        return maxTapOrPhaseShift;
    }

    public void setMaxTapOrPhaseShift(double maxTapOrPhaseShift) {
        this.maxTapOrPhaseShift = maxTapOrPhaseShift;
    }

    public double getStepSize() {
        return stepSize;
    }

    public void setStepSize(double stepSize) {
        this.stepSize = stepSize;
    }

    public double getMinVoltageActiveOrReactivePowerLimit() {
        return minVoltageActiveOrReactivePowerLimit;
    }

    public void setMinVoltageActiveOrReactivePowerLimit(double minVoltageActiveOrReactivePowerLimit) {
        this.minVoltageActiveOrReactivePowerLimit = minVoltageActiveOrReactivePowerLimit;
    }

    public double getMaxVoltageActiveOrReactivePowerLimit() {
        return maxVoltageActiveOrReactivePowerLimit;
    }

    public void setMaxVoltageActiveOrReactivePowerLimit(double maxVoltageActiveOrReactivePowerLimit) {
        this.maxVoltageActiveOrReactivePowerLimit = maxVoltageActiveOrReactivePowerLimit;
    }
}
