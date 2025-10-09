/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.entsoe.util;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.AbstractExtensionSerDe;
import com.powsybl.commons.extensions.ExtensionSerDe;
import com.powsybl.commons.io.DeserializerContext;
import com.powsybl.commons.io.SerializerContext;
import com.powsybl.iidm.network.Substation;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
@AutoService(ExtensionSerDe.class)
public class EntsoeAreaSerDe extends AbstractExtensionSerDe<Substation, EntsoeArea> {

    public EntsoeAreaSerDe() {
        super("entsoeArea", "network", EntsoeArea.class, "entsoeArea.xsd",
                "http://www.itesla_project.eu/schema/iidm/ext/entsoe_area/1_0", "ea");
    }

    @Override
    public void write(EntsoeArea country, SerializerContext context) {
        context.getWriter().writeNodeContent(country.getCode().name());
    }

    @Override
    public EntsoeArea read(Substation substation, DeserializerContext context) {
        EntsoeGeographicalCode code = EntsoeGeographicalCode.valueOf(context.getReader().readContent());
        substation.newExtension(EntsoeAreaAdder.class).withCode(code).add();
        return substation.getExtension(EntsoeArea.class);
    }
}
