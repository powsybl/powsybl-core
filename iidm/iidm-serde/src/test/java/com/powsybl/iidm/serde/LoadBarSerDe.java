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
import com.powsybl.iidm.network.test.LoadBarExt;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
@AutoService(ExtensionSerDe.class)
public class LoadBarSerDe extends AbstractExtensionSerDe<Load, LoadBarExt> {

    public LoadBarSerDe() {
        super("loadBar", "network", LoadBarExt.class, "loadBar.xsd",
                "http://www.itesla_project.eu/schema/iidm/ext/loadbar/1_0", "bar");
    }

    @Override
    public void write(LoadBarExt loadBar, SerializerContext context) {
        // empty extension
    }

    @Override
    public LoadBarExt read(Load load, DeserializerContext context) {
        context.getReader().readEndNode();
        var ext = new LoadBarExt(load);
        load.addExtension(LoadBarExt.class, ext);
        return ext;
    }
}
