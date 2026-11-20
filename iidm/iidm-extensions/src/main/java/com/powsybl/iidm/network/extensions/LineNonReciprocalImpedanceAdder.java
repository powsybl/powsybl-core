/**
 * Copyright (c) 2026, Kick Stage d.o.o.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.ExtensionAdder;
import com.powsybl.iidm.network.Line;

/**
 * @author Pascal Ehlert {@literal <pascal.ehlert at kickstage.com>}
 */
public interface LineNonReciprocalImpedanceAdder extends ExtensionAdder<Line, LineNonReciprocalImpedance> {

    LineNonReciprocalImpedanceAdder withR21(double r21);

    LineNonReciprocalImpedanceAdder withX21(double x21);
}
