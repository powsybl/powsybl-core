/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class TapChangerConversion {

    private int lowTapPosition = 0;
    private Integer tapPosition;
    private final List<StepAdder> steps = new ArrayList<>();
    private boolean ltcFlag = false;
    private String id = null;
    private String regulatingControlId = null;
    private String tculControlMode = null;
    private boolean tapChangerControlEnabled = false;

    class StepAdder {
        private double angleRad = 0.0;
        private double ratio = 1.0;
        private double r = 0.0;
        private double x = 0.0;
        private double g1 = 0.0;
        private double b1 = 0.0;
        private double g2 = 0.0;
        private double b2 = 0.0;

        public StepAdder setAngle(double angle) {
            this.angleRad = angle;
            return this;
        }

        public StepAdder setRatio(double ratio) {
            this.ratio = ratio;
            return this;
        }

        public StepAdder setR(double r) {
            this.r = r;
            return this;
        }

        public StepAdder setX(double x) {
            this.x = x;
            return this;
        }

        public StepAdder setG1(double g1) {
            this.g1 = g1;
            return this;
        }

        public StepAdder setB1(double b1) {
            this.b1 = b1;
            return this;
        }

        public StepAdder setG2(double g2) {
            this.g2 = g2;
            return this;
        }

        public StepAdder setB2(double b2) {
            this.b2 = b2;
            return this;
        }

        public TapChangerConversion endStep() {
            steps.add(this);
            return TapChangerConversion.this;
        }

        public double getAngle() {
            return angleRad;
        }

        public double getRatio() {
            return ratio;
        }

        public double getR() {
            return r;
        }

        public double getX() {
            return x;
        }

        public double getG1() {
            return g1;
        }

        public double getB1() {
            return b1;
        }

        public double getG2() {
            return g2;
        }

        public double getB2() {
            return b2;
        }
    }

    public TapChangerConversion setLowTapPosition(int lowTapPosition) {
        this.lowTapPosition = lowTapPosition;
        return this;
    }

    public TapChangerConversion setTapPosition(int tapPosition) {
        this.tapPosition = tapPosition;
        return this;
    }

    public TapChangerConversion setLtcFlag(boolean ltcFlag) {
        this.ltcFlag = ltcFlag;
        return this;
    }

    public TapChangerConversion setId(String id) {
        this.id = id;
        return this;
    }

    public TapChangerConversion setRegulatingControlId(String regulatingControlId) {
        this.regulatingControlId = regulatingControlId;
        return this;
    }

    public TapChangerConversion setTculControlMode(String tculControlMode) {
        this.tculControlMode = tculControlMode;
        return this;
    }

    public TapChangerConversion setTapChangerControlEnabled(boolean tapChangerControlEnabled) {
        this.tapChangerControlEnabled = tapChangerControlEnabled;
        return this;
    }

    public StepAdder beginStep() {
        return new StepAdder();
    }

    public int getLowTapPosition() {
        return lowTapPosition;
    }

    public Integer getTapPosition() {
        return tapPosition;
    }

    public List<StepAdder> getSteps() {
        return steps;
    }

    public int getHighTapPosition() {
        return lowTapPosition + steps.size() - 1;
    }

    public boolean isLtcFlag() {
        return ltcFlag;
    }

    public String getId() {
        return id;
    }

    public String getRegulatingControlId() {
        return regulatingControlId;
    }

    public String getTculControlMode() {
        return tculControlMode;
    }

    public boolean isTapChangerControlEnabled() {
        return tapChangerControlEnabled;
    }
}
