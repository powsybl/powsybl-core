/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.entsoe.util;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.AbstractExtensionXmlSerializer;
import com.powsybl.commons.extensions.ExtensionXmlSerializer;
import com.powsybl.commons.extensions.XmlReaderContext;
import com.powsybl.commons.extensions.XmlWriterContext;
import com.powsybl.iidm.network.DanglingLine;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(ExtensionXmlSerializer.class)
public class XnodeXmlSerializer extends AbstractExtensionXmlSerializer<DanglingLine, Xnode> {

    public XnodeXmlSerializer() {
        super("xnode", "network", Xnode.class, false, "xnode.xsd",
                "http://www.itesla_project.eu/schema/iidm/ext/xnode/1_0", "xn");
    }

    @Override
    public void write(Xnode xnode, XmlWriterContext context) {
        context.getWriter().writeStringAttribute("code", xnode.getCode());
    }

    @Override
    public Xnode read(DanglingLine dl, XmlReaderContext context) {
        String code = context.getReader().readStringAttribute("code");
        dl.newExtension(XnodeAdder.class).withCode(code).add();
        return dl.getExtension(Xnode.class);
    }
}
