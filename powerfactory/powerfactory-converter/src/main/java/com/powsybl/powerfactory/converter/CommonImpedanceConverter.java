/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.powerfactory.converter;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.powerfactory.converter.PowerFactoryImporter.ImportContext;
import com.powsybl.powerfactory.model.DataObject;

import java.util.List;

import org.apache.commons.math3.complex.Complex;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */

class CommonImpedanceConverter extends AbstractConverter {

    CommonImpedanceConverter(ImportContext importContext, Network network) {
        super(importContext, network);
    }

    void create(DataObject elmZpu) {
        List<NodeRef> nodeRefs = checkNodes(elmZpu, 2);

        NodeRef end1 = nodeRefs.get(0);
        NodeRef end2 = nodeRefs.get(1);
        VoltageLevel vl1 = getNetwork().getVoltageLevel(end1.voltageLevelId);
        VoltageLevel vl2 = getNetwork().getVoltageLevel(end2.voltageLevelId);

        CommonImpedanceModel commonImpedanceModel = CommonImpedanceModel.create(elmZpu, vl1.getNominalV(), vl2.getNominalV());

        getNetwork().newLine()
            .setId(elmZpu.getLocName())
            .setEnsureIdUnicity(true)
            .setVoltageLevel1(end1.voltageLevelId)
            .setVoltageLevel2(end2.voltageLevelId)
            .setNode1(end1.node)
            .setNode2(end2.node)
            .setR((commonImpedanceModel.r12 + commonImpedanceModel.r21) * 0.5)
            .setX((commonImpedanceModel.x12 + commonImpedanceModel.x21) * 0.5)
            .setG1(commonImpedanceModel.g1)
            .setB1(commonImpedanceModel.b1)
            .setG2(commonImpedanceModel.g2)
            .setB2(commonImpedanceModel.b2)
            .add();
    }

    private static final class CommonImpedanceModel {
        private double r12;
        private double x12;
        private double r21;
        private double x21;
        private double g1;
        private double b1;
        private double g2;
        private double b2;

        private CommonImpedanceModel(double r12, double x12, double r21, double x21, double g1, double b1, double g2, double b2) {
            this.r12 = r12;
            this.x12 = x12;
            this.r21 = r21;
            this.x21 = x21;
            this.g1 = g1;
            this.b1 = b1;
            this.g2 = g2;
            this.b2 = b2;
        }

        private static CommonImpedanceModel create(DataObject elmZpu, double vn1, double vn2) {
            CommonImpedanceModel puModel = commonImpedancePerUnitModel(elmZpu);

            // Support common impedance lines with different nominal voltage at ends

            float sn = elmZpu.getFloatAttributeValue("Sn");

            puModel.r12 = impedanceToEngineeringUnitsForLinesWithDifferentNominalVoltageAtEnds(puModel.r12, vn1, vn2, sn);
            puModel.x12 = impedanceToEngineeringUnitsForLinesWithDifferentNominalVoltageAtEnds(puModel.x12, vn1, vn2, sn);
            puModel.r21 = impedanceToEngineeringUnitsForLinesWithDifferentNominalVoltageAtEnds(puModel.r21, vn1, vn2, sn);
            puModel.x21 = impedanceToEngineeringUnitsForLinesWithDifferentNominalVoltageAtEnds(puModel.x21, vn1, vn2, sn);

            Complex y12Eu = new Complex(puModel.r12, puModel.x12).reciprocal();
            puModel.g1 = admittanceEnd1ToEngineeringUnitsForLinesWithDifferentNominalVoltageAtEnds(y12Eu.getReal(), puModel.g1, vn1, vn2, sn);
            puModel.b1 = admittanceEnd1ToEngineeringUnitsForLinesWithDifferentNominalVoltageAtEnds(y12Eu.getImaginary(), puModel.b1, vn1, vn2, sn);

            Complex y21Eu = new Complex(puModel.r21, puModel.x21).reciprocal();
            puModel.g2 = admittanceEnd2ToEngineeringUnitsForLinesWithDifferentNominalVoltageAtEnds(y21Eu.getReal(), puModel.g2, vn1, vn2, sn);
            puModel.b2 = admittanceEnd2ToEngineeringUnitsForLinesWithDifferentNominalVoltageAtEnds(y21Eu.getImaginary(), puModel.b2, vn1, vn2, sn);

            return puModel;
        }

        private static CommonImpedanceModel commonImpedancePerUnitModel(DataObject elmZpu) {
            float r12 = elmZpu.getFloatAttributeValue("r_pu");
            float x12 = elmZpu.getFloatAttributeValue("x_pu");

            float r21 = elmZpu.findFloatAttributeValue("r_pu_ji").orElse(r12);
            float x21 = elmZpu.findFloatAttributeValue("x_pu_ji").orElse(x12);

            float g1 = elmZpu.findFloatAttributeValue("gi_pu").orElse(0f);
            float b1 = elmZpu.findFloatAttributeValue("bi_pu").orElse(0f);

            float g2 = elmZpu.findFloatAttributeValue("gj_pu").orElse(0f);
            float b2 = elmZpu.findFloatAttributeValue("bj_pu").orElse(0f);

            return new CommonImpedanceModel(r12, x12, r21, x21, g1, b1, g2, b2);
        }
    }
}
