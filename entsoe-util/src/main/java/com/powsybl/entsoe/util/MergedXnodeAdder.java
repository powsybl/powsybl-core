/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.entsoe.util;

import com.powsybl.commons.extensions.ExtensionAdder;
import com.powsybl.iidm.network.Line;

/**
 * @author Jérémy Labous <jlabous at silicom.fr>
 */
public interface MergedXnodeAdder extends ExtensionAdder<Line, MergedXnode> {

    @Override
    default Class<MergedXnode> getExtensionClass() {
        return MergedXnode.class;
    }

    MergedXnodeAdder withRdp(float rdp);

    MergedXnodeAdder withXdp(float xdp);

    MergedXnodeAdder withLine1Name(String line1Name);

    MergedXnodeAdder withLine1Fictitious(boolean line1Fictitious);

    MergedXnodeAdder withXnodeP1(double xnodeP1);

    MergedXnodeAdder withXnodeQ1(double xnodeQ1);

    MergedXnodeAdder withB1dp(float b1dp);

    MergedXnodeAdder withG1dp(float g1dp);

    MergedXnodeAdder withLine2Name(String line2Name);

    MergedXnodeAdder withLine2Fictitious(boolean line2Fictitious);

    MergedXnodeAdder withXnodeP2(double xnodeP2);

    MergedXnodeAdder withXnodeQ2(double xnodeQ2);

    MergedXnodeAdder withB2dp(float b2dp);

    MergedXnodeAdder withG2dp(float g2dp);

    MergedXnodeAdder withCode(String code);
}
