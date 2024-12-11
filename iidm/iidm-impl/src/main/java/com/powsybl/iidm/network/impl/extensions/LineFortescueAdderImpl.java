/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.extensions.LineFortescue;
import com.powsybl.iidm.network.extensions.LineFortescueAdder;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Jean-Baptiste Heyberger {@literal <jbheyberger at gmail.com>}
 */
public class LineFortescueAdderImpl extends AbstractExtensionAdder<Line, LineFortescue> implements LineFortescueAdder {

    private double rz = Double.NaN;

    private double xz = Double.NaN;

    public LineFortescueAdderImpl(Line line) {
        super(line);
    }

    @Override
    public Class<? super LineFortescue> getExtensionClass() {
        return LineFortescue.class;
    }

    @Override
    protected LineFortescueImpl createExtension(Line line) {
        return new LineFortescueImpl(line, rz, xz);
    }

    @Override
    public LineFortescueAdderImpl withRz(double rz) {
        this.rz = rz;
        return this;
    }

    @Override
    public LineFortescueAdderImpl withXz(double xz) {
        this.xz = xz;
        return this;
    }
}
