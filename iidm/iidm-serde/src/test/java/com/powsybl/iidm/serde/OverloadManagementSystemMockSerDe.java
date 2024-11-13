/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.AbstractExtensionSerDe;
import com.powsybl.commons.extensions.ExtensionSerDe;
import com.powsybl.commons.io.DeserializerContext;
import com.powsybl.commons.io.SerializerContext;
import com.powsybl.iidm.network.OverloadManagementSystem;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
@AutoService(ExtensionSerDe.class)
public class OverloadManagementSystemMockSerDe extends AbstractExtensionSerDe<OverloadManagementSystem, OverloadManagementSystemMockExt> {

    public OverloadManagementSystemMockSerDe() {
        super("overloadManagementSystemMock", "network", OverloadManagementSystemMockExt.class,
                "overloadManagementSystemMock.xsd",
                "http://www.powsybl.org/schema/iidm/ext/overloadmanagementsystem_mock/1_0", "omsmock");
    }

    @Override
    public void write(OverloadManagementSystemMockExt omsFoo, SerializerContext context) {
        context.getWriter().writeStringAttribute("foo", omsFoo.getFoo());
    }

    @Override
    public OverloadManagementSystemMockExt read(OverloadManagementSystem extendable, DeserializerContext context) {
        String foo = context.getReader().readStringAttribute("foo");
        context.getReader().readEndNode();
        return new OverloadManagementSystemMockExt(extendable, foo);
    }

}
