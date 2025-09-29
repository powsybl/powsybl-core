/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.converter;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.util.BoundaryLineData;
import com.powsybl.psse.model.pf.*;
import org.apache.commons.math3.complex.Complex;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static com.powsybl.psse.converter.AbstractConverter.PsseEquipmentType.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class BoundaryLineConverter extends AbstractConverter {

    BoundaryLineConverter(Network network) {
        super(network);
    }

    static void create(Network network, PssePowerFlowModel psseModel, ContextExport contextExport, PsseExporter.PerUnitContext perUnitContext) {
        List<BoundaryLine> unPairedBoundaryLines = network.getDanglingLineStream().filter(danglingLine -> !danglingLine.isPaired()).toList();
        if (!unPairedBoundaryLines.isEmpty()) {
            createUnpairedDanglingLines(unPairedBoundaryLines, psseModel, contextExport, perUnitContext);
        }
    }

    private static void createUnpairedDanglingLines(List<BoundaryLine> unPairedBoundaryLines, PssePowerFlowModel psseModel, ContextExport contextExport, PsseExporter.PerUnitContext perUnitContext) {
        unPairedBoundaryLines.forEach(danglingLine -> {
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
    private static PsseBus createDanglingLineBus(BoundaryLine boundaryLine, ContextExport contextExport) {
        Bus networkBusView = getTerminalConnectableBusView(boundaryLine.getTerminal());
        PsseBus psseBus = createDefaultBus();
        psseBus.setI(contextExport.getFullExport().getBusI(boundaryLine).orElseThrow());
        psseBus.setName(fixBusName(boundaryLine.getNameOrId()));
        psseBus.setBaskv(boundaryLine.getTerminal().getVoltageLevel().getNominalV());
        psseBus.setIde(findType(boundaryLine, contextExport));
        psseBus.setVm(getVm(new BoundaryLineData(boundaryLine).getBoundaryBusU()));
        psseBus.setVa(getVa(Math.toDegrees(new BoundaryLineData(boundaryLine).getBoundaryBusTheta())));
        psseBus.setNvhi(getHighVm(networkBusView));
        psseBus.setNvlo(getLowVm(networkBusView));
        psseBus.setEvhi(getHighVm(networkBusView));
        psseBus.setEvlo(getLowVm(networkBusView));
        return psseBus;
    }

    private static int findType(BoundaryLine boundaryLine, ContextExport contextExport) {
        if (getStatus(boundaryLine.getTerminal(), contextExport) == 1) {
            return isVoltageRegulationOn(boundaryLine) ? 2 : 1;
        } else {
            return 4;
        }
    }

    private static PsseNonTransformerBranch createDanglingLineBranch(BoundaryLine boundaryLine, int busJ, ContextExport contextExport, PsseExporter.PerUnitContext perUnitContext) {
        PsseNonTransformerBranch psseLine = createDefaultNonTransformerBranch();

        int busI = getTerminalBusI(boundaryLine.getTerminal(), contextExport);
        double vNom1 = boundaryLine.getTerminal().getVoltageLevel().getNominalV();
        Complex transmissionAdmittance = new Complex(boundaryLine.getR(), boundaryLine.getX()).reciprocal();
        psseLine.setI(busI);
        psseLine.setJ(busJ);
        psseLine.setCkt(contextExport.getFullExport().getEquipmentCkt(boundaryLine.getId(), PSSE_BRANCH.getTextCode(), busI, busJ));
        psseLine.setR(impedanceToPerUnitForLinesWithDifferentNominalVoltageAtEnds(boundaryLine.getR(), vNom1, vNom1, perUnitContext.sBase()));
        psseLine.setX(impedanceToPerUnitForLinesWithDifferentNominalVoltageAtEnds(boundaryLine.getX(), vNom1, vNom1, perUnitContext.sBase()));
        psseLine.setName(fixNonTransformerBranchName(boundaryLine.getNameOrId()));
        psseLine.setRates(createRates(boundaryLine, vNom1));
        psseLine.setGi(admittanceEnd1ToPerUnitForLinesWithDifferentNominalVoltageAtEnds(transmissionAdmittance.getReal(), boundaryLine.getG(), vNom1, vNom1, perUnitContext.sBase()));
        psseLine.setBi(admittanceEnd1ToPerUnitForLinesWithDifferentNominalVoltageAtEnds(transmissionAdmittance.getImaginary(), boundaryLine.getB(), vNom1, vNom1, perUnitContext.sBase()));
        psseLine.setSt(getStatus(boundaryLine.getTerminal(), contextExport));
        return psseLine;
    }

    private static PsseRates createRates(BoundaryLine boundaryLine, double vNominal1) {
        PsseRates windingRates = createDefaultRates();
        boundaryLine.getApparentPowerLimits().ifPresent(apparentPowerLimits -> setSortedRatesToPsseRates(getSortedRates(apparentPowerLimits), windingRates));
        if (boundaryLine.getApparentPowerLimits().isEmpty()) {
            boundaryLine.getCurrentLimits().ifPresent(currentLimits -> setSortedRatesToPsseRates(getSortedRates(currentLimits, vNominal1), windingRates));
        }
        if (boundaryLine.getApparentPowerLimits().isEmpty() && boundaryLine.getCurrentLimits().isEmpty()) {
            boundaryLine.getActivePowerLimits().ifPresent(activePowerLimits -> setSortedRatesToPsseRates(getSortedRates(activePowerLimits), windingRates));
        }
        return windingRates;
    }

    private static Optional<PsseLoad> createDanglingLineLoad(BoundaryLine boundaryLine, int busJ, ContextExport contextExport) {
        if (!isLoadDefined(boundaryLine)) {
            return Optional.empty();
        } else {
            PsseLoad psseLoad = createDefaultLoad();
            psseLoad.setI(busJ);
            psseLoad.setId(contextExport.getFullExport().getEquipmentCkt(boundaryLine.getId(), PSSE_LOAD.getTextCode(), busJ));
            psseLoad.setStatus(1); // always connected
            psseLoad.setPl(boundaryLine.getP0());
            psseLoad.setQl(boundaryLine.getQ0());
            return Optional.of(psseLoad);
        }
    }

    private static boolean isLoadDefined(BoundaryLine boundaryLine) {
        return Double.isFinite(boundaryLine.getP0()) && Double.isFinite(boundaryLine.getQ0()) &&
                (boundaryLine.getP0() != 0.0 || boundaryLine.getQ0() != 0.0);
    }

    private static Optional<PsseGenerator> createDanglingLineGenerator(BoundaryLine boundaryLine, int busJ, ContextExport contextExport, PsseExporter.PerUnitContext perUnitContext) {
        if (isGeneratorDefined(boundaryLine)) {
            return Optional.of(createGenerator(boundaryLine, boundaryLine.getGeneration(), busJ, contextExport, perUnitContext));
        } else {
            return Optional.empty();
        }
    }

    private static PsseGenerator createGenerator(BoundaryLine boundaryLine, BoundaryLine.Generation generation, int busJ, ContextExport contextExport, PsseExporter.PerUnitContext perUnitContext) {
        PsseGenerator psseGenerator = createDefaultGenerator();
        psseGenerator.setI(busJ);
        psseGenerator.setId(contextExport.getFullExport().getEquipmentCkt(boundaryLine.getId(), PSSE_GENERATOR.getTextCode(), busJ));
        psseGenerator.setPg(generation.getTargetP());
        psseGenerator.setQg(generation.getTargetQ());
        psseGenerator.setQt(Math.max(getMaxQ(generation), getMinQ(generation)));
        psseGenerator.setQb(getMinQ(generation));
        psseGenerator.setVs(getTargetV(boundaryLine, generation));
        psseGenerator.setIreg(busJ);
        psseGenerator.setNreg(0);
        psseGenerator.setMbase(perUnitContext.sBase());
        psseGenerator.setStat(1);
        psseGenerator.setPt(getMaxP(generation));
        psseGenerator.setPb(getMinP(generation));
        return psseGenerator;
    }

    private static boolean isGeneratorDefined(BoundaryLine boundaryLine) {
        return boundaryLine.getGeneration() != null;
    }

    private static boolean isVoltageRegulationOn(BoundaryLine boundaryLine) {
        return boundaryLine.getGeneration() != null && boundaryLine.getGeneration().isVoltageRegulationOn();
    }

    private static double getMaxP(BoundaryLine.Generation generation) {
        return Double.isNaN(generation.getMaxP()) ? 9999.0 : generation.getMaxP();
    }

    private static double getMinP(BoundaryLine.Generation generation) {
        return Double.isNaN(generation.getMinP()) ? -9999.0 : generation.getMinP();
    }

    private static double getMaxQ(BoundaryLine.Generation generation) {
        return generation.getReactiveLimits() != null ? generation.getReactiveLimits().getMaxQ(generation.getTargetP()) : 9999.0;
    }

    private static double getMinQ(BoundaryLine.Generation generation) {
        return generation.getReactiveLimits() != null ? generation.getReactiveLimits().getMinQ(generation.getTargetP()) : -9999.0;
    }

    private static double getTargetV(BoundaryLine boundaryLine, BoundaryLine.Generation generation) {
        if (Double.isNaN(generation.getTargetV()) || generation.getTargetV() <= 0.0) {
            return 1.0;
        } else {
            return generation.getTargetV() / boundaryLine.getTerminal().getVoltageLevel().getNominalV();
        }
    }
}
