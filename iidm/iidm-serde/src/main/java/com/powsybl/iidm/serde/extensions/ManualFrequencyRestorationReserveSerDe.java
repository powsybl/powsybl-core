/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.AbstractExtensionSerDe;
import com.powsybl.commons.extensions.ExtensionSerDe;
import com.powsybl.commons.io.DeserializerContext;
import com.powsybl.commons.io.SerializerContext;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.extensions.ManualFrequencyRestorationReserve;
import com.powsybl.iidm.network.extensions.ManualFrequencyRestorationReserveAdder;
import com.powsybl.iidm.serde.NetworkDeserializerContext;
import com.powsybl.iidm.serde.NetworkSerializerContext;

import static com.powsybl.iidm.serde.extensions.AbstractVersionableNetworkExtensionSerDe.convertContext;

/**
 * @author Jacques Borsenberger {@literal <jacques.borsenberger at rte-france.com>}
 */
@AutoService(ExtensionSerDe.class)
public class ManualFrequencyRestorationReserveSerDe extends AbstractExtensionSerDe<Generator, ManualFrequencyRestorationReserve> {

    public ManualFrequencyRestorationReserveSerDe() {
        super(ManualFrequencyRestorationReserve.NAME, "network", ManualFrequencyRestorationReserve.class, "manualFrequencyRestorationReserve.xsd",
                "http://www.powsybl.org/schema/iidm/ext/manual_frequency_restoration_reserve/1_0", "mfrr");
    }

    @Override
    public void write(ManualFrequencyRestorationReserve manualFrequencyRestorationReserve, SerializerContext context) {
        NetworkSerializerContext networkContext = convertContext(context);
        networkContext.getWriter().writeBooleanAttribute("participate", manualFrequencyRestorationReserve.isParticipate());
    }

    @Override
    public ManualFrequencyRestorationReserve read(Generator generator, DeserializerContext context) {
        NetworkDeserializerContext networkContext = (NetworkDeserializerContext) context;
        boolean participate = networkContext.getReader().readBooleanAttribute("participate");
        context.getReader().readEndNode();
        return generator.newExtension(ManualFrequencyRestorationReserveAdder.class)
                .withParticipate(participate)
                .add();
    }
}
