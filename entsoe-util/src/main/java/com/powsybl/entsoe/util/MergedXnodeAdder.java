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

    MergedXnodeAdder withXnodeP1(double xnodeP1);

    MergedXnodeAdder withXnodeQ1(double xnodeQ1);

    MergedXnodeAdder withXnodeP2(double xnodeP2);

    MergedXnodeAdder withXnodeQ2(double xnodeQ2);

    MergedXnodeAdder withLine1Name(String line1Name);

    MergedXnodeAdder withLine2Name(String line2Name);

    MergedXnodeAdder withCode(String code);
}
