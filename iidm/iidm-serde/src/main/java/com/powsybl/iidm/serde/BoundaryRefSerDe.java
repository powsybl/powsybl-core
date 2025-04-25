/**
 * Copyright (c) 2024, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.iidm.network.Boundary;
import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.Network;

import java.util.function.Consumer;

/**
 * @author Marine Guibert {@literal <marine.guibert at artelys.com>}
 * @author Valentin Mouradian {@literal <valentin.mouradian at artelys.com>}
 */
public final class BoundaryRefSerDe {

    private static final String ID = "id";

    public static final String ROOT_ELEMENT_NAME = "boundaryRef";

    public static void readBoundaryRef(NetworkDeserializerContext context, Network network, Consumer<Boundary> endTaskTerminalConsumer) {
        String id = context.getAnonymizer().deanonymizeString(context.getReader().readStringAttribute(ID));
        context.getReader().readEndNode();
        context.addEndTask(DeserializationEndTask.Step.AFTER_EXTENSIONS, () -> {
            DanglingLine danglingLine = network.getDanglingLine(id);
            endTaskTerminalConsumer.accept(danglingLine.getBoundary());
        });
    }

    public static void writeBoundaryRefAttributes(Boundary boundary, NetworkSerializerContext context) {
        context.getWriter().writeStringAttribute(ID, context.getAnonymizer().anonymizeString(boundary.getDanglingLine().getId()));
    }

    private BoundaryRefSerDe() {
    }
}
