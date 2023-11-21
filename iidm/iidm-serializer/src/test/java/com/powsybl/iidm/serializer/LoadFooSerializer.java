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
import com.powsybl.iidm.network.test.LoadFooExt;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
@AutoService(ExtensionSerializer.class)
public class LoadFooSerializer extends AbstractExtensionSerializer<Load, LoadFooExt> {

    public LoadFooSerializer() {
        super("loadFoo", "network", LoadFooExt.class, "loadFoo.xsd",
                "http://www.itesla_project.eu/schema/iidm/ext/loadfoo/1_0", "foo");
    }

    @Override
    public void write(LoadFooExt loadFoo, WriterContext context) {
    }

    @Override
    public LoadFooExt read(Load load, ReaderContext context) {
        context.getReader().readEndNode();
        return new LoadFooExt(load);
    }
}
