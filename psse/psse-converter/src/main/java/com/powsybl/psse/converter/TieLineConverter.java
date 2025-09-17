/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.converter;

import com.powsybl.iidm.network.*;
import com.powsybl.psse.model.pf.PsseNonTransformerBranch;
import com.powsybl.psse.model.pf.PssePowerFlowModel;
import com.powsybl.psse.model.pf.PsseRates;
import org.apache.commons.math3.complex.Complex;

import java.util.Collections;
import java.util.Comparator;

import static com.powsybl.psse.converter.AbstractConverter.PsseEquipmentType.PSSE_BRANCH;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class TieLineConverter extends AbstractConverter {

    TieLineConverter(Network network) {
        super(network);
    }

    static void create(Network network, PssePowerFlowModel psseModel, ContextExport contextExport, PsseExporter.PerUnitContext perUnitContext) {
        network.getTieLines().forEach(tieLine -> psseModel.addNonTransformerBranches(Collections.singletonList(createTieLine(tieLine, contextExport, perUnitContext))));
        psseModel.replaceAllNonTransformerBranches(psseModel.getNonTransformerBranches().stream().sorted(Comparator.comparingInt(PsseNonTransformerBranch::getI).thenComparingInt(PsseNonTransformerBranch::getJ).thenComparing(PsseNonTransformerBranch::getCkt)).toList());
    }

    private static PsseNonTransformerBranch createTieLine(TieLine tieLine, ContextExport contextExport, PsseExporter.PerUnitContext perUnitContext) {
        PsseNonTransformerBranch psseLine = createDefaultNonTransformerBranch();
        int busI = getTerminalBusI(tieLine.getTerminal1(), contextExport);
        int busJ = getTerminalBusI(tieLine.getTerminal2(), contextExport);
        double vNom1 = tieLine.getTerminal1().getVoltageLevel().getNominalV();
        double vNom2 = tieLine.getTerminal2().getVoltageLevel().getNominalV();
        Complex transmissionAdmittance = new Complex(tieLine.getR(), tieLine.getX()).reciprocal();

        psseLine.setI(busI);
        psseLine.setJ(busJ);
        psseLine.setCkt(contextExport.getFullExport().getEquipmentCkt(tieLine.getId(), PSSE_BRANCH.getTextCode(), busI, busJ));
        psseLine.setR(impedanceToPerUnitForLinesWithDifferentNominalVoltageAtEnds(tieLine.getR(), vNom1, vNom2, perUnitContext.sBase()));
        psseLine.setX(impedanceToPerUnitForLinesWithDifferentNominalVoltageAtEnds(tieLine.getX(), vNom1, vNom2, perUnitContext.sBase()));
        psseLine.setName(tieLine.getNameOrId().substring(0, Math.min(40, tieLine.getNameOrId().length())));
        psseLine.setRates(createRates(tieLine, vNom1, vNom2));
        psseLine.setGi(admittanceEnd1ToPerUnitForLinesWithDifferentNominalVoltageAtEnds(transmissionAdmittance.getReal(), tieLine.getG1(), vNom1, vNom2, perUnitContext.sBase()));
        psseLine.setBi(admittanceEnd1ToPerUnitForLinesWithDifferentNominalVoltageAtEnds(transmissionAdmittance.getImaginary(), tieLine.getB1(), vNom1, vNom2, perUnitContext.sBase()));
        psseLine.setGj(admittanceEnd2ToPerUnitForLinesWithDifferentNominalVoltageAtEnds(transmissionAdmittance.getReal(), tieLine.getG2(), vNom1, vNom2, perUnitContext.sBase()));
        psseLine.setBj(admittanceEnd2ToPerUnitForLinesWithDifferentNominalVoltageAtEnds(transmissionAdmittance.getImaginary(), tieLine.getB2(), vNom1, vNom2, perUnitContext.sBase()));
        psseLine.setSt(getStatus(tieLine.getTerminal1(), tieLine.getTerminal2(), contextExport));
        return psseLine;
    }

    private static PsseRates createRates(TieLine tieLine, double vNominal1, double vNominal2) {
        PsseRates windingRates = createDefaultRates();
        tieLine.getApparentPowerLimits1().ifPresent(apparentPowerLimits1 -> setSortedRatesToPsseRates(getSortedRates(apparentPowerLimits1), windingRates));
        if (tieLine.getApparentPowerLimits1().isEmpty()) {
            tieLine.getApparentPowerLimits2().ifPresent(apparentPowerLimits2 -> setSortedRatesToPsseRates(getSortedRates(apparentPowerLimits2), windingRates));
        }
        if (tieLine.getApparentPowerLimits1().isEmpty() && tieLine.getApparentPowerLimits2().isEmpty()) {
            tieLine.getCurrentLimits1().ifPresent(currentLimits1 -> setSortedRatesToPsseRates(getSortedRates(currentLimits1, vNominal1), windingRates));
        }
        if (tieLine.getApparentPowerLimits1().isEmpty() && tieLine.getApparentPowerLimits2().isEmpty() && tieLine.getCurrentLimits1().isEmpty()) {
            tieLine.getCurrentLimits2().ifPresent(currentLimits2 -> setSortedRatesToPsseRates(getSortedRates(currentLimits2, vNominal2), windingRates));
        }
        if (tieLine.getApparentPowerLimits1().isEmpty() && tieLine.getApparentPowerLimits2().isEmpty() && tieLine.getCurrentLimits1().isEmpty()
                && tieLine.getCurrentLimits2().isEmpty()) {
            tieLine.getActivePowerLimits1().ifPresent(activePowerLimits1 -> setSortedRatesToPsseRates(getSortedRates(activePowerLimits1), windingRates));
        }
        if (tieLine.getApparentPowerLimits1().isEmpty() && tieLine.getApparentPowerLimits2().isEmpty() && tieLine.getCurrentLimits1().isEmpty()
                && tieLine.getCurrentLimits2().isEmpty() && tieLine.getActivePowerLimits1().isEmpty()) {
            tieLine.getActivePowerLimits2().ifPresent(activePowerLimits2 -> setSortedRatesToPsseRates(getSortedRates(activePowerLimits2), windingRates));
        }
        return windingRates;
    }
}
