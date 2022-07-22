/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements.transformers;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class TapChanger {

    static TapChanger ratioTapChangerFromEnd(PropertyBag end, Context context) {
        Objects.requireNonNull(end);
        Objects.requireNonNull(context);
        PropertyBag rtc = context.ratioTapChanger(end.getId(CgmesNames.RATIO_TAP_CHANGER));
        return rtc != null ? AbstractCgmesTapChangerBuilder.newRatioTapChanger(rtc, context).build() : null;
    }

    static TapChanger phaseTapChangerFromEnd(PropertyBag end, double x, Context context) {
        Objects.requireNonNull(end);
        Objects.requireNonNull(context);
        PropertyBag ptc = context.phaseTapChanger(end.getId(CgmesNames.PHASE_TAP_CHANGER));
        return ptc != null ? AbstractCgmesTapChangerBuilder.newPhaseTapChanger(ptc, x, context).build() : null;
    }

    private int lowTapPosition = 0;
    private Integer tapPosition;
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

        Step setAngle(double angle) {
            this.angleRad = angle;
            return this;
        }

        Step setRatio(double ratio) {
            this.ratio = ratio;
            return this;
        }

        Step setR(double r) {
            this.r = r;
            return this;
        }

        Step setX(double x) {
            this.x = x;
            return this;
        }

        Step setG1(double g1) {
            this.g1 = g1;
            return this;
        }

        Step setB1(double b1) {
            this.b1 = b1;
            return this;
        }

        Step setG2(double g2) {
            this.g2 = g2;
            return this;
        }

        Step setB2(double b2) {
            this.b2 = b2;
            return this;
        }

        TapChanger endStep() {
            steps.add(this);
            return TapChanger.this;
        }

        double getAngle() {
            return angleRad;
        }

        double getRatio() {
            return ratio;
        }

        double getR() {
            return r;
        }

        double getX() {
            return x;
        }

        double getG1() {
            return g1;
        }

        double getB1() {
            return b1;
        }

        double getG2() {
            return g2;
        }

        double getB2() {
            return b2;
        }
    }

    TapChanger setLowTapPosition(int lowTapPosition) {
        this.lowTapPosition = lowTapPosition;
        return this;
    }

    TapChanger setTapPosition(int tapPosition) {
        this.tapPosition = tapPosition;
        return this;
    }

    TapChanger setLtcFlag(boolean ltcFlag) {
        this.ltcFlag = ltcFlag;
        return this;
    }

    TapChanger setType(String type) {
        this.type = type;
        return this;
    }

    TapChanger setId(String id) {
        this.id = id;
        return this;
    }

    TapChanger setRegulating(boolean regulating) {
        this.regulating = regulating;
        return this;
    }

    TapChanger setRegulatingControlId(String regulatingControlId) {
        this.regulatingControlId = regulatingControlId;
        return this;
    }

    TapChanger setTculControlMode(String tculControlMode) {
        this.tculControlMode = tculControlMode;
        return this;
    }

    TapChanger setTapChangerControlEnabled(boolean tapChangerControlEnabled) {
        this.tapChangerControlEnabled = tapChangerControlEnabled;
        return this;
    }

    TapChanger setHiddenCombinedTapChanger(TapChanger hiddenCombinedTapChanger) {
        this.hiddenCombinedTapChanger = hiddenCombinedTapChanger;
        return this;
    }

    Step beginStep() {
        return new Step();
    }

    int getLowTapPosition() {
        return lowTapPosition;
    }

    Integer getTapPosition() {
        return tapPosition;
    }

    List<Step> getSteps() {
        return steps;
    }

    int getHighTapPosition() {
        return lowTapPosition + steps.size() - 1;
    }

    boolean isLtcFlag() {
        return ltcFlag;
    }

    String getType() {
        return type;
    }

    String getId() {
        return id;
    }

    boolean isRegulating() {
        return regulating;
    }

    String getRegulatingControlId() {
        return regulatingControlId;
    }

    String getTculControlMode() {
        return tculControlMode;
    }

    boolean isTapChangerControlEnabled() {
        return tapChangerControlEnabled;
    }

    TapChanger getHiddenCombinedTapChanger() {
        return hiddenCombinedTapChanger;
    }
}
