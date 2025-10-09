/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.ExtensionAdderProvider;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.extensions.LoadDetail;

/**
 * @author Jérémy Labous {@literal <jlabous at silicom.fr>}
 */
@AutoService(ExtensionAdderProvider.class)
public class LoadDetailAdderImplProvider implements ExtensionAdderProvider<Load, LoadDetail, LoadDetailAdderImpl> {

    @Override
    public String getImplementationName() {
        return "Default";
    }

    @Override
    public String getExtensionName() {
        return LoadDetail.NAME;
    }

    @Override
    public Class<LoadDetailAdderImpl> getAdderClass() {
        return LoadDetailAdderImpl.class;
    }

    @Override
    public LoadDetailAdderImpl newAdder(Load load) {
        return new LoadDetailAdderImpl(load);
    }
}
