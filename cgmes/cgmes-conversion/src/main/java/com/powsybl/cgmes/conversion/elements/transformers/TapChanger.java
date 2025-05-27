/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.elements.transformers;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class TapChanger {

    public static TapChanger ratioTapChangerFromEnd(PropertyBag end, Context context) {
        Objects.requireNonNull(end);
        Objects.requireNonNull(context);
        PropertyBag rtc = context
                .ratioTapChangers(end.getId("PowerTransformer"))
                .stream()
                .filter(tc -> end.getId(CgmesNames.TRANSFORMER_END).equals(tc.getId(CgmesNames.TRANSFORMER_END)))
                .findFirst()
                .orElse(null);
        return rtc != null ? AbstractCgmesTapChangerBuilder.newRatioTapChanger(rtc, context).build() : null;
    }

    public static TapChanger phaseTapChangerFromEnd(PropertyBag end, double x, Context context) {
        Objects.requireNonNull(end);
        Objects.requireNonNull(context);
        PropertyBag ptc = context
                .phaseTapChangers(end.getId("PowerTransformer"))
                .stream()
                .filter(tc -> end.getId(CgmesNames.TRANSFORMER_END).equals(tc.getId(CgmesNames.TRANSFORMER_END)))
                .findFirst()
                .orElse(null);
        return ptc != null ? AbstractCgmesTapChangerBuilder.newPhaseTapChanger(ptc, x, context).build() : null;
    }

    private int lowTapPosition = 0;
    private Integer tapPosition;
    private Integer solvedTapPosition;
    private final List<Step> steps = new ArrayList<>();
    private boolean ltcFlag = false;
    private String id = null;
    private String type = null;
    private boolean regulating = false;
    private String regulatingControlId = null;
    private String tculControlMode = null;
    private boolean tapChangerControlEnabled = false;
    private TapChanger hiddenCombinedTapChanger = null;

    class Step {
        private double angleRad = 0.0;
        private double ratio = 1.0;
        private double r = 0.0;
        private double x = 0.0;
        private double g1 = 0.0;
        private double b1 = 0.0;
        private double g2 = 0.0;
        private double b2 = 0.0;

        public Step setAngle(double angle) {
            this.angleRad = angle;
            return this;
        }

        public Step setRatio(double ratio) {
            this.ratio = ratio;
            return this;
        }

        public Step setR(double r) {
            this.r = r;
            return this;
        }

        public Step setX(double x) {
            this.x = x;
            return this;
        }

        public Step setG1(double g1) {
            this.g1 = g1;
            return this;
        }

        public Step setB1(double b1) {
            this.b1 = b1;
            return this;
        }

        public Step setG2(double g2) {
            this.g2 = g2;
            return this;
        }

        public Step setB2(double b2) {
            this.b2 = b2;
            return this;
        }

        public TapChanger endStep() {
            steps.add(this);
            return TapChanger.this;
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

    public TapChanger setLowTapPosition(int lowTapPosition) {
        this.lowTapPosition = lowTapPosition;
        return this;
    }

    public TapChanger setTapPosition(int tapPosition) {
        this.tapPosition = tapPosition;
        return this;
    }

    public TapChanger setSolvedTapPosition(Integer solvedTapPosition) {
        this.solvedTapPosition = solvedTapPosition;
        return this;
    }

    public TapChanger setLtcFlag(boolean ltcFlag) {
        this.ltcFlag = ltcFlag;
        return this;
    }

    public TapChanger setType(String type) {
        this.type = type;
        return this;
    }

    public TapChanger setId(String id) {
        this.id = id;
        return this;
    }

    public TapChanger setRegulating(boolean regulating) {
        this.regulating = regulating;
        return this;
    }

    public TapChanger setRegulatingControlId(String regulatingControlId) {
        this.regulatingControlId = regulatingControlId;
        return this;
    }

    public TapChanger setTculControlMode(String tculControlMode) {
        this.tculControlMode = tculControlMode;
        return this;
    }

    public TapChanger setTapChangerControlEnabled(boolean tapChangerControlEnabled) {
        this.tapChangerControlEnabled = tapChangerControlEnabled;
        return this;
    }

    public TapChanger setHiddenCombinedTapChanger(TapChanger hiddenCombinedTapChanger) {
        this.hiddenCombinedTapChanger = hiddenCombinedTapChanger;
        return this;
    }

    public Step beginStep() {
        return new Step();
    }

    public int getLowTapPosition() {
        return lowTapPosition;
    }

    public Integer getTapPosition() {
        return tapPosition;
    }

    public Integer getSolvedTapPosition() {
        return solvedTapPosition;
    }

    public List<Step> getSteps() {
        return steps;
    }

    public int getHighTapPosition() {
        return lowTapPosition + steps.size() - 1;
    }

    public boolean isLtcFlag() {
        return ltcFlag;
    }

    public String getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public boolean isRegulating() {
        return regulating;
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

    public TapChanger getHiddenCombinedTapChanger() {
        return hiddenCombinedTapChanger;
    }
}
