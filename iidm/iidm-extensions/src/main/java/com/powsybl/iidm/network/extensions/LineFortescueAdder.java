/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.ExtensionAdder;
import com.powsybl.iidm.network.Line;

/**
 * @author Jean-Baptiste Heyberger {@literal <jbheyberger at gmail.com>}
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface LineFortescueAdder extends ExtensionAdder<Line, LineFortescue> {

    LineFortescueAdder withRz(double rz);

    LineFortescueAdder withXz(double xz);

    LineFortescueAdder withOpenPhaseA(boolean openPhaseA);

    LineFortescueAdder withOpenPhaseB(boolean openPhaseB);

    LineFortescueAdder withOpenPhaseC(boolean openPhaseC);
}
