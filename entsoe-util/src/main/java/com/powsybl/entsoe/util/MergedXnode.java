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

    float getRdp();

    MergedXnode setRdp(float rdp);

    float getXdp();

    MergedXnode setXdp(float xdp);

    boolean isLine1Fictitious();

    MergedXnode setLine1Fictitious(boolean line1Fictitious);

    String getLine1Name();

    MergedXnode setLine1Name(String line1Name);

    double getXnodeP1();

    MergedXnode setXnodeP1(double xNodeP1);

    double getXnodeQ1();

    MergedXnode setXnodeQ1(double xNodeQ1);

    float getB1dp();

    MergedXnode setB1dp(float b1dp);

    float getG1dp();

    MergedXnode setG1dp(float g1dp);

    String getLine2Name();

    MergedXnode setLine2Name(String line2Name);

    boolean isLine2Fictitious();

    MergedXnode setLine2Fictitious(boolean line2Fictitious);

    double getXnodeP2();

    MergedXnode setXnodeP2(double xNodeP2);

    double getXnodeQ2();

    MergedXnode setXnodeQ2(double xNodeQ2);

    float getB2dp();

    MergedXnode setB2dp(float b2dp);

    float getG2dp();

    MergedXnode setG2dp(float g2dp);

    String getCode();

    MergedXnode setCode(String xNodeCode);
}
