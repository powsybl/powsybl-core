/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.entsoe.util;

import com.powsybl.commons.extensions.ExtensionAdder;
import com.powsybl.iidm.network.BoundaryLine;
import com.powsybl.iidm.network.DanglingLine;

/**
 * @author Jon Harper <jon.harper at rte-france.com>
 */
public interface XnodeAdder extends ExtensionAdder<BoundaryLine, Xnode> {

    @Override
    default Class<Xnode> getExtensionClass() {
        return Xnode.class;
    }

    // No need for CRTP style return type returning a more specific adder
    // because this interface is not meant to be extended.
    XnodeAdder withCode(String code);
}
