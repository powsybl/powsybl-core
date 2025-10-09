/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.ExtensionAdder;
import com.powsybl.iidm.network.Generator;

/**
 * @author Jean-Baptiste Heyberger {@literal <jbheyberger at gmail.com>}
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface GeneratorFortescueAdder extends ExtensionAdder<Generator, GeneratorFortescue> {

    GeneratorFortescueAdder withGrounded(boolean grounded);

    GeneratorFortescueAdder withRz(double rz);

    GeneratorFortescueAdder withXz(double xz);

    GeneratorFortescueAdder withRn(double rn);

    GeneratorFortescueAdder withXn(double xn);

    GeneratorFortescueAdder withGroundingR(double groundingR);

    GeneratorFortescueAdder withGroundingX(double groundingX);
}
