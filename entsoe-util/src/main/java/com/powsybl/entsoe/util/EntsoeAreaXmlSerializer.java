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
import com.powsybl.iidm.network.Substation;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(ExtensionXmlSerializer.class)
public class EntsoeAreaXmlSerializer extends AbstractExtensionXmlSerializer<Substation, EntsoeArea> {

    public EntsoeAreaXmlSerializer() {
        super("entsoeArea", "network", EntsoeArea.class, true, "entsoeArea.xsd",
                "http://www.itesla_project.eu/schema/iidm/ext/entsoe_area/1_0", "ea");
    }

    @Override
    public void write(EntsoeArea country, XmlWriterContext context) {
        context.getWriter().writeNodeContent(country.getCode().name());
    }

    @Override
    public EntsoeArea read(Substation substation, XmlReaderContext context) {
        EntsoeGeographicalCode code = EntsoeGeographicalCode.valueOf(context.getReader().readUntilEndNode(getExtensionName(), null));
        substation.newExtension(EntsoeAreaAdder.class).withCode(code).add();
        return substation.getExtension(EntsoeArea.class);
    }
}
