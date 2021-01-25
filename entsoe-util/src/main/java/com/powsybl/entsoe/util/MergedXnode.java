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

    double getXnodeP1();

    MergedXnode setXnodeP1(double xNodeP1);

    double getXnodeQ1();

    MergedXnode setXnodeQ1(double xNodeQ1);

    double getXnodeP2();

    MergedXnode setXnodeP2(double xNodeP2);

    double getXnodeQ2();

    MergedXnode setXnodeQ2(double xNodeQ2);

    boolean isLine1Fictitious();

    MergedXnode setLine1Fictitious(boolean line1Fictitious);

    String getLine1Name();

    MergedXnode setLine1Name(String line1Name);

    double getLine1B1();

    MergedXnode setLine1B1(double line1B1);

    double getLine1B2();

    MergedXnode setLine1B2(double line1B2);

    double getLine1G1();

    MergedXnode setLine1G1(double line1G1);

    double getLine1G2();

    MergedXnode setLine1G2(double line1G2);

    String getLine2Name();

    MergedXnode setLine2Name(String line2Name);

    boolean isLine2Fictitious();

    MergedXnode setLine2Fictitious(boolean line2Fictitious);

    double getLine2B1();

    MergedXnode setLine2B1(double line2B1);

    double getLine2B2();

    MergedXnode setLine2B2(double line2B2);

    double getLine2G1();

    MergedXnode setLine2G1(double line2G1);

    double getLine2G2();

    MergedXnode setLine2G2(double line2G2);

    String getCode();

    MergedXnode setCode(String xNodeCode);
}
