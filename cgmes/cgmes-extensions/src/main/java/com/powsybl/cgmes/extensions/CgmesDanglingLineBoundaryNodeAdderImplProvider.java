/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.ExtensionAdderProvider;
import com.powsybl.iidm.network.DanglingLine;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
@AutoService(ExtensionAdderProvider.class)
public class CgmesDanglingLineBoundaryNodeAdderImplProvider implements ExtensionAdderProvider<DanglingLine, CgmesDanglingLineBoundaryNode, CgmesDanglingLineBoundaryNodeAdderImpl> {
    @Override
    public String getImplementationName() {
        return "Default";
    }

    @Override
    public String getExtensionName() {
        return CgmesDanglingLineBoundaryNode.NAME;
    }

    @Override
    public Class<? super CgmesDanglingLineBoundaryNodeAdderImpl> getAdderClass() {
        return CgmesDanglingLineBoundaryNodeAdderImpl.class;
    }

    @Override
    public CgmesDanglingLineBoundaryNodeAdderImpl newAdder(DanglingLine extendable) {
        return new CgmesDanglingLineBoundaryNodeAdderImpl(extendable);
    }
}
