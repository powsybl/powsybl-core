/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.AbstractExtensionXmlSerializer;
import com.powsybl.commons.extensions.ExtensionXmlSerializer;
import com.powsybl.commons.xml.XmlReaderContext;
import com.powsybl.commons.xml.XmlWriterContext;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.test.LoadFooExt;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
@AutoService(ExtensionXmlSerializer.class)
public class LoadFooXmlSerializer extends AbstractExtensionXmlSerializer<Load, LoadFooExt> {

    public LoadFooXmlSerializer() {
        super("loadFoo", "network", LoadFooExt.class, false, "loadFoo.xsd",
                "http://www.itesla_project.eu/schema/iidm/ext/loadfoo/1_0", "foo");
    }

    @Override
    public void write(LoadFooExt loadFoo, XmlWriterContext context) {
    }

    @Override
    public LoadFooExt read(Load load, XmlReaderContext context) {
        return new LoadFooExt(load);
    }
}
