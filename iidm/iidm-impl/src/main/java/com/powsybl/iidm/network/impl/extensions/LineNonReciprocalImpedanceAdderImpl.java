/**
 * Copyright (c) 2026, Kick Stage d.o.o.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.extensions.LineNonReciprocalImpedance;
import com.powsybl.iidm.network.extensions.LineNonReciprocalImpedanceAdder;

/**
 * @author Pascal Ehlert {@literal <pascal.ehlert at kickstage.com>}
 */
public class LineNonReciprocalImpedanceAdderImpl extends AbstractExtensionAdder<Line, LineNonReciprocalImpedance> implements LineNonReciprocalImpedanceAdder {

    private double r21 = Double.NaN;
    private double x21 = Double.NaN;

    public LineNonReciprocalImpedanceAdderImpl(Line line) {
        super(line);
    }

    @Override
    public Class<? super LineNonReciprocalImpedance> getExtensionClass() {
        return LineNonReciprocalImpedance.class;
    }

    @Override
    protected LineNonReciprocalImpedanceImpl createExtension(Line line) {
        if (Double.isNaN(r21) || Double.isNaN(x21)) {
            throw new PowsyblException("Line '" + line.getId() + "': r21 and x21 are required");
        }
        return new LineNonReciprocalImpedanceImpl(line, r21, x21);
    }

    @Override
    public LineNonReciprocalImpedanceAdderImpl withR21(double r21) {
        this.r21 = r21;
        return this;
    }

    @Override
    public LineNonReciprocalImpedanceAdderImpl withX21(double x21) {
        this.x21 = x21;
        return this;
    }
}
