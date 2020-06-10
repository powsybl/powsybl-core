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

    String getLine1Name();

    MergedXnode setLine1Name(String line1Name);

    String getLine2Name();

    MergedXnode setLine2Name(String line2Name);

    String getCode();

    MergedXnode setCode(String xNodeCode);
}
