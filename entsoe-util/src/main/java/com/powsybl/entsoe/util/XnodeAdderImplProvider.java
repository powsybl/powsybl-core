/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.entsoe.util;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.ExtensionAdderProvider;
import com.powsybl.iidm.network.DanglingLine;

/**
 * @author Jon Harper <jon.harper at rte-france.com>
 */
@AutoService(ExtensionAdderProvider.class)
public class XnodeAdderImplProvider implements ExtensionAdderProvider<DanglingLine, Xnode, XnodeAdderImpl> {

    @Override
    public String getImplementationName() {
        return "Default";
    }

    @Override
    public String getExtensionsName() {
        return Xnode.NAME;
    }

    @Override
    public Class<XnodeAdderImpl> getAdderClass() {
        return XnodeAdderImpl.class;
    }

    @Override
    public XnodeAdderImpl newAdder(DanglingLine extendable) {
        return new XnodeAdderImpl(extendable);
    }

}
