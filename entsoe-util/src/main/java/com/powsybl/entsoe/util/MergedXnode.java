/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.entsoe.util;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Line;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface MergedXnode extends Extension<Line> {

    @Override
    default String getName() {
        return "mergedXnode";
    }

    default float getRdp() {
        return 0.5f;
    }

    default MergedXnode setRdp(float rdp) {
        return this;
    }

    default float getXdp() {
        return 0.5f;
    }

    default MergedXnode setXdp(float xdp) {
        return this;
    }

    String getLine1Name();

    MergedXnode setLine1Name(String line1Name);

    default boolean isLine1Fictitious() {
        return false;
    }

    default MergedXnode setLine1Fictitious(boolean line1Fictitious) {
        return this;
    }

    double getXnodeP1();

    MergedXnode setXnodeP1(double xNodeP1);

    double getXnodeQ1();

    MergedXnode setXnodeQ1(double xNodeQ1);

    default float getB1dp() {
        return 0.5f;
    }

    default MergedXnode setB1dp(float b1dp) {
        return this;
    }

    default float getG1dp() {
        return 0.5f;
    }

    default MergedXnode setG1dp(float g1dp) {
        return this;
    }

    String getLine2Name();

    MergedXnode setLine2Name(String line2Name);

    default boolean isLine2Fictitious() {
        return false;
    }

    default MergedXnode setLine2Fictitious(boolean line1Fictitious) {
        return this;
    }

    double getXnodeP2();

    MergedXnode setXnodeP2(double xNodeP2);

    double getXnodeQ2();

    MergedXnode setXnodeQ2(double xNodeQ2);

    default float getB2dp() {
        return 0.5f;
    }

    default MergedXnode setB2dp(float b2dp) {
        return this;
    }

    default float getG2dp() {
        return 0.5f;
    }

    default MergedXnode setG2dp(float g2dp) {
        return this;
    }

    String getCode();

    MergedXnode setCode(String xNodeCode);
}
