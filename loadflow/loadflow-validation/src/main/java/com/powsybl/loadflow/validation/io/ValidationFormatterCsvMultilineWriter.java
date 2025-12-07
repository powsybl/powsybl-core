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
import com.powsybl.loadflow.validation.data.*;
import org.apache.commons.lang3.ArrayUtils;

import com.powsybl.commons.io.table.Column;
import com.powsybl.commons.io.table.TableFormatterConfig;
import com.powsybl.commons.io.table.TableFormatterFactory;
import com.powsybl.iidm.network.TwoSides;
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
    protected void writeBranch(String branchId, double p1, double p1Calc, double q1, double q1Calc, double p2, double p2Calc, double q2, double q2Calc,
                               double r, double x, double g1, double g2, double b1, double b2, double rho1, double rho2, double alpha1, double alpha2,
                               double u1, double u2, double theta1, double theta2, double z, double y, double ksi, int phaseAngleClock, boolean connected1, boolean connected2,
                               boolean mainComponent1, boolean mainComponent2, boolean validated, ValidatedFlow validatedFlow, boolean found, boolean writeValues) throws IOException {
        write(branchId, NETWORK_P1, found, validatedFlow.p1(), writeValues, p1);
        write(branchId, EXPECTED_P1, found, validatedFlow.p1Calc(), writeValues, p1Calc);
        write(branchId, NETWORK_Q1, found, validatedFlow.q1(), writeValues, q1);
        write(branchId, EXPECTED_Q1, found, validatedFlow.q1Calc(), writeValues, q1Calc);
        write(branchId, NETWORK_P2, found, validatedFlow.p2(), writeValues, p2);
        write(branchId, EXPECTED_P2, found, validatedFlow.p2Calc(), writeValues, p2Calc);
        write(branchId, NETWORK_Q2, found, validatedFlow.q2(), writeValues, q2);
        write(branchId, EXPECTED_Q2, found, validatedFlow.q2Calc(), writeValues, q2Calc);
        if (verbose) {
            write(branchId, "r", found, validatedFlow.r(), writeValues, r);
            write(branchId, "x", found, validatedFlow.x(), writeValues, x);
            write(branchId, "g1", found, validatedFlow.g1(), writeValues, g1);
            write(branchId, "g2", found, validatedFlow.g2(), writeValues, g2);
            write(branchId, "b1", found, validatedFlow.b1(), writeValues, b1);
            write(branchId, "b2", found, validatedFlow.b2(), writeValues, b2);
            write(branchId, "rho1", found, validatedFlow.rho1(), writeValues, rho1);
            write(branchId, "rho2", found, validatedFlow.rho2(), writeValues, rho2);
            write(branchId, "alpha1", found, validatedFlow.alpha1(), writeValues, alpha1);
            write(branchId, "alpha2", found, validatedFlow.alpha2(), writeValues, alpha2);
            write(branchId, "u1", found, validatedFlow.u1(), writeValues, u1);
            write(branchId, "u2", found, validatedFlow.u2(), writeValues, u2);
            write(branchId, THETA1, found, validatedFlow.theta1(), writeValues, theta1);
            write(branchId, THETA2, found, validatedFlow.theta2(), writeValues, theta2);
            write(branchId, "z", found, validatedFlow.z(), writeValues, z);
            write(branchId, "y", found, validatedFlow.y(), writeValues, y);
            write(branchId, "ksi", found, validatedFlow.ksi(), writeValues, ksi);
            write(branchId, "phaseAngleClock", found, validatedFlow.phaseAngleClock(), writeValues, phaseAngleClock);
            write(branchId, CONNECTED + "1", found, validatedFlow.connected1(), writeValues, connected1);
            write(branchId, CONNECTED + "2", found, validatedFlow.connected2(), writeValues, connected2);
            write(branchId, MAIN_COMPONENT + "1", found, validatedFlow.mainComponent1(), writeValues, mainComponent1);
            write(branchId, MAIN_COMPONENT + "2", found, validatedFlow.mainComponent2(), writeValues, mainComponent2);
            write(branchId, VALIDATION, found, getValidated(validatedFlow.validated()), writeValues, getValidated(validated));
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
    protected void writeT2wt(String twtId, double error, double upIncrement, double downIncrement, double rho, double rhoPreviousStep, double rhoNextStep,
                             int tapPosition, int lowTapPosition, int highTapPosition, double targetV, TwoSides regulatedSide, double v, boolean connected,
                             boolean mainComponent, boolean validated, ValidatedTransformer twtData, boolean found, boolean writeValues) throws IOException {
        write(twtId, "error", found, twtData.error(), writeValues, error);
        write(twtId, "upIncrement", found, twtData.upIncrement(), writeValues, upIncrement);
        write(twtId, "downIncrement", found, twtData.downIncrement(), writeValues, downIncrement);
        if (verbose) {
            write(twtId, "rho", found, twtData.rho(), writeValues, rho);
            write(twtId, "rhoPreviousStep", found, twtData.rhoPreviousStep(), writeValues, rhoPreviousStep);
            write(twtId, "rhoNextStep", found, twtData.rhoNextStep(), writeValues, rhoNextStep);
            write(twtId, "tapPosition", found, twtData.tapPosition(), writeValues, tapPosition);
            write(twtId, "lowTapPosition", found, twtData.lowTapPosition(), writeValues, lowTapPosition);
            write(twtId, "highTapPosition", found, twtData.highTapPosition(), writeValues, highTapPosition);
            write(twtId, "tapChangerTargetV", found, twtData.targetV(), writeValues, targetV);
            write(twtId, "regulatedSide", found, twtData.regulatedSide() != null ? twtData.regulatedSide().name() : invalidString, writeValues, regulatedSide != null ? regulatedSide.name() : invalidString);
            write(twtId, "v", found, twtData.v(), writeValues, v);
            write(twtId, CONNECTED, found, twtData.connected(), writeValues, connected);
            write(twtId, MAIN_COMPONENT, found, twtData.mainComponent(), writeValues, mainComponent);
            write(twtId, VALIDATION, found, getValidated(twtData.validated()), writeValues, getValidated(validated));
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
