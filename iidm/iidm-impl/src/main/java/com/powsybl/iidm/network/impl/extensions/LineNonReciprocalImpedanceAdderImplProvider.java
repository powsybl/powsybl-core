/**
 * Copyright (c) 2026, Kick Stage d.o.o.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.ExtensionAdderProvider;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.extensions.LineNonReciprocalImpedance;
import com.powsybl.iidm.network.extensions.LineNonReciprocalImpedanceAdder;

/**
 * @author Pascal Ehlert {@literal <pascal.ehlert at kickstage.com>}
 */
@AutoService(ExtensionAdderProvider.class)
public class LineNonReciprocalImpedanceAdderImplProvider
        implements ExtensionAdderProvider<Line, LineNonReciprocalImpedance, LineNonReciprocalImpedanceAdder> {

    @Override
    public String getImplementationName() {
        return "Default";
    }

    @Override
    public String getExtensionName() {
        return LineNonReciprocalImpedance.NAME;
    }

    @Override
    public Class<LineNonReciprocalImpedanceAdder> getAdderClass() {
        return LineNonReciprocalImpedanceAdder.class;
    }

    @Override
    public LineNonReciprocalImpedanceAdder newAdder(Line extendable) {
        return new LineNonReciprocalImpedanceAdderImpl(extendable);
    }
}
