/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.powerfactory.converter;

import java.util.Optional;

import org.apache.commons.math3.complex.Complex;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.powerfactory.converter.PowerFactoryImporter.ImportContext;
import com.powsybl.powerfactory.model.DataObject;
import com.powsybl.powerfactory.model.PowerFactoryException;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */

class ShuntConverter extends AbstractConverter {

    ShuntConverter(ImportContext importContext, Network network) {
        super(importContext, network);
    }

    void create(DataObject elmShnt) {
        NodeRef nodeRef = checkNodes(elmShnt, 1).get(0);
        LinearShuntModel linearShuntModel = LinearShuntModel.create(elmShnt);

        VoltageLevel vl = getNetwork().getVoltageLevel(nodeRef.voltageLevelId);
        vl.newShuntCompensator()
            .setId(elmShnt.getLocName())
            .setEnsureIdUnicity(true)
            .setNode(nodeRef.node)
            .setSectionCount(linearShuntModel.sectionCount)
            .newLinearModel()
            .setGPerSection(linearShuntModel.gPerSection)
            .setBPerSection(linearShuntModel.bPerSection)
            .setMaximumSectionCount(linearShuntModel.maximumSectionCount)
            .add()
            .add();
    }

    private static final class LinearShuntModel {
        private final int sectionCount;
        private final int maximumSectionCount;
        private final double gPerSection;
        private final double bPerSection;

        private LinearShuntModel(Section section, double gPerSection, double bPerSection) {
            this.sectionCount = section.sectionCount;
            this.maximumSectionCount = section.maximumSectionCount;
            this.gPerSection = gPerSection;
            this.bPerSection = bPerSection;
        }

        private static LinearShuntModel create(DataObject elmShnt) {
            int shtype = elmShnt.getIntAttributeValue("shtype");
            switch (shtype) {
                case 1:
                    return rlShunt(elmShnt);
                case 2:
                    return cShunt(elmShnt);
                default:
                    throw new PowerFactoryException("Shunt type not supported: " + shtype);
            }
        }

        private static LinearShuntModel rlShunt(DataObject elmShnt) {
            Section section = Section.create(elmShnt);

            double rrea = elmShnt.findFloatAttributeValue("rrea").orElse(0f);
            double xrea = elmShnt.findFloatAttributeValue("xrea").orElse(0f);
            if (rrea != 0.0 || xrea != 0.0) {
                Complex yPerSection = new Complex(rrea, xrea).reciprocal();
                return new LinearShuntModel(section, yPerSection.getReal(), yPerSection.getImaginary());
            }

            Optional<Float> ushnm = elmShnt.findFloatAttributeValue("ushnm");
            Optional<Float> qcapn = elmShnt.findFloatAttributeValue("qcapn");
            if (ushnm.isPresent() && qcapn.isPresent() && ushnm.get() > 0.0) {

                double bPerSection = -qcapn.get() / (ushnm.get() * ushnm.get());
                return new LinearShuntModel(section, 0.0, bPerSection);
            }

            throw new PowerFactoryException("Cannot convert RL shunt");
        }

        private static LinearShuntModel cShunt(DataObject elmShnt) {
            Section section = Section.create(elmShnt);
            float gparac = elmShnt.getFloatAttributeValue("gparac");
            float bcap = elmShnt.getFloatAttributeValue("bcap");

            double gPerSection = microSiemensToSiemens(gparac);
            double bPerSection = microSiemensToSiemens(bcap);

            return new LinearShuntModel(section, gPerSection, bPerSection);
        }

        private static final class Section {
            private final int sectionCount;
            private final int maximumSectionCount;

            private Section(int sectionCount, int maximumSectionCount) {
                this.sectionCount = sectionCount;
                this.maximumSectionCount = maximumSectionCount;
            }

            private static Section create(DataObject elmShnt) {
                int ncapa = elmShnt.getIntAttributeValue("ncapa");
                int ncapx = elmShnt.getIntAttributeValue("ncapx");

                return new Section(ncapa, ncapx);
            }
        }
    }
}
