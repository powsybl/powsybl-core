/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.converter;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.util.DanglingLineData;
import com.powsybl.psse.model.pf.*;
import org.apache.commons.math3.complex.Complex;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class DanglingLineConverter extends AbstractConverter {

    DanglingLineConverter(Network network) {
        super(network);
    }

    static void create(Network network, PssePowerFlowModel psseModel, ContextExport contextExport, PsseExporter.PerUnitContext perUnitContext) {
        List<DanglingLine> unPairedDanglingLines = network.getDanglingLineStream().filter(danglingLine -> !danglingLine.isPaired()).toList();
        if (!unPairedDanglingLines.isEmpty()) {
            createUnpairedDanglingLines(unPairedDanglingLines, psseModel, contextExport, perUnitContext);
        }
    }

    private static void createUnpairedDanglingLines(List<DanglingLine> unPairedDanglingLines, PssePowerFlowModel psseModel, ContextExport contextExport, PsseExporter.PerUnitContext perUnitContext) {
        unPairedDanglingLines.forEach(danglingLine -> {
            PsseBus psseBus = createDanglingLineBus(danglingLine, contextExport);
            psseModel.addBuses(Collections.singletonList(psseBus));
            psseModel.addNonTransformerBranches(Collections.singletonList(createDanglingLineBranch(danglingLine, psseBus.getI(), contextExport, perUnitContext)));
            createDanglingLineLoad(danglingLine, psseBus.getI(), contextExport).ifPresent(psseLoad -> psseModel.addLoads(Collections.singletonList(psseLoad)));
            createDanglingLineGenerator(danglingLine, psseBus.getI(), contextExport, perUnitContext).ifPresent(psseGenerator -> psseModel.addGenerators(Collections.singletonList(psseGenerator)));
        });
        psseModel.replaceAllBuses(psseModel.getBuses().stream().sorted(Comparator.comparingInt(PsseBus::getI)).toList());
        psseModel.replaceAllNonTransformerBranches(psseModel.getNonTransformerBranches().stream().sorted(Comparator.comparingInt(PsseNonTransformerBranch::getI).thenComparingInt(PsseNonTransformerBranch::getJ).thenComparing(PsseNonTransformerBranch::getCkt)).toList());
        psseModel.replaceAllLoads(psseModel.getLoads().stream().sorted(Comparator.comparingInt(PsseLoad::getI).thenComparing(PsseLoad::getId)).toList());
        psseModel.replaceAllGenerators(psseModel.getGenerators().stream().sorted(Comparator.comparingInt(PsseGenerator::getI).thenComparing(PsseGenerator::getId)).toList());
    }

    // Boundary bus will always be exported as busBranch
    private static PsseBus createDanglingLineBus(DanglingLine danglingLine, ContextExport contextExport) {
        Bus networkBusView = getTerminalBusView(danglingLine.getTerminal());
        PsseBus psseBus = createDefaultBus();
        psseBus.setI(contextExport.getLinkExport().getBusI(danglingLine).orElseThrow());
        psseBus.setName(fixBusName(danglingLine.getNameOrId()));
        psseBus.setBaskv(danglingLine.getTerminal().getVoltageLevel().getNominalV());
        psseBus.setIde(findType(danglingLine));
        psseBus.setVm(getVm(new DanglingLineData(danglingLine).getBoundaryBusU()));
        psseBus.setVa(getVa(Math.toDegrees(new DanglingLineData(danglingLine).getBoundaryBusTheta())));
        psseBus.setNvhi(getHighVm(networkBusView));
        psseBus.setNvlo(getLowVm(networkBusView));
        psseBus.setEvhi(getHighVm(networkBusView));
        psseBus.setEvlo(getLowVm(networkBusView));
        return psseBus;
    }

    private static int findType(DanglingLine danglingLine) {
        if (getStatus(danglingLine.getTerminal()) == 1) {
            return isVoltageRegulationOn(danglingLine) ? 2 : 1;
        } else {
            return 4;
        }
    }

    private static PsseNonTransformerBranch createDanglingLineBranch(DanglingLine danglingLine, int busJ, ContextExport contextExport, PsseExporter.PerUnitContext perUnitContext) {
        PsseNonTransformerBranch psseLine = createDefaultNonTransformerBranch();

        int busI = getTerminalBusI(danglingLine.getTerminal(), contextExport);
        double vNom1 = danglingLine.getTerminal().getVoltageLevel().getNominalV();
        Complex transmissionAdmittance = new Complex(danglingLine.getR(), danglingLine.getX()).reciprocal();
        psseLine.setI(busI);
        psseLine.setJ(busJ);
        psseLine.setCkt(contextExport.getFullExport().getEquipmentCkt(danglingLine.getId(), IdentifiableType.LINE, busI, busJ));
        psseLine.setR(impedanceToPerUnitForLinesWithDifferentNominalVoltageAtEnds(danglingLine.getR(), vNom1, vNom1, perUnitContext.sBase()));
        psseLine.setX(impedanceToPerUnitForLinesWithDifferentNominalVoltageAtEnds(danglingLine.getX(), vNom1, vNom1, perUnitContext.sBase()));
        psseLine.setName(fixNonTransformerBranchName(danglingLine.getNameOrId()));
        psseLine.setRates(createRates(danglingLine, vNom1));
        psseLine.setGi(admittanceEnd1ToPerUnitForLinesWithDifferentNominalVoltageAtEnds(transmissionAdmittance.getReal(), danglingLine.getG(), vNom1, vNom1, perUnitContext.sBase()));
        psseLine.setBi(admittanceEnd1ToPerUnitForLinesWithDifferentNominalVoltageAtEnds(transmissionAdmittance.getImaginary(), danglingLine.getB(), vNom1, vNom1, perUnitContext.sBase()));
        psseLine.setSt(getStatus(danglingLine.getTerminal()));
        return psseLine;
    }

    private static PsseRates createRates(DanglingLine danglingLine, double vNominal1) {
        PsseRates windingRates = createDefaultRates();
        danglingLine.getApparentPowerLimits().ifPresent(apparentPowerLimits -> setSortedRatesToPsseRates(getSortedRates(apparentPowerLimits), windingRates));
        if (danglingLine.getApparentPowerLimits().isEmpty()) {
            danglingLine.getCurrentLimits().ifPresent(currentLimits -> setSortedRatesToPsseRates(getSortedRates(currentLimits, vNominal1), windingRates));
        }
        if (danglingLine.getApparentPowerLimits().isEmpty() && danglingLine.getCurrentLimits().isEmpty()) {
            danglingLine.getActivePowerLimits().ifPresent(activePowerLimits -> setSortedRatesToPsseRates(getSortedRates(activePowerLimits), windingRates));
        }
        return windingRates;
    }

    private static Optional<PsseLoad> createDanglingLineLoad(DanglingLine danglingLine, int busJ, ContextExport contextExport) {
        if (!isLoadDefined(danglingLine)) {
            return Optional.empty();
        } else {
            PsseLoad psseLoad = createDefaultLoad();
            psseLoad.setI(busJ);
            psseLoad.setId(contextExport.getFullExport().getEquipmentCkt(danglingLine.getId(), IdentifiableType.LOAD, busJ));
            psseLoad.setStatus(1); // always connected
            psseLoad.setPl(danglingLine.getP0());
            psseLoad.setQl(danglingLine.getQ0());
            return Optional.of(psseLoad);
        }
    }

    private static boolean isLoadDefined(DanglingLine danglingLine) {
        return Double.isFinite(danglingLine.getP0()) && Double.isFinite(danglingLine.getQ0()) &&
                (danglingLine.getP0() != 0.0 || danglingLine.getQ0() != 0.0);
    }

    private static Optional<PsseGenerator> createDanglingLineGenerator(DanglingLine danglingLine, int busJ, ContextExport contextExport, PsseExporter.PerUnitContext perUnitContext) {
        if (isGeneratorDefined(danglingLine)) {
            return Optional.of(createGenerator(danglingLine, danglingLine.getGeneration(), busJ, contextExport, perUnitContext));
        } else {
            return Optional.empty();
        }
    }

    private static PsseGenerator createGenerator(DanglingLine danglingLine, DanglingLine.Generation generation, int busJ, ContextExport contextExport, PsseExporter.PerUnitContext perUnitContext) {
        PsseGenerator psseGenerator = createDefaultGenerator();
        psseGenerator.setI(busJ);
        psseGenerator.setId(contextExport.getFullExport().getEquipmentCkt(danglingLine.getId(), IdentifiableType.GENERATOR, busJ));
        psseGenerator.setPg(generation.getTargetP());
        psseGenerator.setQg(generation.getTargetQ());
        psseGenerator.setQt(getMaxQ(generation));
        psseGenerator.setQb(getMinQ(generation));
        psseGenerator.setVs(getTargetV(danglingLine, generation));
        psseGenerator.setIreg(busJ);
        psseGenerator.setNreg(0);
        psseGenerator.setMbase(perUnitContext.sBase());
        psseGenerator.setStat(1);
        psseGenerator.setPt(getMaxP(generation));
        psseGenerator.setPb(getMinP(generation));
        return psseGenerator;
    }

    private static boolean isGeneratorDefined(DanglingLine danglingLine) {
        return danglingLine.getGeneration() != null;
    }

    private static boolean isVoltageRegulationOn(DanglingLine danglingLine) {
        return danglingLine.getGeneration() != null && danglingLine.getGeneration().isVoltageRegulationOn();
    }

    private static double getMaxP(DanglingLine.Generation generation) {
        return Double.isNaN(generation.getMaxP()) ? 9999.0 : generation.getMaxP();
    }

    private static double getMinP(DanglingLine.Generation generation) {
        return Double.isNaN(generation.getMinP()) ? -9999.0 : generation.getMinP();
    }

    private static double getMaxQ(DanglingLine.Generation generation) {
        return generation.getReactiveLimits() != null ? generation.getReactiveLimits().getMaxQ(generation.getTargetP()) : 9999.0;
    }

    private static double getMinQ(DanglingLine.Generation generation) {
        return generation.getReactiveLimits() != null ? generation.getReactiveLimits().getMinQ(generation.getTargetP()) : -9999.0;
    }

    private static double getTargetV(DanglingLine danglingLine, DanglingLine.Generation generation) {
        if (Double.isNaN(generation.getTargetV())) {
            return 1.0;
        } else {
            return generation.getTargetV() / danglingLine.getTerminal().getVoltageLevel().getNominalV();
        }
    }
}
