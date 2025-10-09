/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.ExtensionAdderProvider;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.extensions.LineFortescue;
import com.powsybl.iidm.network.extensions.LineFortescueAdder;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
@AutoService(ExtensionAdderProvider.class)
public class LineFortescueAdderImplProvider
        implements ExtensionAdderProvider<Line, LineFortescue, LineFortescueAdder> {

    @Override
    public String getImplementationName() {
        return "Default";
    }

    @Override
    public String getExtensionName() {
        return LineFortescue.NAME;
    }

    @Override
    public Class<LineFortescueAdder> getAdderClass() {
        return LineFortescueAdder.class;
    }

    @Override
    public LineFortescueAdder newAdder(Line extendable) {
        return new LineFortescueAdderImpl(extendable);
    }
}
