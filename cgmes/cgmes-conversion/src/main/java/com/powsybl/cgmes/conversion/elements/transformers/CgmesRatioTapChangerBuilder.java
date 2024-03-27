/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.elements.transformers;

import java.util.Comparator;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.conversion.RegulatingControlMappingForTransformers;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class CgmesRatioTapChangerBuilder extends AbstractCgmesTapChangerBuilder {

    CgmesRatioTapChangerBuilder(PropertyBag ratioTapChanger, Context context) {
        super(ratioTapChanger, context);
    }

    @Override
    protected void addRegulationData() {
        String regulatingControlId = RegulatingControlMappingForTransformers.getRegulatingControlId(p);
        tapChanger.setId(p.getId(CgmesNames.RATIO_TAP_CHANGER))
                .setRegulating(context.regulatingControlMapping().forTransformers().getRegulating(regulatingControlId))
                .setRegulatingControlId(regulatingControlId)
                .setTculControlMode(p.get(CgmesNames.TCUL_CONTROL_MODE))
                .setTapChangerControlEnabled(p.asBoolean(CgmesNames.TAP_CHANGER_CONTROL_ENABLED, false));
    }

    @Override
    protected void addSteps() {
        String tableId = p.getId(CgmesNames.RATIO_TAP_CHANGER_TABLE);
        if (tableId != null) {
            PropertyBags table = context.ratioTapChangerTable(tableId);
            if (table == null) {
                addStepsFromLowHighIncrement();
                return;
            }
            if (isTableValid(tableId, table)) {
                addStepsFromTable(table, tableId);
            } else {
                addStepsFromLowHighIncrement();
            }
        } else {
            addStepsFromLowHighIncrement();
        }
    }

    private void addStepsFromTable(PropertyBags table, String tableId) {
        Comparator<PropertyBag> byStep = Comparator
                .comparingInt((PropertyBag p) -> p.asInt(CgmesNames.STEP));
        table.sort(byStep);
        for (PropertyBag point : table) {
            int step = point.asInt(CgmesNames.STEP);
            double ratio = fixing(point, CgmesNames.RATIO, 1.0, tableId, step);
            double r = fixing(point, CgmesNames.R, 0, tableId, step);
            double x = fixing(point, CgmesNames.X, 0, tableId, step);
            double g = fixing(point, CgmesNames.G, 0, tableId, step);
            double b = fixing(point, CgmesNames.B, 0, tableId, step);
            tapChanger.beginStep()
                    .setRatio(ratio)
                    .setR(r)
                    .setX(x)
                    .setG1(g)
                    .setB1(b)
                    .endStep();
        }
    }

    private void addStepsFromLowHighIncrement() {
        double stepVoltageIncrement = p.asDouble(CgmesNames.STEP_VOLTAGE_INCREMENT);
        int neutralStep = p.asInt(CgmesNames.NEUTRAL_STEP);
        for (int step = lowStep; step <= highStep; step++) {
            double ratio = 1.0 + (step - neutralStep) * (stepVoltageIncrement / 100.0);
            tapChanger.beginStep()
                    .setRatio(ratio)
                    .endStep();
        }
    }
}
