/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
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
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.test.LoadFooExt;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
@AutoService(ExtensionSerDe.class)
public class LoadFooSerDe extends AbstractExtensionSerDe<Load, LoadFooExt> {

    public LoadFooSerDe() {
        super("loadFoo", "network", LoadFooExt.class, "loadFoo.xsd",
                "http://www.itesla_project.eu/schema/iidm/ext/loadfoo/1_0", "foo");
    }

    @Override
    public void write(LoadFooExt loadFoo, SerializerContext context) {
    }

    @Override
    public LoadFooExt read(Load load, DeserializerContext context) {
        context.getReader().readEndNode();
        var ext = new LoadFooExt(load);
        load.addExtension(LoadFooExt.class, ext);
        return ext;
    }
}
