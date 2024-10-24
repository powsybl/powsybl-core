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
import com.powsybl.iidm.network.EnergySource;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import static com.powsybl.cgmes.model.CgmesNamespace.RDF_NAMESPACE;

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
public final class GeneratingUnitEq {

    private static final String EQ_GENERATINGUNIT_MINP = "GeneratingUnit.minOperatingP";
    private static final String EQ_GENERATINGUNIT_MAXP = "GeneratingUnit.maxOperatingP";
    private static final String EQ_GENERATINGUNIT_INITIALP = "GeneratingUnit.initialP";

    public static void write(String id, String generatingUnitName, EnergySource energySource, double minP, double maxP, double initialP, String cimNamespace, boolean writeInitialP,
                             String equipmentContainer, String hydroPowerPlantId, String windGenUnitType, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartIdName(generatingUnitClassName(energySource), id, generatingUnitName, cimNamespace, writer, context);
        writer.writeStartElement(cimNamespace, EQ_GENERATINGUNIT_MINP);
        writer.writeCharacters(CgmesExportUtil.format(minP));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, EQ_GENERATINGUNIT_MAXP);
        writer.writeCharacters(CgmesExportUtil.format(maxP));
        writer.writeEndElement();
        if (writeInitialP) {
            writer.writeStartElement(cimNamespace, EQ_GENERATINGUNIT_INITIALP);
            writer.writeCharacters(CgmesExportUtil.format(initialP));
            writer.writeEndElement();
        }
        if (equipmentContainer != null) {
            CgmesExportUtil.writeReference("Equipment.EquipmentContainer", equipmentContainer, cimNamespace, writer, context);
        }
        if (energySource == EnergySource.WIND) {
            writer.writeEmptyElement(cimNamespace, "WindGeneratingUnit.windGenUnitType");
            writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, String.format("%s%s", cimNamespace, "WindGenUnitKind." + windGenUnitType));
        }
        if (hydroPowerPlantId != null) {
            CgmesExportUtil.writeReference("HydroGeneratingUnit.HydroPowerPlant", hydroPowerPlantId, cimNamespace, writer, context);
        }
        writer.writeEndElement();
    }

    private static String generatingUnitClassName(EnergySource energySource) {
        if (EnergySource.HYDRO.equals(energySource)) {
            return "HydroGeneratingUnit";
        } else if (EnergySource.NUCLEAR.equals(energySource)) {
            return "NuclearGeneratingUnit";
        } else if (EnergySource.THERMAL.equals(energySource)) {
            return "ThermalGeneratingUnit";
        } else if (EnergySource.WIND.equals(energySource)) {
            return "WindGeneratingUnit";
        } else if (EnergySource.SOLAR.equals(energySource)) {
            return "SolarGeneratingUnit";
        } else if (EnergySource.OTHER.equals(energySource)) {
            return "GeneratingUnit";
        }
        return "GeneratingUnit";
    }

    private GeneratingUnitEq() {
    }
}
