/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.serializer;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.AbstractExtensionSerializer;
import com.powsybl.commons.extensions.ExtensionSerializer;
import com.powsybl.commons.io.ReaderContext;
import com.powsybl.commons.io.WriterContext;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.test.LoadBarExt;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
@AutoService(ExtensionSerializer.class)
public class LoadBarSerializer extends AbstractExtensionSerializer<Load, LoadBarExt> {

    public LoadBarSerializer() {
        super("loadBar", "network", LoadBarExt.class, "loadBar.xsd",
                "http://www.itesla_project.eu/schema/iidm/ext/loadbar/1_0", "bar");
    }

    @Override
    public void write(LoadBarExt loadBar, WriterContext context) {
    }

    @Override
    public LoadBarExt read(Load load, ReaderContext context) {
        context.getReader().readEndNode();
        return new LoadBarExt(load);
    }
}
