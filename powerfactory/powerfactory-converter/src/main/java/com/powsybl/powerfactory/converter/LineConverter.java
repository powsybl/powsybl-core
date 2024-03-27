/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.powerfactory.converter;

import com.powsybl.iidm.network.Network;
import com.powsybl.powerfactory.converter.PowerFactoryImporter.ImportContext;
import com.powsybl.powerfactory.model.DataObject;
import com.powsybl.powerfactory.model.DataObjectRef;
import com.powsybl.powerfactory.model.PowerFactoryException;

import java.util.List;
import java.util.Optional;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */

class LineConverter extends AbstractConverter {

    LineConverter(ImportContext importContext, Network network) {
        super(importContext, network);
    }

    void create(DataObject elmLne) {
        List<NodeRef> nodeRefs = checkNodes(elmLne, 2);
        Optional<LineModel> lineModel = LineModel.createFromElmLne(elmLne);
        if (lineModel.isEmpty()) {
            return;
        }
        NodeRef end1 = nodeRefs.get(0);
        NodeRef end2 = nodeRefs.get(1);

        createLine(end1, end2, elmLne.getLocName(), lineModel.get());
    }

    void createTower(DataObject elmTow) {
        Optional<List<DataObjectRef>> plines = elmTow.findObjectVectorAttributeValue("plines");
        if (plines.isEmpty() || plines.get().isEmpty()) {
            throw new PowerFactoryException("ElmTow without plines '" + elmTow.getLocName() + "'");
        }

        plines.get().forEach(pline -> {
            DataObject elmLne = pline.resolve()
                .orElseThrow(() -> new PowerFactoryException("pline dataObject not found, ElmTow Id: " + elmTow.getId()));
            createFromElmLneFromElmTow(elmTow, elmLne);
        });
    }

    private void createLine(NodeRef end1, NodeRef end2, String id, LineModel lineModel) {
        getNetwork().newLine()
            .setId(id)
            .setEnsureIdUnicity(true)
            .setVoltageLevel1(end1.voltageLevelId)
            .setVoltageLevel2(end2.voltageLevelId)
            .setNode1(end1.node)
            .setNode2(end2.node)
            .setR(lineModel.r)
            .setX(lineModel.x)
            .setG1(lineModel.g1)
            .setB1(lineModel.b1)
            .setG2(lineModel.g2)
            .setB2(lineModel.b2)
            .add();
    }

    private void createFromElmLneFromElmTow(DataObject elmTow, DataObject elmLne) {
        List<NodeRef> nodeRefs = checkNodes(elmLne, 2);
        Optional<LineModel> lineModel = LineModel.createFromElmTow(elmTow, elmLne);

        NodeRef end1 = nodeRefs.get(0);
        NodeRef end2 = nodeRefs.get(1);

        createLine(end1, end2, elmLne.getLocName(), lineModel.get());
    }

    private static final class LineModel {
        private final double r;
        private final double x;
        private final double g1;
        private final double b1;
        private final double g2;
        private final double b2;

        private LineModel(double r, double x, double g1, double b1, double g2, double b2) {
            this.r = r;
            this.x = x;
            this.g1 = g1;
            this.b1 = b1;
            this.g2 = g2;
            this.b2 = b2;
        }

        private static Optional<LineModel> createFromElmLne(DataObject elmLne) {
            return elmLne.findObjectAttributeValue(DataAttributeNames.TYP_ID).flatMap(DataObjectRef::resolve).map(typLne -> typeLneModel(elmLne, typLne));
        }

        private static LineModel typeLneModel(DataObject elmLne, DataObject typLne) {
            float dline = elmLne.getFloatAttributeValue("dline");
            float rline = typLne.getFloatAttributeValue("rline");
            float xline = typLne.getFloatAttributeValue("xline");
            double r = rline * dline;
            double x = xline * dline;

            double g = typeLneModelShuntConductance(typLne) * dline;
            double b = typeLneModelShuntSusceptance(typLne) * dline;

            int numberOfParallelLines = elmLne.findIntAttributeValue("nlnum").orElse(1);
            if (numberOfParallelLines > 1) {
                r /= numberOfParallelLines;
                x /= numberOfParallelLines;
                g *= numberOfParallelLines;
                b *= numberOfParallelLines;
            }

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

        private static Optional<LineModel> createFromElmTow(DataObject elmTow, DataObject elmLne) {
            Float dline = elmLne.getFloatAttributeValue("dline");
            DataObject typTow = getTypeTow(elmTow);

            double r = typTow.getDoubleMatrixAttributeValue("R_c1").getEntry(0, 0) * dline;
            double x = typTow.getDoubleMatrixAttributeValue("X_c1").getEntry(0, 0) * dline;
            double g = microSiemensToSiemens(typTow.getDoubleMatrixAttributeValue("G_c1").getEntry(0, 0) * dline);
            double b = microSiemensToSiemens(typTow.getDoubleMatrixAttributeValue("B_c1").getEntry(0, 0) * dline);

            return Optional.of(new LineModel(r, x, g * 0.5, b * 0.5, g * 0.5, b * 0.5));
        }

        private static DataObject getTypeTow(DataObject elmTow) {
            return elmTow.findObjectVectorAttributeValue("pGeo")
                .flatMap(listDataObjectRef -> listDataObjectRef.stream().findFirst()).flatMap(DataObjectRef::resolve)
                .orElseThrow(() -> new PowerFactoryException("Unexpected elmTow configuration '" + elmTow.getLocName() + "'"));
        }
    }
}
