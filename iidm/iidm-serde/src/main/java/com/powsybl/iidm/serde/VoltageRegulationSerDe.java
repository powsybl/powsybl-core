/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.iidm.serde;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.io.TreeDataWriter;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.regulation.VoltageRegulation;
import com.powsybl.iidm.network.regulation.VoltageRegulationHolder;

import java.util.Optional;

/**
 * @author Matthieu Saur {@literal <matthieu.saur@rte-france.com>}
 */
public final class VoltageRegulationSerDe {

    private static final String ID = "id";
    private static final String MSA = "msa";
    private static final String MSA2 = "msa2";

    public static void writeVoltageRegulation(VoltageRegulation vr, NetworkSerializerContext context, String elementName) {
        writeVoltageRegulation(vr, context, context.getVersion().getNamespaceURI(context.isValid()), elementName);
    }

    public static void writeVoltageRegulation(VoltageRegulation vr, NetworkSerializerContext context, String namespace, String elementName) {
        writeVoltageRegulation(vr, context, namespace, elementName, context.getWriter());
    }

    public static void writeVoltageRegulation(VoltageRegulation vr, NetworkSerializerContext context, String namespace, String elementName, TreeDataWriter writer) {
        if (vr != null) {
            writer.writeStartNode(namespace, elementName);
            writeVoltageRegulationAttribute(vr, context, writer);
            writer.writeEndNode();
        }
    }

    public static void writeVoltageRegulationAttribute(VoltageRegulation vr, NetworkSerializerContext context) {
        writeVoltageRegulationAttribute(vr, context, context.getWriter());
    }

    public static void writeVoltageRegulationAttribute(VoltageRegulation voltageRegulation, NetworkSerializerContext context, TreeDataWriter writer) {
        writer.writeDoubleAttribute(MSA, 28.4);
        writer.writeDoubleAttribute(MSA2, 2.0);
    }

    private static void checkVoltageRegulation(VoltageRegulation voltageRegulation, NetworkSerializerContext context) {
    }

    public static VoltageRegulation readVoltageRegulation(NetworkDeserializerContext context, Network n, VoltageRegulationHolder holder) {
        Double msa = context.getReader().readDoubleAttribute(MSA, Double.NaN);
        Double msa2 = context.getReader().readDoubleAttribute(MSA2, Double.NaN);
        context.getReader().readEndNode();
        holder.setVoltageRegulation();
        return VoltageRegulationSerDe.resolve(id, msa, msa2, n);
    }

    private VoltageRegulationSerDe() {
    }
}
