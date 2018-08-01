/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow.validation.io;

import java.io.IOException;
import java.io.Writer;
import java.util.Objects;

import org.apache.commons.lang3.ArrayUtils;

import com.powsybl.commons.io.table.Column;
import com.powsybl.commons.io.table.TableFormatterConfig;
import com.powsybl.commons.io.table.TableFormatterFactory;
import com.powsybl.iidm.network.Branch.Side;
import com.powsybl.iidm.network.StaticVarCompensator.RegulationMode;
import com.powsybl.loadflow.validation.ValidationType;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.it>
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
    protected void write(String branchId, double p1, double p1Calc, double q1, double q1Calc, double p2, double p2Calc, double q2, double q2Calc,
                         double r, double x, double g1, double g2, double b1, double b2, double rho1, double rho2, double alpha1, double alpha2,
                         double u1, double u2, double theta1, double theta2, double z, double y, double ksi, boolean connected1, boolean connected2,
                         boolean mainComponent1, boolean mainComponent2, boolean validated, FlowData flowData, boolean found, boolean writeValues) throws IOException {
        write(branchId, "network_p1", found, flowData.p1, writeValues, p1);
        write(branchId, "expected_p1", found, flowData.p1Calc, writeValues, p1Calc);
        write(branchId, "network_q1", found, flowData.q1, writeValues, q1);
        write(branchId, "expected_q1", found, flowData.q1Calc, writeValues, q1Calc);
        write(branchId, "network_p2", found, flowData.p2, writeValues, p2);
        write(branchId, "expected_p2", found, flowData.p2Calc, writeValues, p2Calc);
        write(branchId, "network_q2", found, flowData.q2, writeValues, q2);
        write(branchId, "expected_q2", found, flowData.q2Calc, writeValues, q2Calc);
        if (verbose) {
            write(branchId, "r", found, flowData.r, writeValues, r);
            write(branchId, "x", found, flowData.x, writeValues, x);
            write(branchId, "g1", found, flowData.g1, writeValues, g1);
            write(branchId, "g2", found, flowData.g2, writeValues, g2);
            write(branchId, "b1", found, flowData.b1, writeValues, b1);
            write(branchId, "b2", found, flowData.b2, writeValues, b2);
            write(branchId, "rho1", found, flowData.rho1, writeValues, rho1);
            write(branchId, "rho2", found, flowData.rho2, writeValues, rho2);
            write(branchId, "alpha1", found, flowData.alpha1, writeValues, alpha1);
            write(branchId, "alpha2", found, flowData.alpha2, writeValues, alpha2);
            write(branchId, "u1", found, flowData.u1, writeValues, u1);
            write(branchId, "u2", found, flowData.u2, writeValues, u2);
            write(branchId, "theta1", found, flowData.theta1, writeValues, theta1);
            write(branchId, "theta2", found, flowData.theta2, writeValues, theta2);
            write(branchId, "z", found, flowData.z, writeValues, z);
            write(branchId, "y", found, flowData.y, writeValues, y);
            write(branchId, "ksi", found, flowData.ksi, writeValues, ksi);
            write(branchId, CONNECTED + "1", found, flowData.connected1, writeValues, connected1);
            write(branchId, CONNECTED + "2", found, flowData.connected2, writeValues, connected2);
            write(branchId, MAIN_COMPONENT + "1", found, flowData.mainComponent1, writeValues, mainComponent1);
            write(branchId, MAIN_COMPONENT + "2", found, flowData.mainComponent2, writeValues, mainComponent2);
            write(branchId, VALIDATION, found, getValidated(flowData.validated), writeValues, getValidated(validated));
        }
    }

    @Override
    protected void write(String generatorId, double p, double q, double v, double targetP, double targetQ, double targetV,
            boolean connected, boolean voltageRegulatorOn, double minP, double maxP, double minQ, double maxQ, boolean mainComponent,
            boolean validated, GeneratorData generatorData, boolean found, boolean writeValues) throws IOException {
        write(generatorId, "p", found, -generatorData.p, writeValues, -p);
        write(generatorId, "q", found, -generatorData.q, writeValues, -q);
        write(generatorId, "v", found, generatorData.v, writeValues, v);
        write(generatorId, "targetP", found, generatorData.targetP, writeValues, targetP);
        write(generatorId, "targetQ", found, generatorData.targetQ, writeValues, targetQ);
        write(generatorId, "targetV", found, generatorData.targetV, writeValues, targetV);
        if (verbose) {
            write(generatorId, CONNECTED, found, generatorData.connected, writeValues, connected);
            write(generatorId, "voltageRegulatorOn", found, generatorData.voltageRegulatorOn, writeValues, voltageRegulatorOn);
            write(generatorId, "minP", found, generatorData.minP, writeValues, minP);
            write(generatorId, "maxP", found, generatorData.maxP, writeValues, maxP);
            write(generatorId, "minQ", found, generatorData.minQ, writeValues, minQ);
            write(generatorId, "maxQ", found, generatorData.maxQ, writeValues, maxQ);
            write(generatorId, MAIN_COMPONENT, found, generatorData.mainComponent, writeValues, mainComponent);
            write(generatorId, VALIDATION, found, getValidated(generatorData.validated), writeValues, getValidated(validated));
        }
    }

    @Override
    protected void write(String busId, double incomingP, double incomingQ, double loadP, double loadQ, double genP, double genQ,
                         double shuntP, double shuntQ, double svcP, double svcQ, double vscCSP, double vscCSQ, double lineP, double lineQ,
                         double danglingLineP, double danglingLineQ, double twtP, double twtQ, double tltP, double tltQ, boolean validated,
                         boolean mainComponent, BusData busData, boolean found, boolean writeValues) throws IOException {
        write(busId, "incomingP", found, busData.incomingP, writeValues, incomingP);
        write(busId, "incomingQ", found, busData.incomingQ, writeValues, incomingQ);
        write(busId, "loadP", found, busData.loadP, writeValues, loadP);
        write(busId, "loadQ", found, busData.loadQ, writeValues, loadQ);
        if (verbose) {
            write(busId, "genP", found, busData.genP, writeValues, genP);
            write(busId, "genQ", found, busData.genQ, writeValues, genQ);
            write(busId, "shuntP", found, busData.shuntP, writeValues, shuntP);
            write(busId, "shuntQ", found, busData.shuntQ, writeValues, shuntQ);
            write(busId, "svcP", found, busData.svcP, writeValues, svcP);
            write(busId, "svcQ", found, busData.svcQ, writeValues, svcQ);
            write(busId, "vscCSP", found, busData.vscCSP, writeValues, vscCSP);
            write(busId, "vscCSQ", found, busData.vscCSQ, writeValues, vscCSQ);
            write(busId, "lineP", found, busData.lineP, writeValues, lineP);
            write(busId, "lineQ", found, busData.lineQ, writeValues, lineQ);
            write(busId, "danglingLineP", found, busData.danglingLineP, writeValues, danglingLineP);
            write(busId, "danglingLineQ", found, busData.danglingLineQ, writeValues, danglingLineQ);
            write(busId, "twtP", found, busData.twtP, writeValues, twtP);
            write(busId, "twtQ", found, busData.twtQ, writeValues, twtQ);
            write(busId, "tltP", found, busData.tltP, writeValues, tltP);
            write(busId, "tltQ", found, busData.tltQ, writeValues, tltQ);
            write(busId, MAIN_COMPONENT, found, busData.mainComponent, writeValues, mainComponent);
            write(busId, VALIDATION, found, getValidated(busData.validated), writeValues, getValidated(validated));
        }
    }

    @Override
    protected void write(String svcId, double p, double q, double v, double nominalV, double reactivePowerSetpoint, double voltageSetpoint,
                         boolean connected, RegulationMode regulationMode, double bMin, double bMax, boolean mainComponent, boolean validated,
                         SvcData svcData, boolean found, boolean writeValues) throws IOException {
        write(svcId, "p", found, -svcData.p, writeValues, -p);
        write(svcId, "q", found, -svcData.q, writeValues, -q);
        write(svcId, "v", found, svcData.v, writeValues, v);
        write(svcId, NOMINAL_V, found, svcData.nominalV, writeValues, nominalV);
        write(svcId, "reactivePowerSetpoint", found, svcData.reactivePowerSetpoint, writeValues, reactivePowerSetpoint);
        write(svcId, "voltageSetpoint", found, svcData.voltageSetpoint, writeValues, voltageSetpoint);
        if (verbose) {
            write(svcId, CONNECTED, found, svcData.connected, writeValues, connected);
            write(svcId, "regulationMode", found, svcData.regulationMode.name(), writeValues, regulationMode.name());
            write(svcId, "bMin", found, svcData.bMin, writeValues, bMin);
            write(svcId, "bMax", found, svcData.bMax, writeValues, bMax);
            write(svcId, MAIN_COMPONENT, found, svcData.mainComponent, writeValues, mainComponent);
            write(svcId, VALIDATION, found, getValidated(svcData.validated), writeValues, getValidated(validated));
        }
    }

    protected void write(String shuntId, double q, double expectedQ, double p, int currentSectionCount, int maximumSectionCount,
                         double bPerSection, double v, boolean connected, double qMax, double nominalV, boolean mainComponent,
                         boolean validated, ShuntData shuntData, boolean found, boolean writeValues) throws IOException {
        write(shuntId, "q", found, shuntData.q, writeValues, q);
        write(shuntId, "expectedQ", found, shuntData.expectedQ, writeValues, expectedQ);
        if (verbose) {
            write(shuntId, "p", found, shuntData.p, writeValues, p);
            write(shuntId, "currentSectionCount", found, shuntData.currentSectionCount, writeValues, currentSectionCount);
            write(shuntId, "maximumSectionCount", found, shuntData.maximumSectionCount, writeValues, maximumSectionCount);
            write(shuntId, "bPerSection", found, shuntData.bPerSection, writeValues, bPerSection);
            write(shuntId, "v", found, shuntData.v, writeValues, v);
            write(shuntId, CONNECTED, found, shuntData.connected, writeValues, connected);
            write(shuntId, "qMax", found, shuntData.qMax, writeValues, qMax);
            write(shuntId, NOMINAL_V, found, shuntData.nominalV, writeValues, nominalV);
            write(shuntId, MAIN_COMPONENT, found, shuntData.mainComponent, writeValues, mainComponent);
            write(shuntId, VALIDATION, found, getValidated(shuntData.validated), writeValues, getValidated(validated));
        }
    }

    @Override
    protected void write(String twtId, double error, double upIncrement, double downIncrement, double rho, double rhoPreviousStep, double rhoNextStep,
                         int tapPosition, int lowTapPosition, int highTapPosition, double targetV, Side regulatedSide, double v, boolean connected,
                         boolean mainComponent, boolean validated, TransformerData twtData, boolean found, boolean writeValues) throws IOException {
        write(twtId, "error", found, twtData.error, writeValues, error);
        write(twtId, "upIncrement", found, twtData.upIncrement, writeValues, upIncrement);
        write(twtId, "downIncrement", found, twtData.downIncrement, writeValues, downIncrement);
        if (verbose) {
            write(twtId, "rho", found, twtData.rho, writeValues, rho);
            write(twtId, "rhoPreviousStep", found, twtData.rhoPreviousStep, writeValues, rhoPreviousStep);
            write(twtId, "rhoNextStep", found, twtData.rhoNextStep, writeValues, rhoNextStep);
            write(twtId, "tapPosition", found, twtData.tapPosition, writeValues, tapPosition);
            write(twtId, "lowTapPosition", found, twtData.lowTapPosition, writeValues, lowTapPosition);
            write(twtId, "highTapPosition", found, twtData.highTapPosition, writeValues, highTapPosition);
            write(twtId, "tapChangerTargetV", found, twtData.targetV, writeValues, targetV);
            write(twtId, "regulatedSide", found, twtData.regulatedSide != null ? twtData.regulatedSide.name() : invalidString, writeValues, regulatedSide != null ? regulatedSide.name() : invalidString);
            write(twtId, "v", found, twtData.v, writeValues, v);
            write(twtId, CONNECTED, found, twtData.connected, writeValues, connected);
            write(twtId, MAIN_COMPONENT, found, twtData.mainComponent, writeValues, mainComponent);
            write(twtId, VALIDATION, found, getValidated(twtData.validated), writeValues, getValidated(validated));
        }
    }

    private void write(String id, String label, boolean writeFirst, float first, boolean writeSecond, float second) throws IOException {
        formatter.writeCell(id).writeCell(label);
        if (compareResults) {
            formatter = writeFirst ? formatter.writeCell(first) : formatter.writeEmptyCell();
        }
        formatter = writeSecond ? formatter.writeCell(second) : formatter.writeEmptyCell();
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
