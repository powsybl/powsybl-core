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
public final class CgmesGeneratingUnit {

    private static final String GENERATING_UNIT_MIN_OPERATING_P = GENERATING_UNIT + ".minOperatingP";
    private static final String GENERATING_UNIT_MAX_OPERATING_P = GENERATING_UNIT + ".maxOperatingP";
    private static final String GENERATING_UNIT_INITIAL_P = GENERATING_UNIT + "." + INITIAL_P;
    private static final String WIND_GENERATING_UNIT = "WindGeneratingUnit";
    private static final String WIND_GENERATING_UNIT_WIND_GEN_UNIT_TYPE = WIND_GENERATING_UNIT + ".windGenUnitType";
    private static final String WIND_GEN_UNIT_KIND = "WindGenUnitKind.";
    private static final String HYDRO_GENERATING_UNIT_HYDRO_POWER_PLANT = "HydroGeneratingUnit.HydroPowerPlant";

    private CgmesGeneratingUnit() {
    }

    public static void writeEq(GeneratingUnitEq generatingUnit, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartId(generatingUnit.className, generatingUnit.rdfId, true, writer, context);
        CgmesExportUtil.writeAttribute(IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(generatingUnit.name), writer, context);
        CgmesExportUtil.writeAttribute(GENERATING_UNIT_MIN_OPERATING_P, CgmesExportUtil.format(generatingUnit.minOperatingP), writer, context);
        CgmesExportUtil.writeAttribute(GENERATING_UNIT_MAX_OPERATING_P, CgmesExportUtil.format(generatingUnit.maxOperatingP), writer, context);
        if (context.getCimVersion() == 16) {
            CgmesExportUtil.writeAttribute(GENERATING_UNIT_INITIAL_P, CgmesExportUtil.format(generatingUnit.initialP), writer, context);
        }
        if (generatingUnit.equipmentContainer != null) {
            CgmesExportUtil.writeResource(EQUIPMENT_EQUIPMENT_CONTAINER, generatingUnit.equipmentContainer, writer, context);
        }
        if (WIND_GENERATING_UNIT.equals(generatingUnit.className)) {
            CgmesExportUtil.writeResource(WIND_GENERATING_UNIT_WIND_GEN_UNIT_TYPE, context.getCim().getNamespace(),
                context.getCim().getNamespace() + WIND_GEN_UNIT_KIND + generatingUnit.windGenUnitType, writer);
        }
        if (generatingUnit.hydroPowerPlant != null) {
            CgmesExportUtil.writeResource(HYDRO_GENERATING_UNIT_HYDRO_POWER_PLANT, generatingUnit.hydroPowerPlant, writer, context);
        }
        CgmesExportUtil.writeEnd(writer);
    }

    public record GeneratingUnitEq(
        String className,
        String rdfId,
        String name,
        double initialP,
        double maxOperatingP,
        double minOperatingP,
        String windGenUnitType,
        String equipmentContainer,
        String hydroPowerPlant
    ) {
    }

}
