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
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.extensions.LoadAsymmetrical;
import com.powsybl.iidm.network.extensions.LoadAsymmetricalAdder;

/**
 * @author Jean-Baptiste Heyberger {@literal <jbheyberger at gmail.com>}
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
@AutoService(ExtensionAdderProvider.class)
public class LoadAsymmetricalAdderImplProvider implements ExtensionAdderProvider<Load, LoadAsymmetrical, LoadAsymmetricalAdder> {

    @Override
    public String getImplementationName() {
        return "Default";
    }

    @Override
    public String getExtensionName() {
        return LoadAsymmetrical.NAME;
    }

    @Override
    public Class<LoadAsymmetricalAdder> getAdderClass() {
        return LoadAsymmetricalAdder.class;
    }

    @Override
    public LoadAsymmetricalAdder newAdder(Load load) {
        return new LoadAsymmetricalAdderImpl(load);
    }
}
