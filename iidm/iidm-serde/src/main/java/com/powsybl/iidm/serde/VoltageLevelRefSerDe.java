/**
 * Copyright (c) 2024, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.iidm.network.*;

import java.util.function.Consumer;

/**
 * @author Marine Guibert {@literal <marine.guibert at artelys.com>}
 * @author Valentin Mouradian {@literal <valentin.mouradian at artelys.com>}
 */
public final class VoltageLevelRefSerDe {
    private static final String ID = "id";

    public static final String ROOT_ELEMENT_NAME = "voltageLevelRef";

    public static void readVoltageLevelRef(NetworkDeserializerContext context, Network network, Consumer<VoltageLevel> endTaskTerminalConsumer) {
        String id = context.getAnonymizer().deanonymizeString(context.getReader().readStringAttribute(ID));
        context.getReader().readEndNode();
        context.getEndTasks().add(() -> {
            VoltageLevel voltageLevel = network.getVoltageLevel(id);
            endTaskTerminalConsumer.accept(voltageLevel);
        });
    }

    public static void writeVoltageLevelRef(VoltageLevel voltageLevel, NetworkSerializerContext context) {
        context.getWriter().writeStartNode(context.getVersion().getNamespaceURI(context.isValid()), ROOT_ELEMENT_NAME);
        context.getWriter().writeStringAttribute(ID, context.getAnonymizer().anonymizeString(voltageLevel.getId()));
        context.getWriter().writeEndNode();
    }

    private VoltageLevelRefSerDe() {
    }
}
