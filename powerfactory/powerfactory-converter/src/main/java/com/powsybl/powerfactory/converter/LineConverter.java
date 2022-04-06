/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.powerfactory.converter;

import com.powsybl.iidm.network.Network;
import com.powsybl.powerfactory.converter.PowerFactoryImporter.ImportContext;
import com.powsybl.powerfactory.converter.PowerFactoryImporter.NodeRef;
import com.powsybl.powerfactory.model.DataObject;
import com.powsybl.powerfactory.model.DataObjectRef;

import java.util.List;
import java.util.Optional;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */

class LineConverter extends AbstractConverter {

    LineConverter(ImportContext importContext, Network network) {
        super(importContext, network);
    }

    void create(DataObject elmLne) {
        List<NodeRef> nodeRefs = checkNodes(elmLne, 2);
        Optional<LineModel> lineModel = LineModel.createFromTypLne(elmLne);
        if (lineModel.isEmpty()) {
            return;
        }
        NodeRef end1 = nodeRefs.get(0);
        NodeRef end2 = nodeRefs.get(1);

        getNetwork().newLine()
            .setId(elmLne.getLocName())
            .setEnsureIdUnicity(true)
            .setVoltageLevel1(end1.voltageLevelId)
            .setVoltageLevel2(end2.voltageLevelId)
            .setNode1(end1.node)
            .setNode2(end2.node)
            .setR(lineModel.get().r)
            .setX(lineModel.get().x)
            .setG1(lineModel.get().g1)
            .setB1(lineModel.get().b1)
            .setG2(lineModel.get().g2)
            .setB2(lineModel.get().b2)
            .add();
    }

    static class LineModel {
        private final double r;
        private final double x;
        private final double g1;
        private final double b1;
        private final double g2;
        private final double b2;

        LineModel(double r, double x, double g1, double b1, double g2, double b2) {
            this.r = r;
            this.x = x;
            this.g1 = g1;
            this.b1 = b1;
            this.g2 = g2;
            this.b2 = b2;
        }

        static Optional<LineModel> createFromTypLne(DataObject elmLne) {
            return elmLne.findObjectAttributeValue(TYP_ID).flatMap(DataObjectRef::resolve).map(typLne -> typeLneModel(elmLne, typLne));
        }

        private static LineModel typeLneModel(DataObject elmLne, DataObject typLne) {
            float dline = elmLne.getFloatAttributeValue("dline");
            float rline = typLne.getFloatAttributeValue("rline");
            float xline = typLne.getFloatAttributeValue("xline");
            double r = rline * dline;
            double x = xline * dline;

            double g = typeLneModelShuntConductance(typLne) * dline;
            double b = typeLneModelShuntSusceptance(typLne) * dline;

            return new LineModel(r, x, g * 0.5, b * 0.5, g * 0.5, b * 0.5);
        }

        private static double typeLneModelShuntConductance(DataObject typLne) {
            Optional<Float> gline = typLne.findFloatAttributeValue("gline");
            if (gline.isPresent()) {
                return microSiemensToSiemens(gline.get());
            }
            Optional<Float> tline = typLne.findFloatAttributeValue("tline");
            Optional<Float> bline = typLne.findFloatAttributeValue("bline");
            if (tline.isPresent() && bline.isPresent()) {
                return microSiemensToSiemens(bline.get() * tline.get());
            }
            return 0.0;
        }

        private static double typeLneModelShuntSusceptance(DataObject typLne) {
            Optional<Float> bline = typLne.findFloatAttributeValue("bline");
            if (bline.isPresent()) {
                return microSiemensToSiemens(bline.get());
            }
            Optional<Float> frnom = typLne.findFloatAttributeValue("frnom");
            Optional<Float> cline = typLne.findFloatAttributeValue("cline");
            if (frnom.isPresent() && cline.isPresent()) {
                return microFaradToSiemens(frnom.get(), cline.get());
            }
            return 0.0;
        }
    }
}
