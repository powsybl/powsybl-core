/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
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
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.test.VoltageLevelFooExt;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
@AutoService(ExtensionSerDe.class)
public class VoltageLevelFooSerDe extends AbstractExtensionSerDe<VoltageLevel, VoltageLevelFooExt> {

    public VoltageLevelFooSerDe() {
        super("voltageLevelFoo", "network", VoltageLevelFooExt.class, "voltageLevelFoo.xsd",
                "http://www.itesla_project.eu/schema/iidm/ext/voltagelevelfoo/1_0", "foo");
    }

    @Override
    public void write(VoltageLevelFooExt voltageLevelFoo, SerializerContext context) {
    }

    @Override
    public VoltageLevelFooExt read(VoltageLevel voltageLevel, DeserializerContext context) {
        context.getReader().readEndNode();
        var ext = new VoltageLevelFooExt(voltageLevel);
        voltageLevel.addExtension(VoltageLevelFooExt.class, ext);
        return ext;
    }
}
