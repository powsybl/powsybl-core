/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.loadflow.validation.io;

import java.io.IOException;
import java.io.Writer;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;

import com.powsybl.iidm.network.ThreeSides;
import com.powsybl.iidm.network.util.BranchData;
import com.powsybl.loadflow.validation.data.*;
import org.apache.commons.lang3.ArrayUtils;

import com.powsybl.commons.io.table.Column;
import com.powsybl.commons.io.table.TableFormatterConfig;
import com.powsybl.commons.io.table.TableFormatterFactory;
import com.powsybl.iidm.network.util.TwtData;
import com.powsybl.loadflow.validation.ValidationType;

/**
 *
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.it>}
 */
public class ValidationFormatterCsvMultilineWriter extends AbstractValidationFormatterWriter {

    private final boolean verbose;

    public ValidationFormatterCsvMultilineWriter(String id, Class<? extends TableFormatterFactory> formatterFactoryClass,
                                            TableFormatterConfig formatterConfig, Writer writer, boolean verbose,
                                            ValidationType validationType, boolean compareResults) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(formatterFactoryClass);
        Objects.requireNonNull(writer);
        this.verbose = verbose;
        this.validationType = Objects.requireNonNull(validationType);
        this.compareResults = compareResults;
        formatter = createTableFormatter(id, formatterFactoryClass, formatterConfig, writer, validationType);
        this.invalidString = formatterConfig.getInvalidString();
    }

    public ValidationFormatterCsvMultilineWriter(String id, Class<? extends TableFormatterFactory> formatterFactoryClass,
                                            Writer writer, boolean verbose, ValidationType validationType, boolean compareResults) {
        this(id, formatterFactoryClass, TableFormatterConfig.load(), writer, verbose, validationType, compareResults);
    }

    protected Column[] getColumns() {
        Column[] columns = new Column[] {
            new Column("id"),
            new Column("characteristic"),
            new Column("value")
        };
        if (compareResults) {
            columns = ArrayUtils.add(columns, new Column("value" + POST_COMPUTATION_SUFFIX));
        }
        return columns;
    }

    @Override
    protected void writeBranch(Validated<BranchData> v, Validated<BranchData> validatedFlow, boolean found, boolean writeValues) throws IOException {
        String branchId = v.data().getId();
        write(branchId, NETWORK_P1, found, validatedFlow.data().getP1(), writeValues, v.data().getP1());
        write(branchId, EXPECTED_P1, found, validatedFlow.data().getComputedP1(), writeValues, v.data().getComputedP1());
        write(branchId, NETWORK_Q1, found, validatedFlow.data().getQ1(), writeValues, v.data().getQ1());
        write(branchId, EXPECTED_Q1, found, validatedFlow.data().getComputedQ1(), writeValues, v.data().getComputedQ1());
        write(branchId, NETWORK_P2, found, validatedFlow.data().getP2(), writeValues, v.data().getP2());
        write(branchId, EXPECTED_P2, found, validatedFlow.data().getComputedP2(), writeValues, v.data().getComputedP2());
        write(branchId, NETWORK_Q2, found, validatedFlow.data().getQ2(), writeValues, v.data().getQ2());
        write(branchId, EXPECTED_Q2, found, validatedFlow.data().getComputedQ2(), writeValues, v.data().getComputedQ2());
        if (verbose) {
            write(branchId, "r", found, validatedFlow.data().getR(), writeValues, v.data().getR());
            write(branchId, "x", found, validatedFlow.data().getX(), writeValues, v.data().getX());
            write(branchId, "g1", found, validatedFlow.data().getG1(), writeValues, v.data().getG1());
            write(branchId, "g2", found, validatedFlow.data().getG2(), writeValues, v.data().getG2());
            write(branchId, "b1", found, validatedFlow.data().getB1(), writeValues, v.data().getB1());
            write(branchId, "b2", found, validatedFlow.data().getB2(), writeValues, v.data().getB2());
            write(branchId, "rho1", found, validatedFlow.data().getRho1(), writeValues, v.data().getRho1());
            write(branchId, "rho2", found, validatedFlow.data().getRho2(), writeValues, v.data().getRho2());
            write(branchId, "alpha1", found, validatedFlow.data().getAlpha1(), writeValues, v.data().getAlpha1());
            write(branchId, "alpha2", found, validatedFlow.data().getAlpha2(), writeValues, v.data().getAlpha2());
            write(branchId, "u1", found, validatedFlow.data().getU1(), writeValues, v.data().getU1());
            write(branchId, "u2", found, validatedFlow.data().getU2(), writeValues, v.data().getU2());
            write(branchId, THETA1, found, validatedFlow.data().getTheta1(), writeValues, v.data().getTheta1());
            write(branchId, THETA2, found, validatedFlow.data().getTheta2(), writeValues, v.data().getTheta2());
            write(branchId, "z", found, validatedFlow.data().getZ(), writeValues, v.data().getZ());
            write(branchId, "y", found, validatedFlow.data().getY(), writeValues, v.data().getY());
            write(branchId, "ksi", found, validatedFlow.data().getKsi(), writeValues, v.data().getKsi());
            write(branchId, "phaseAngleClock", found, validatedFlow.data().getPhaseAngleClock(), writeValues, v.data().getPhaseAngleClock());
            write(branchId, CONNECTED + "1", found, validatedFlow.data().isConnected1(), writeValues, v.data().isConnected1());
            write(branchId, CONNECTED + "2", found, validatedFlow.data().isConnected2(), writeValues, v.data().isConnected2());
            write(branchId, MAIN_COMPONENT + "1", found, validatedFlow.data().isMainComponent1(), writeValues, v.data().isMainComponent1());
            write(branchId, MAIN_COMPONENT + "2", found, validatedFlow.data().isMainComponent2(), writeValues, v.data().isMainComponent2());
            write(branchId, VALIDATION, found, getValidated(validatedFlow.validated()), writeValues, getValidated(v.validated()));
        }
    }

    @Override
    protected void writeGenerator(Validated<GeneratorData> v, Validated<GeneratorData> validatedGenerator, boolean found, boolean writeValues) throws IOException {
        String generatorId = v.data().generatorId();
        write(generatorId, "p", found, -validatedGenerator.data().p(), writeValues, -v.data().p());
        write(generatorId, "q", found, -validatedGenerator.data().q(), writeValues, -v.data().q());
        write(generatorId, "v", found, validatedGenerator.data().v(), writeValues, v.data().v());
        write(generatorId, "targetP", found, validatedGenerator.data().targetP(), writeValues, v.data().targetP());
        write(generatorId, "targetQ", found, validatedGenerator.data().targetQ(), writeValues, v.data().targetQ());
        write(generatorId, "targetV", found, validatedGenerator.data().targetV(), writeValues, v.data().targetV());
        write(generatorId, "expectedP", found, validatedGenerator.data().expectedP(), writeValues, v.data().expectedP());
        if (verbose) {
            write(generatorId, CONNECTED, found, validatedGenerator.data().connected(), writeValues, v.data().connected());
            write(generatorId, "voltageRegulatorOn", found, validatedGenerator.data().voltageRegulatorOn(), writeValues, v.data().voltageRegulatorOn());
            write(generatorId, "minP", found, validatedGenerator.data().minP(), writeValues, v.data().minP());
            write(generatorId, "maxP", found, validatedGenerator.data().maxP(), writeValues, v.data().maxP());
            write(generatorId, "minQ", found, validatedGenerator.data().minQ(), writeValues, v.data().minQ());
            write(generatorId, "maxQ", found, validatedGenerator.data().maxQ(), writeValues, v.data().maxQ());
            write(generatorId, MAIN_COMPONENT, found, validatedGenerator.data().mainComponent(), writeValues, v.data().mainComponent());
            write(generatorId, VALIDATION, found, getValidated(validatedGenerator.validated()), writeValues, getValidated(v.validated()));
        }
    }

    @Override
    protected void writeBus(Validated<BusData> v, Validated<BusData> validatedBus, boolean found, boolean writeValues) throws IOException {
        String busId = v.data().busId();
        write(busId, "incomingP", found, validatedBus.data().incomingP(), writeValues, v.data().incomingP());
        write(busId, "incomingQ", found, validatedBus.data().incomingQ(), writeValues, v.data().incomingQ());
        write(busId, "loadP", found, validatedBus.data().loadP(), writeValues, v.data().loadP());
        write(busId, "loadQ", found, validatedBus.data().loadQ(), writeValues, v.data().loadQ());
        if (verbose) {
            write(busId, "genP", found, validatedBus.data().genP(), writeValues, v.data().genP());
            write(busId, "genQ", found, validatedBus.data().genQ(), writeValues, v.data().genQ());
            write(busId, "batP", found, validatedBus.data().batP(), writeValues, v.data().batP());
            write(busId, "batQ", found, validatedBus.data().batQ(), writeValues, v.data().batQ());
            write(busId, "shuntP", found, validatedBus.data().shuntP(), writeValues, v.data().shuntP());
            write(busId, "shuntQ", found, validatedBus.data().shuntQ(), writeValues, v.data().shuntQ());
            write(busId, "svcP", found, validatedBus.data().svcP(), writeValues, v.data().svcP());
            write(busId, "svcQ", found, validatedBus.data().svcQ(), writeValues, v.data().svcQ());
            write(busId, "vscCSP", found, validatedBus.data().vscCSP(), writeValues, v.data().vscCSP());
            write(busId, "vscCSQ", found, validatedBus.data().vscCSQ(), writeValues, v.data().vscCSQ());
            write(busId, "lineP", found, validatedBus.data().lineP(), writeValues, v.data().lineP());
            write(busId, "lineQ", found, validatedBus.data().lineQ(), writeValues, v.data().lineQ());
            write(busId, "danglingLineP", found, validatedBus.data().danglingLineP(), writeValues, v.data().danglingLineP());
            write(busId, "danglingLineQ", found, validatedBus.data().danglingLineQ(), writeValues, v.data().danglingLineQ());
            write(busId, "twtP", found, validatedBus.data().twtP(), writeValues, v.data().twtP());
            write(busId, "twtQ", found, validatedBus.data().twtQ(), writeValues, v.data().twtQ());
            write(busId, "tltP", found, validatedBus.data().tltP(), writeValues, v.data().tltP());
            write(busId, "tltQ", found, validatedBus.data().tltQ(), writeValues, v.data().tltQ());
            write(busId, MAIN_COMPONENT, found, validatedBus.data().mainComponent(), writeValues, v.data().mainComponent());
            write(busId, VALIDATION, found, getValidated(validatedBus.validated()), writeValues, getValidated(v.validated()));
        }
    }

    @Override
    protected void writeSvc(Validated<SvcData> v, Validated<SvcData> validatedSvc, boolean found, boolean writeValues) throws IOException {
        String svcId = v.data().svcId();
        write(svcId, "p", found, -validatedSvc.data().p(), writeValues, -v.data().p());
        write(svcId, "q", found, -validatedSvc.data().q(), writeValues, -v.data().q());
        write(svcId, "vControlled", found, validatedSvc.data().vControlled(), writeValues, v.data().vControlled());
        write(svcId, "vController", found, validatedSvc.data().vController(), writeValues, v.data().vController());
        write(svcId, NOMINAL_V, found, validatedSvc.data().nominalVcontroller(), writeValues, v.data().nominalVcontroller());
        write(svcId, "reactivePowerSetpoint", found, validatedSvc.data().reactivePowerSetpoint(), writeValues, v.data().reactivePowerSetpoint());
        write(svcId, "voltageSetpoint", found, validatedSvc.data().voltageSetpoint(), writeValues, v.data().voltageSetpoint());
        if (verbose) {
            write(svcId, CONNECTED, found, validatedSvc.data().connected(), writeValues, v.data().connected());
            write(svcId, "regulationMode", found, validatedSvc.data().regulationMode() != null ? validatedSvc.data().regulationMode().name() : "", writeValues, v.data().regulationMode() != null ? v.data().regulationMode().name() : "");
            write(svcId, "regulating", found, validatedSvc.data().regulating(), writeValues, v.data().regulating());
            write(svcId, "bMin", found, validatedSvc.data().bMin(), writeValues, v.data().bMin());
            write(svcId, "bMax", found, validatedSvc.data().bMax(), writeValues, v.data().bMax());
            write(svcId, MAIN_COMPONENT, found, validatedSvc.data().mainComponent(), writeValues, v.data().mainComponent());
            write(svcId, VALIDATION, found, getValidated(validatedSvc.validated()), writeValues, getValidated(v.validated()));
        }
    }

    protected void writeShunt(Validated<ShuntData> v,
                              Validated<ShuntData> validatedShunt, boolean found, boolean writeValues) throws IOException {
        String shuntId = v.data().shuntId();
        write(shuntId, "q", found, validatedShunt.data().q(), writeValues, v.data().q());
        write(shuntId, "expectedQ", found, validatedShunt.data().expectedQ(), writeValues, v.data().expectedQ());
        if (verbose) {
            write(shuntId, "p", found, validatedShunt.data().p(), writeValues, v.data().p());
            write(shuntId, "currentSectionCount", found, validatedShunt.data().currentSectionCount(), writeValues, v.data().currentSectionCount());
            write(shuntId, "maximumSectionCount", found, validatedShunt.data().maximumSectionCount(), writeValues, v.data().maximumSectionCount());
            write(shuntId, "bPerSection", found, validatedShunt.data().bPerSection(), writeValues, v.data().bPerSection());
            write(shuntId, "v", found, validatedShunt.data().v(), writeValues, v.data().v());
            write(shuntId, CONNECTED, found, validatedShunt.data().connected(), writeValues, v.data().connected());
            write(shuntId, "qMax", found, validatedShunt.data().qMax(), writeValues, v.data().qMax());
            write(shuntId, NOMINAL_V, found, validatedShunt.data().nominalV(), writeValues, v.data().nominalV());
            write(shuntId, MAIN_COMPONENT, found, validatedShunt.data().mainComponent(), writeValues, v.data().mainComponent());
            write(shuntId, VALIDATION, found, getValidated(validatedShunt.validated()), writeValues, getValidated(v.validated()));
        }
    }

    @Override
    protected void writeT2wt(Validated<TransformerData> v, Validated<TransformerData> twtData, boolean found, boolean writeValues) throws IOException {
        String twtId = v.data().twtId();
        write(twtId, "error", found, twtData.data().error(), writeValues, v.data().error());
        write(twtId, "upIncrement", found, twtData.data().upIncrement(), writeValues, v.data().upIncrement());
        write(twtId, "downIncrement", found, twtData.data().downIncrement(), writeValues, v.data().downIncrement());
        if (verbose) {
            write(twtId, "rho", found, twtData.data().rho(), writeValues, v.data().rho());
            write(twtId, "rhoPreviousStep", found, twtData.data().rhoPreviousStep(), writeValues, v.data().rhoPreviousStep());
            write(twtId, "rhoNextStep", found, twtData.data().rhoNextStep(), writeValues, v.data().rhoNextStep());
            write(twtId, "tapPosition", found, twtData.data().tapPosition(), writeValues, v.data().tapPosition());
            write(twtId, "lowTapPosition", found, twtData.data().lowTapPosition(), writeValues, v.data().lowTapPosition());
            write(twtId, "highTapPosition", found, twtData.data().highTapPosition(), writeValues, v.data().highTapPosition());
            write(twtId, "tapChangerTargetV", found, twtData.data().targetV(), writeValues, v.data().targetV());
            write(twtId, "regulatedSide", found, twtData.data().regulatedSide() != null ? twtData.data().regulatedSide().name() : invalidString, writeValues, v.data().regulatedSide() != null ? v.data().regulatedSide().name() : invalidString);
            write(twtId, "v", found, twtData.data().v(), writeValues, v.data().v());
            write(twtId, CONNECTED, found, twtData.data().connected(), writeValues, v.data().connected());
            write(twtId, MAIN_COMPONENT, found, twtData.data().mainComponent(), writeValues, v.data().mainComponent());
            write(twtId, VALIDATION, found, getValidated(twtData.validated()), writeValues, getValidated(v.validated()));
        }
    }

    private double getTwtSideValue(boolean bool, TwtData twtData, ThreeSides side, BiFunction<TwtData, ThreeSides, Double> f) {
        return bool ? f.apply(twtData, side) : Double.NaN;
    }

    private double getTwtValue(boolean bool, TwtData twtData, ToDoubleFunction<TwtData> f) {
        return bool ? f.applyAsDouble(twtData) : Double.NaN;
    }

    private int getTwtValue(boolean bool, TwtData twtData, ToIntFunction<TwtData> f) {
        return bool ? f.applyAsInt(twtData) : 0;
    }

    @Override
    protected void writeT3wt(String twtId, ValidatedTransformer3W validatedTransformer3W1, ValidatedTransformer3W validatedTransformer3W2, boolean found, boolean writeValues) throws IOException {
        TwtData twtData1 = validatedTransformer3W1.twtData();
        TwtData twtData2 = validatedTransformer3W2.twtData();
        write(twtId, NETWORK_P1, found, getTwtSideValue(found, twtData2, ThreeSides.ONE, TwtData::getP), writeValues, getTwtSideValue(writeValues, twtData1, ThreeSides.ONE, TwtData::getP));
        write(twtId, EXPECTED_P1, found, getTwtSideValue(found, twtData2, ThreeSides.ONE, TwtData::getComputedP), writeValues, getTwtSideValue(writeValues, twtData1, ThreeSides.ONE, TwtData::getComputedP));
        write(twtId, NETWORK_Q1, found, getTwtSideValue(found, twtData2, ThreeSides.ONE, TwtData::getQ), writeValues, getTwtSideValue(writeValues, twtData1, ThreeSides.ONE, TwtData::getQ));
        write(twtId, EXPECTED_Q1, found, getTwtSideValue(found, twtData2, ThreeSides.ONE, TwtData::getComputedQ), writeValues, getTwtSideValue(writeValues, twtData1, ThreeSides.ONE, TwtData::getComputedQ));
        write(twtId, NETWORK_P2, found, getTwtSideValue(found, twtData2, ThreeSides.TWO, TwtData::getP), writeValues, getTwtSideValue(writeValues, twtData1, ThreeSides.TWO, TwtData::getP));
        write(twtId, EXPECTED_P2, found, getTwtSideValue(found, twtData2, ThreeSides.TWO, TwtData::getComputedP), writeValues, getTwtSideValue(writeValues, twtData1, ThreeSides.TWO, TwtData::getComputedP));
        write(twtId, NETWORK_Q2, found, getTwtSideValue(found, twtData2, ThreeSides.TWO, TwtData::getQ), writeValues, getTwtSideValue(writeValues, twtData1, ThreeSides.TWO, TwtData::getQ));
        write(twtId, EXPECTED_Q2, found, getTwtSideValue(found, twtData2, ThreeSides.TWO, TwtData::getComputedQ), writeValues, getTwtSideValue(writeValues, twtData1, ThreeSides.TWO, TwtData::getComputedQ));
        write(twtId, NETWORK_P3, found, getTwtSideValue(found, twtData2, ThreeSides.THREE, TwtData::getP), writeValues, getTwtSideValue(writeValues, twtData1, ThreeSides.THREE, TwtData::getP));
        write(twtId, EXPECTED_P3, found, getTwtSideValue(found, twtData2, ThreeSides.THREE, TwtData::getComputedP), writeValues, getTwtSideValue(writeValues, twtData1, ThreeSides.THREE, TwtData::getComputedP));
        write(twtId, NETWORK_Q3, found, getTwtSideValue(found, twtData2, ThreeSides.THREE, TwtData::getQ), writeValues, getTwtSideValue(writeValues, twtData1, ThreeSides.THREE, TwtData::getQ));
        write(twtId, EXPECTED_Q3, found, getTwtSideValue(found, twtData2, ThreeSides.THREE, TwtData::getComputedQ), writeValues, getTwtSideValue(writeValues, twtData1, ThreeSides.THREE, TwtData::getComputedQ));
        if (verbose) {
            write(twtId, "u1", found, getTwtSideValue(found, twtData2, ThreeSides.ONE, TwtData::getU), writeValues, getTwtSideValue(writeValues, twtData1, ThreeSides.ONE, TwtData::getU));
            write(twtId, "u2", found, getTwtSideValue(found, twtData2, ThreeSides.TWO, TwtData::getU), writeValues, getTwtSideValue(writeValues, twtData1, ThreeSides.TWO, TwtData::getU));
            write(twtId, "u3", found, getTwtSideValue(found, twtData2, ThreeSides.THREE, TwtData::getU), writeValues, getTwtSideValue(writeValues, twtData1, ThreeSides.THREE, TwtData::getU));
            write(twtId, "starU", found, getTwtValue(found, twtData2, TwtData::getStarU), writeValues, getTwtValue(writeValues, twtData1, TwtData::getStarU));
            write(twtId, THETA1, found, getTwtSideValue(found, twtData2, ThreeSides.ONE, TwtData::getTheta), writeValues, getTwtSideValue(writeValues, twtData1, ThreeSides.ONE, TwtData::getTheta));
            write(twtId, THETA2, found, getTwtSideValue(found, twtData2, ThreeSides.TWO, TwtData::getTheta), writeValues, getTwtSideValue(writeValues, twtData1, ThreeSides.TWO, TwtData::getTheta));
            write(twtId, THETA3, found, getTwtSideValue(found, twtData2, ThreeSides.THREE, TwtData::getTheta), writeValues, getTwtSideValue(writeValues, twtData1, ThreeSides.THREE, TwtData::getTheta));
            write(twtId, "starTheta", found, getTwtValue(found, twtData2, TwtData::getStarTheta), writeValues, getTwtValue(writeValues, twtData1, TwtData::getStarTheta));
            write(twtId, "g11", found, getTwtSideValue(found, twtData2, ThreeSides.ONE, TwtData::getG1), writeValues, getTwtSideValue(writeValues, twtData1, ThreeSides.ONE, TwtData::getG1));
            write(twtId, "b11", found, getTwtSideValue(found, twtData2, ThreeSides.ONE, TwtData::getB1), writeValues, getTwtSideValue(writeValues, twtData1, ThreeSides.ONE, TwtData::getB1));
            write(twtId, "g12", found, getTwtSideValue(found, twtData2, ThreeSides.ONE, TwtData::getG2), writeValues, getTwtSideValue(writeValues, twtData1, ThreeSides.ONE, TwtData::getG2));
            write(twtId, "b12", found, getTwtSideValue(found, twtData2, ThreeSides.ONE, TwtData::getB2), writeValues, getTwtSideValue(writeValues, twtData1, ThreeSides.ONE, TwtData::getB2));
            write(twtId, "g21", found, getTwtSideValue(found, twtData2, ThreeSides.TWO, TwtData::getG1), writeValues, getTwtSideValue(writeValues, twtData1, ThreeSides.TWO, TwtData::getG1));
            write(twtId, "b21", found, getTwtSideValue(found, twtData2, ThreeSides.TWO, TwtData::getB1), writeValues, getTwtSideValue(writeValues, twtData1, ThreeSides.TWO, TwtData::getB1));
            write(twtId, "g22", found, getTwtSideValue(found, twtData2, ThreeSides.TWO, TwtData::getG2), writeValues, getTwtSideValue(writeValues, twtData1, ThreeSides.TWO, TwtData::getG2));
            write(twtId, "b22", found, getTwtSideValue(found, twtData2, ThreeSides.TWO, TwtData::getB2), writeValues, getTwtSideValue(writeValues, twtData1, ThreeSides.TWO, TwtData::getB2));
            write(twtId, "g31", found, getTwtSideValue(found, twtData2, ThreeSides.THREE, TwtData::getG1), writeValues, getTwtSideValue(writeValues, twtData1, ThreeSides.THREE, TwtData::getG1));
            write(twtId, "b31", found, getTwtSideValue(found, twtData2, ThreeSides.THREE, TwtData::getB1), writeValues, getTwtSideValue(writeValues, twtData1, ThreeSides.THREE, TwtData::getB1));
            write(twtId, "g32", found, getTwtSideValue(found, twtData2, ThreeSides.THREE, TwtData::getG2), writeValues, getTwtSideValue(writeValues, twtData1, ThreeSides.THREE, TwtData::getG2));
            write(twtId, "b32", found, getTwtSideValue(found, twtData2, ThreeSides.THREE, TwtData::getB2), writeValues, getTwtSideValue(writeValues, twtData1, ThreeSides.THREE, TwtData::getB2));
            write(twtId, "r1", found, getTwtSideValue(found, twtData2, ThreeSides.ONE, TwtData::getR), writeValues, getTwtSideValue(writeValues, twtData1, ThreeSides.ONE, TwtData::getR));
            write(twtId, "r2", found, getTwtSideValue(found, twtData2, ThreeSides.TWO, TwtData::getR), writeValues, getTwtSideValue(writeValues, twtData1, ThreeSides.TWO, TwtData::getR));
            write(twtId, "r3", found, getTwtSideValue(found, twtData2, ThreeSides.THREE, TwtData::getR), writeValues, getTwtSideValue(writeValues, twtData1, ThreeSides.THREE, TwtData::getR));
            write(twtId, "x1", found, getTwtSideValue(found, twtData2, ThreeSides.ONE, TwtData::getX), writeValues, getTwtSideValue(writeValues, twtData1, ThreeSides.ONE, TwtData::getX));
            write(twtId, "x2", found, getTwtSideValue(found, twtData2, ThreeSides.TWO, TwtData::getX), writeValues, getTwtSideValue(writeValues, twtData1, ThreeSides.TWO, TwtData::getX));
            write(twtId, "x3", found, getTwtSideValue(found, twtData2, ThreeSides.THREE, TwtData::getX), writeValues, getTwtSideValue(writeValues, twtData1, ThreeSides.THREE, TwtData::getX));
            write(twtId, "ratedU1", found, getTwtSideValue(found, twtData2, ThreeSides.ONE, TwtData::getRatedU), writeValues, getTwtSideValue(writeValues, twtData1, ThreeSides.ONE, TwtData::getRatedU));
            write(twtId, "ratedU2", found, getTwtSideValue(found, twtData2, ThreeSides.TWO, TwtData::getRatedU), writeValues, getTwtSideValue(writeValues, twtData1, ThreeSides.TWO, TwtData::getRatedU));
            write(twtId, "ratedU3", found, getTwtSideValue(found, twtData2, ThreeSides.THREE, TwtData::getRatedU), writeValues, getTwtSideValue(writeValues, twtData1, ThreeSides.THREE, TwtData::getRatedU));
            write(twtId, "phaseAngleClock2", found, getTwtValue(found, twtData2, TwtData::getPhaseAngleClock2), writeValues, getTwtValue(writeValues, twtData1, TwtData::getPhaseAngleClock2));
            write(twtId, "phaseAngleClock3", found, getTwtValue(found, twtData2, TwtData::getPhaseAngleClock3), writeValues, getTwtValue(writeValues, twtData1, TwtData::getPhaseAngleClock3));
            write(twtId, "ratedU0", found, getTwtValue(found, twtData2, TwtData::getRatedU0), writeValues, getTwtValue(writeValues, twtData1, TwtData::getRatedU0));
            write(twtId, CONNECTED + "1", found, found && twtData2.isConnected(ThreeSides.ONE), writeValues, writeValues && twtData1.isConnected(ThreeSides.ONE));
            write(twtId, CONNECTED + "2", found, found && twtData2.isConnected(ThreeSides.TWO), writeValues, writeValues && twtData1.isConnected(ThreeSides.TWO));
            write(twtId, CONNECTED + "3", found, found && twtData2.isConnected(ThreeSides.THREE), writeValues, writeValues && twtData1.isConnected(ThreeSides.THREE));
            write(twtId, MAIN_COMPONENT + "1", found, found && twtData2.isMainComponent(ThreeSides.ONE), writeValues, writeValues && twtData1.isMainComponent(ThreeSides.ONE));
            write(twtId, MAIN_COMPONENT + "2", found, found && twtData2.isMainComponent(ThreeSides.TWO), writeValues, writeValues && twtData1.isMainComponent(ThreeSides.TWO));
            write(twtId, MAIN_COMPONENT + "3", found, found && twtData2.isMainComponent(ThreeSides.THREE), writeValues, writeValues && twtData1.isMainComponent(ThreeSides.THREE));
            write(twtId, VALIDATION, found, getValidated(validatedTransformer3W2.validated()), writeValues, getValidated(validatedTransformer3W1.validated()));
        }
    }

    private void write(String id, String label, boolean writeFirst, double first, boolean writeSecond, double second) throws IOException {
        formatter.writeCell(id).writeCell(label);
        if (compareResults) {
            formatter = writeFirst ? formatter.writeCell(first) : formatter.writeEmptyCell();
        }
        formatter = writeSecond ? formatter.writeCell(second) : formatter.writeEmptyCell();
    }

    private void write(String id, String label, boolean writeFirst, int first, boolean writeSecond, int second) throws IOException {
        formatter.writeCell(id).writeCell(label);
        if (compareResults) {
            formatter = writeFirst ? formatter.writeCell(first) : formatter.writeEmptyCell();
        }
        formatter = writeSecond ? formatter.writeCell(second) : formatter.writeEmptyCell();
    }

    private void write(String id, String label, boolean writeFirst, boolean first, boolean writeSecond, boolean second) throws IOException {
        formatter.writeCell(id).writeCell(label);
        if (compareResults) {
            formatter = writeFirst ? formatter.writeCell(first) : formatter.writeEmptyCell();
        }
        formatter = writeSecond ? formatter.writeCell(second) : formatter.writeEmptyCell();
    }

    private void write(String id, String label, boolean writeFirst, String first, boolean writeSecond, String second) throws IOException {
        formatter.writeCell(id).writeCell(label);
        if (compareResults) {
            formatter = writeFirst ? formatter.writeCell(first) : formatter.writeEmptyCell();
        }
        formatter = writeSecond ? formatter.writeCell(second) : formatter.writeEmptyCell();
    }

}
