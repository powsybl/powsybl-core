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

import java.util.OptionalInt;

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
        context.getWriter().writeOptionalIntAttribute("value", loadBar.getValue());
        if (loadBar.getPoint() != null) {
            context.getWriter().writeStartNode(getNamespaceUri(), "point");
            context.getWriter().writeDoubleAttribute("x", loadBar.getPoint().x());
            context.getWriter().writeDoubleAttribute("y", loadBar.getPoint().y());
            context.getWriter().writeEndNode();
        }
    }

    @Override
    public LoadBarExt read(Load load, DeserializerContext context) {
        LoadBarExt.Point[] point = new LoadBarExt.Point[1];
        OptionalInt value = context.getReader().readOptionalIntAttribute("value");
        context.getReader().readChildNodes(elementName -> {
            var x = context.getReader().readDoubleAttribute("x");
            var y = context.getReader().readDoubleAttribute("y");
            point[0] = new LoadBarExt.Point(x, y);
            context.getReader().readEndNode();
        });
        LoadBarExt loadBarExt = new LoadBarExt(load);
        value.ifPresent(loadBarExt::setValue);
        if (point[0] != null) {
            loadBarExt.setPoint(point[0]);
        }
        load.addExtension(LoadBarExt.class, loadBarExt);
        return loadBarExt;
    }
}
