/**
 * Copyright (c) 2026, Kick Stage d.o.o.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Line;

/**
 * Series impedance of a line seen from its side 2, when it differs from the one seen from side 1 (the line's r and x).
 * The current entering at side 1 is (V1 - V2) / (r + jx), the current entering at side 2 is (V2 - V1) / (r21 + jx21).
 * Such a line is the result of a network reduction, as a CGMES EquivalentBranch with r21 and x21.
 *
 * @author Pascal Ehlert {@literal <pascal.ehlert at kickstage.com>}
 */
public interface LineNonReciprocalImpedance extends Extension<Line> {

    String NAME = "lineNonReciprocalImpedance";

    @Override
    default String getName() {
        return NAME;
    }

    /**
     * The series resistance seen from side 2, in ohm.
     */
    double getR21();

    void setR21(double r21);

    /**
     * The series reactance seen from side 2, in ohm.
     */
    double getX21();

    void setX21(double x21);
}
