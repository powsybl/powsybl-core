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

    default MergedXnodeAdder withRdp(double rdp) {
        return this;
    }

    default MergedXnodeAdder withXdp(double xdp) {
        return this;
    }

    MergedXnodeAdder withLine1Name(String line1Name);

    default MergedXnodeAdder withLine1Fictitious(boolean line1Fictitious) {
        return this;
    }

    MergedXnodeAdder withXnodeP1(double xnodeP1);

    MergedXnodeAdder withXnodeQ1(double xnodeQ1);

    default MergedXnodeAdder withB1dp(double b1dp) {
        return this;
    }

    default MergedXnodeAdder withG1dp(double g1dp) {
        return this;
    }

    MergedXnodeAdder withLine2Name(String line2Name);

    default MergedXnodeAdder withLine2Fictitious(boolean line2Fictitious) {
        return this;
    }

    MergedXnodeAdder withXnodeP2(double xnodeP2);

    MergedXnodeAdder withXnodeQ2(double xnodeQ2);

    default MergedXnodeAdder withB2dp(double b2dp) {
        return this;
    }

    default MergedXnodeAdder withG2dp(double g2dp) {
        return this;
    }

    MergedXnodeAdder withCode(String code);
}
