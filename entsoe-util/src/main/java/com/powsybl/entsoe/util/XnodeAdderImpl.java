/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.entsoe.util;

import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.DanglingLine;

/**
 * @author Jon Harper <jon.harper at rte-france.com>
 */
public class XnodeAdderImpl extends AbstractExtensionAdder<DanglingLine, Xnode>
        implements XnodeAdder {

    private String code;

    public XnodeAdderImpl(DanglingLine extendable) {
        super(extendable);
    }

    @Override
    public XnodeAdderImpl withCode(String code) {
        this.code = code;
        return this;
    }

    @Override
    public XnodeImpl createExtension(DanglingLine danglingLine) {
        return new XnodeImpl(danglingLine, code);
    }

}

