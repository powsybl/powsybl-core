/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde.extensions.alternatives;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.AbstractExtensionSerDe;
import com.powsybl.commons.extensions.ExtensionSerDe;
import com.powsybl.commons.io.DeserializerContext;
import com.powsybl.commons.io.SerializerContext;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.extensions.OperatingStatus;
import com.powsybl.iidm.network.extensions.OperatingStatusAdder;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
@AutoService(ExtensionSerDe.class)
public class LegacyOperatingStatusSerDe<I extends Identifiable<I>> extends AbstractExtensionSerDe<I, OperatingStatus<I>> {

    public LegacyOperatingStatusSerDe() {
        super("legacyOperatingStatus", "network", OperatingStatus.class,
                "legacyOperatingStatus.xsd", "http://www.powsybl.org/schema/iidm/ext/legacy_operating_status/1_0",
                "los");
    }

    @Override
    public void write(OperatingStatus<I> status, SerializerContext context) {
        context.getWriter().writeNodeContent(status.getStatus().name());
    }

    @Override
    public OperatingStatus<I> read(I identifiable, DeserializerContext context) {
        OperatingStatus.Status status = OperatingStatus.Status.valueOf(context.getReader().readContent());
        OperatingStatusAdder<I> adder = identifiable.newExtension(OperatingStatusAdder.class);
        return adder.withStatus(status)
                .add();
    }
}
