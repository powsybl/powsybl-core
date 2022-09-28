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
import com.powsybl.commons.extensions.XmlReaderContext;
import com.powsybl.commons.extensions.XmlWriterContext;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.test.LoadBarExt;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
@AutoService(ExtensionXmlSerializer.class)
public class LoadBarXmlSerializer extends AbstractExtensionXmlSerializer<Load, LoadBarExt> {

    public LoadBarXmlSerializer() {
        super("loadBar", "network", LoadBarExt.class, false, "loadBar.xsd",
                "http://www.itesla_project.eu/schema/iidm/ext/loadbar/1_0", "bar");
    }

    @Override
    public void write(LoadBarExt loadBar, XmlWriterContext context) {
    }

    @Override
    public LoadBarExt read(Load load, XmlReaderContext context) {
        return new LoadBarExt(load);
    }
}
