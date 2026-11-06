/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.export.elements;

import com.powsybl.cgmes.conversion.export.CgmesExportContext;
import com.powsybl.cgmes.conversion.export.CgmesExportUtil;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import static com.powsybl.cgmes.model.CgmesNames.*;

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
public final class CgmesSynchronousMachine {

    private static final String ROTATING_MACHINE_GENERATING_UNIT = "RotatingMachine." + GENERATING_UNIT;
    private static final String SYNCHRONOUS_MACHINE_INITIAL_REACTIVE_CAPABILITY_CURVE = SYNCHRONOUS_MACHINE + ".InitialReactiveCapabilityCurve";
    private static final String SYNCHRONOUS_MACHINE_MIN_Q = SYNCHRONOUS_MACHINE + ".minQ";
    private static final String SYNCHRONOUS_MACHINE_MAX_Q = SYNCHRONOUS_MACHINE + ".maxQ";
    private static final String ROTATING_MACHINE_RATED_S = "RotatingMachine." + RATEDS;
    private static final String SYNCHRONOUS_MACHINE_TYPE = SYNCHRONOUS_MACHINE + ".type";
    private static final String SYNCHRONOUS_MACHINE_KIND = "SynchronousMachineKind.";

    private CgmesSynchronousMachine() {
    }

    public static void writeEq(SynchronousMachineEq synchronousMachine, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartId(SYNCHRONOUS_MACHINE, synchronousMachine.rdfId, true, writer, context);
        CgmesExportUtil.writeAttribute(IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(synchronousMachine.name), writer, context);
        CgmesExportUtil.writeResource(EQUIPMENT_EQUIPMENT_CONTAINER, synchronousMachine.equipmentContainer, writer, context);
        if (synchronousMachine.generatingUnit != null) {
            // Some synchronous machines are not associated to a generating unit (condensers)
            CgmesExportUtil.writeResource(ROTATING_MACHINE_GENERATING_UNIT, synchronousMachine.generatingUnit, writer, context);
        }
        if (synchronousMachine.regulatingControl != null) {
            CgmesExportUtil.writeResource(REGULATING_COND_EQ_REGULATING_CONTROL, synchronousMachine.regulatingControl, writer, context);
        }
        if (synchronousMachine.initialReactiveCapabilityCurve != null) {
            CgmesExportUtil.writeResource(SYNCHRONOUS_MACHINE_INITIAL_REACTIVE_CAPABILITY_CURVE, synchronousMachine.initialReactiveCapabilityCurve, writer, context);
        }
        if (synchronousMachine.minQ != null) {
            CgmesExportUtil.writeAttribute(SYNCHRONOUS_MACHINE_MIN_Q, CgmesExportUtil.format(synchronousMachine.minQ), writer, context);
        }
        if (synchronousMachine.maxQ != null) {
            CgmesExportUtil.writeAttribute(SYNCHRONOUS_MACHINE_MAX_Q, CgmesExportUtil.format(synchronousMachine.maxQ), writer, context);
        }
        CgmesExportUtil.writeAttribute(ROTATING_MACHINE_RATED_S, synchronousMachine.ratedS, writer, context);
        CgmesExportUtil.writeResource(SYNCHRONOUS_MACHINE_TYPE, context.getCim().getNamespace(),
            context.getCim().getNamespace() + SYNCHRONOUS_MACHINE_KIND + synchronousMachine.type, writer);
        CgmesExportUtil.writeEnd(writer);
    }

    public record SynchronousMachineEq(
        String rdfId,
        String name,
        Double maxQ,
        Double minQ,
        String type,
        String ratedS,
        String initialReactiveCapabilityCurve,
        String generatingUnit,
        String regulatingControl,
        String equipmentContainer
    ) {
    }

}
