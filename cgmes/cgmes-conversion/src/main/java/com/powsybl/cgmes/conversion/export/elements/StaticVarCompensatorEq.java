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
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.extensions.VoltagePerReactivePowerControl;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import static com.powsybl.cgmes.model.CgmesNamespace.RDF_NAMESPACE;

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
public final class StaticVarCompensatorEq {

    private static final String EQ_STATICVARCOMPENSATOR_INDUCTIVERATING = "StaticVarCompensator.inductiveRating";
    private static final String EQ_STATICVARCOMPENSATOR_CAPACITIVERATING = "StaticVarCompensator.capacitiveRating";
    private static final String EQ_STATICVARCOMPENSATOR_SLOPE = "StaticVarCompensator.slope";
    private static final String EQ_STATICVARCOMPENSATOR_SVCCONTROLMODE = "StaticVarCompensator.sVCControlMode";
    private static final String EQ_STATICVARCOMPENSATOR_VOLTAGESETPOINT = "StaticVarCompensator.voltageSetPoint";

    public static void write(String id, String svcName, String equipmentContainer, String regulatingControlId, double inductiveRating, double capacitiveRating, VoltagePerReactivePowerControl voltagePerReactivePowerControl,
                             StaticVarCompensator.RegulationMode svcControlMode, double voltageSetPoint, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartIdName("StaticVarCompensator", id, svcName, cimNamespace, writer, context);
        CgmesExportUtil.writeReference("Equipment.EquipmentContainer", equipmentContainer, cimNamespace, writer, context);
        if (regulatingControlId != null) {
            CgmesExportUtil.writeReference("RegulatingCondEq.RegulatingControl", regulatingControlId, cimNamespace, writer, context);
        }
        writer.writeStartElement(cimNamespace, EQ_STATICVARCOMPENSATOR_INDUCTIVERATING);
        writer.writeCharacters(CgmesExportUtil.format(inductiveRating));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, EQ_STATICVARCOMPENSATOR_CAPACITIVERATING);
        writer.writeCharacters(CgmesExportUtil.format(capacitiveRating));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, EQ_STATICVARCOMPENSATOR_SLOPE);
        if (voltagePerReactivePowerControl != null) {
            writer.writeCharacters(CgmesExportUtil.format(voltagePerReactivePowerControl.getSlope()));
        } else {
            writer.writeCharacters(CgmesExportUtil.format(0.0));
        }
        writer.writeEndElement();
        writer.writeEmptyElement(cimNamespace, EQ_STATICVARCOMPENSATOR_SVCCONTROLMODE);
        if (svcControlMode != null) {
            writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, cimNamespace + regulationMode(svcControlMode));
        }
        writer.writeStartElement(cimNamespace, EQ_STATICVARCOMPENSATOR_VOLTAGESETPOINT);
        writer.writeCharacters(CgmesExportUtil.format(voltageSetPoint));
        writer.writeEndElement();
        writer.writeEndElement();
    }

    private static String regulationMode(StaticVarCompensator.RegulationMode svcControlMode) {
        if (StaticVarCompensator.RegulationMode.VOLTAGE.equals(svcControlMode)) {
            return "SVCControlMode.voltage";
        } else if (StaticVarCompensator.RegulationMode.REACTIVE_POWER.equals(svcControlMode)) {
            return "SVCControlMode.reactivePower";
        }
        throw new PowsyblException("Invalid regulation mode for Static Var Compensator " + svcControlMode);
    }

    private StaticVarCompensatorEq() {
    }
}
