/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.elements;

import com.powsybl.cgmes.conversion.CgmesReports;
import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.model.CgmesModelException;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.iidm.network.*;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public class VoltageLevelConversion extends AbstractIdentifiedObjectConversion {

    public VoltageLevelConversion(PropertyBag vl, Context context) {
        super(CgmesNames.VOLTAGE_LEVEL, vl, context);
        cgmesSubstationId = p.getId("Substation");
        iidmSubstationId = context.nodeContainerMapping().substationIidm(cgmesSubstationId);
        substation = context.network().getSubstation(iidmSubstationId);
    }

    @Override
    public boolean valid() {
        double nominalVoltage = p.asDouble("nominalVoltage");
        if (nominalVoltage == 0) {
            CgmesReports.nominalVoltageIsZeroReport(context.getReportNode(), id);
            ignored("Voltage level", () -> String.format("nominal voltage of %s is equal to 0", id));
            return false;
        }
        if (substation == null) {
            CgmesReports.missingMandatoryAttributeReport(context.getReportNode(), "Substation", CgmesNames.VOLTAGE_LEVEL, id);
            missing(String.format("Substation %s (IIDM id: %s)",
                    cgmesSubstationId,
                    iidmSubstationId));
            return false;
        }
        return !context.nodeContainerMapping().voltageLevelIsMapped(id);
    }

    @Override
    public void convert() {
        String baseVoltage = p.getId("BaseVoltage");
        double nominalVoltage = p.asDouble("nominalVoltage");
        double lowVoltageLimit = p.asDouble("lowVoltageLimit");
        double highVoltageLimit = p.asDouble("highVoltageLimit");

        // Missing elements in the boundary file
        if (Double.isNaN(nominalVoltage)) {
            String bv = String.format("BaseVoltage %s", baseVoltage);
            missing(bv);
            throw new CgmesModelException(String.format("nominalVoltage not found for %s", bv));
        }

        String iidmVoltageLevelId = context.nodeContainerMapping().voltageLevelIidm(id);
        VoltageLevel voltageLevel = context.network().getVoltageLevel(iidmVoltageLevelId);
        if (voltageLevel == null) {
            VoltageLevelAdder adder = substation.newVoltageLevel()
                    .setNominalV(nominalVoltage)
                    .setTopologyKind(
                            context.nodeBreaker()
                                    ? TopologyKind.NODE_BREAKER
                                    : TopologyKind.BUS_BREAKER)
                    .setLowVoltageLimit(lowVoltageLimit)
                    .setHighVoltageLimit(highVoltageLimit);
            identify(adder);
            VoltageLevel vl = adder.add();
            addAliases(vl);

            vl.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.HIGH_VOLTAGE_LIMIT, String.valueOf(highVoltageLimit));
            vl.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.LOW_VOLTAGE_LIMIT, String.valueOf(lowVoltageLimit));
        }
    }

    private void addAliases(VoltageLevel vl) {
        int index = 0;
        for (String mergedVl : context.nodeContainerMapping().mergedVoltageLevels(vl.getId())) {
            index++;
            vl.addAlias(mergedVl, "MergedVoltageLevel" + index, context.config().isEnsureIdAliasUnicity());
        }
    }

    public static void update(VoltageLevel voltageLevel, Context context) {
        OperationalLimitConversion.update(voltageLevel, context);
    }

    private final String cgmesSubstationId;
    private final String iidmSubstationId;
    private final Substation substation;
}
