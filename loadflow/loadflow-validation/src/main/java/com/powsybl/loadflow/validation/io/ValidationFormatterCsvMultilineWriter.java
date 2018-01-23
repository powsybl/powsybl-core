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

import com.powsybl.commons.io.table.Column;
import com.powsybl.commons.io.table.TableFormatterConfig;
import com.powsybl.commons.io.table.TableFormatterFactory;
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
    }

    public ValidationFormatterCsvMultilineWriter(String id, Class<? extends TableFormatterFactory> formatterFactoryClass,
                                            Writer writer, boolean verbose, ValidationType validationType, boolean compareResults) {
        this(id, formatterFactoryClass, TableFormatterConfig.load(), writer, verbose, validationType, compareResults);
    }

    protected Column[] getColumns() {
        if (compareResults) {
            return new Column[] {
                new Column("id"),
                new Column("characteristic"),
                new Column("value"),
                new Column("value" + POST_LF_SUFFIX)
            };
        }
        return new Column[] {
            new Column("id"),
            new Column("characteristic"),
            new Column("value")
        };
    }

    @Override
    protected void write(String branchId, double p1, double p1Calc, double q1, double q1Calc, double p2, double p2Calc, double q2, double q2Calc,
                         double r, double x, double g1, double g2, double b1, double b2, double rho1, double rho2, double alpha1, double alpha2,
                         double u1, double u2, double theta1, double theta2, double z, double y, double ksi, boolean connected1, boolean connected2,
                         boolean mainComponent1, boolean mainComponent2, boolean validated, FlowData flowData, boolean found, boolean writeValues) throws IOException {
        write(branchId, "network_p1", found, found ? flowData.p1 : Double.NaN, writeValues, p1);
        write(branchId, "expected_p1", found, found ? flowData.p1Calc : Double.NaN, writeValues, p1Calc);
        write(branchId, "network_q1", found, found ? flowData.q1 : Double.NaN, writeValues, q1);
        write(branchId, "expected_q1", found, found ? flowData.q1Calc : Double.NaN, writeValues, q1Calc);
        write(branchId, "network_p2", found, found ? flowData.p2 : Double.NaN, writeValues, p2);
        write(branchId, "expected_p2", found, found ? flowData.p2Calc : Double.NaN, writeValues, p2Calc);
        write(branchId, "network_q2", found, found ? flowData.q2 : Double.NaN, writeValues, q2);
        write(branchId, "expected_q2", found, found ? flowData.q2Calc : Double.NaN, writeValues, q2Calc);
        if (verbose) {
            write(branchId, "r", found, found ? flowData.r : Double.NaN, writeValues, r);
            write(branchId, "x", found, found ? flowData.x : Double.NaN, writeValues, x);
            write(branchId, "g1", found, found ? flowData.g1 : Double.NaN, writeValues, g1);
            write(branchId, "g2", found, found ? flowData.g2 : Double.NaN, writeValues, g2);
            write(branchId, "b1", found, found ? flowData.b1 : Double.NaN, writeValues, b1);
            write(branchId, "b2", found, found ? flowData.b2 : Double.NaN, writeValues, b2);
            write(branchId, "rho1", found, found ? flowData.rho1 : Double.NaN, writeValues, rho1);
            write(branchId, "rho2", found, found ? flowData.rho2 : Double.NaN, writeValues, rho2);
            write(branchId, "alpha1", found, found ? flowData.alpha1 : Double.NaN, writeValues, alpha1);
            write(branchId, "alpha2", found, found ? flowData.alpha2 : Double.NaN, writeValues, alpha2);
            write(branchId, "u1", found, found ? flowData.u1 : Double.NaN, writeValues, u1);
            write(branchId, "u2", found, found ? flowData.u2 : Double.NaN, writeValues, u2);
            write(branchId, "theta1", found, found ? flowData.theta1 : Double.NaN, writeValues, theta1);
            write(branchId, "theta2", found, found ? flowData.theta2 : Double.NaN, writeValues, theta2);
            write(branchId, "z", found, found ? flowData.z : Double.NaN, writeValues, z);
            write(branchId, "y", found, found ? flowData.y : Double.NaN, writeValues, y);
            write(branchId, "ksi", found, found ? flowData.ksi : Double.NaN, writeValues, ksi);
            write(branchId, "connected1", found, found ? flowData.connected1 : false, writeValues, connected1);
            write(branchId, "connected2", found, found ? flowData.connected2 : false, writeValues, connected2);
            write(branchId, "mainComponent1", found, found ? flowData.mainComponent1 : false, writeValues, mainComponent1);
            write(branchId, "mainComponent2", found, found ? flowData.mainComponent2 : false, writeValues, mainComponent2);
            writeValidated(branchId, VALIDATION, found, found ? flowData.validated : false, writeValues, validated);
        }
    }

    @Override
    protected void write(String generatorId, float p, float q, float v, float targetP, float targetQ, float targetV,
            boolean connected, boolean voltageRegulatorOn, float minQ, float maxQ, boolean validated,
            GeneratorData generatorData, boolean found, boolean writeValues) throws IOException {
        write(generatorId, "p", found, found ? -generatorData.p : Float.NaN, writeValues, -p);
        write(generatorId, "q", found, found ? -generatorData.q : Float.NaN, writeValues, -q);
        write(generatorId, "v", found, found ? generatorData.v : Float.NaN, writeValues, v);
        write(generatorId, "targetP", found, found ? generatorData.targetP : Float.NaN, writeValues, targetP);
        write(generatorId, "targetQ", found, found ? generatorData.targetQ : Float.NaN, writeValues, targetQ);
        write(generatorId, "targetV", found, found ? generatorData.targetV : Float.NaN, writeValues, targetV);
        if (verbose) {
            write(generatorId, CONNECTED, found, found ? generatorData.connected : false, writeValues, connected);
            write(generatorId, "voltageRegulatorOn", found, found ? generatorData.voltageRegulatorOn : false, writeValues, voltageRegulatorOn);
            write(generatorId, "minQ", found, found ? generatorData.minQ : Float.NaN, writeValues, minQ);
            write(generatorId, "maxQ", found, found ? generatorData.maxQ : Float.NaN, writeValues, maxQ);
            writeValidated(generatorId, VALIDATION, found, found ? generatorData.validated : false, writeValues, validated);
        }
    }

    @Override
    protected void write(String busId, double incomingP, double incomingQ, double loadP, double loadQ, double genP, double genQ,
                         double shuntP, double shuntQ, double svcP, double svcQ, double vscCSP, double vscCSQ, double lineP, double lineQ,
                         double twtP, double twtQ, double tltP, double tltQ, boolean validated, BusData busData, boolean found,
                         boolean writeValues) throws IOException {
        write(busId, "incomingP", found, found ? busData.incomingP : Double.NaN, writeValues, incomingP);
        write(busId, "incomingQ", found, found ? busData.incomingQ : Double.NaN, writeValues, incomingQ);
        write(busId, "loadP", found, found ? busData.loadP : Double.NaN, writeValues, loadP);
        write(busId, "loadQ", found, found ? busData.loadQ : Double.NaN, writeValues, loadQ);
        if (verbose) {
            write(busId, "genP", found, found ? busData.genP : Double.NaN, writeValues, genP);
            write(busId, "genQ", found, found ? busData.genQ : Double.NaN, writeValues, genQ);
            write(busId, "shuntP", found, found ? busData.shuntP : Double.NaN, writeValues, shuntP);
            write(busId, "shuntQ", found, found ? busData.shuntQ : Double.NaN, writeValues, shuntQ);
            write(busId, "svcP", found, found ? busData.svcP : Double.NaN, writeValues, svcP);
            write(busId, "svcQ", found, found ? busData.svcQ : Double.NaN, writeValues, svcQ);
            write(busId, "vscCSP", found, found ? busData.vscCSP : Double.NaN, writeValues, vscCSP);
            write(busId, "vscCSQ", found, found ? busData.vscCSQ : Double.NaN, writeValues, vscCSQ);
            write(busId, "lineP", found, found ? busData.lineP : Double.NaN, writeValues, lineP);
            write(busId, "lineQ", found, found ? busData.lineQ : Double.NaN, writeValues, lineQ);
            write(busId, "twtP", found, found ? busData.twtP : Double.NaN, writeValues, twtP);
            write(busId, "twtQ", found, found ? busData.twtQ : Double.NaN, writeValues, twtQ);
            write(busId, "tltP", found, found ? busData.tltP : Double.NaN, writeValues, tltP);
            write(busId, "tltQ", found, found ? busData.tltQ : Double.NaN, writeValues, tltQ);
            writeValidated(busId, VALIDATION, found, found ? busData.validated : false, writeValues, validated);
        }
    }

    @Override
    protected void write(String svcId, float p, float q, float v, float reactivePowerSetpoint, float voltageSetpoint,
                         boolean connected, RegulationMode regulationMode, float bMin, float bMax, boolean validated,
                         SvcData svcData, boolean found, boolean writeValues) throws IOException {
        write(svcId, "p", found, found ? -svcData.p : Float.NaN, writeValues, -p);
        write(svcId, "q", found, found ? -svcData.q : Float.NaN, writeValues, -q);
        write(svcId, "v", found, found ? svcData.v : Float.NaN, writeValues, v);
        write(svcId, "reactivePowerSetpoint", found, found ? svcData.reactivePowerSetpoint : Float.NaN, writeValues, reactivePowerSetpoint);
        write(svcId, "voltageSetpoint", found, found ? svcData.voltageSetpoint : Float.NaN, writeValues, voltageSetpoint);
        if (verbose) {
            write(svcId, CONNECTED, found, found ? svcData.connected : false, writeValues, connected);
            write(svcId, "regulationMode", found, found ? svcData.regulationMode.name() : "", writeValues, writeValues ? regulationMode.name() : "");
            write(svcId, "bMin", found, found ? svcData.bMin : Float.NaN, writeValues, bMin);
            write(svcId, "bMax", found, found ? svcData.bMax : Float.NaN, writeValues, bMax);
            writeValidated(svcId, VALIDATION, found, found ? svcData.validated : false, writeValues, validated);
        }
    }

    protected void write(String shuntId, float q, float expectedQ, float p, int currentSectionCount, int maximumSectionCount,
                         float bPerSection, float v, boolean connected, float qMax, float nominalV, boolean validated,
                         ShuntData shuntData, boolean found, boolean writeValues) throws IOException {
        write(shuntId, "q", found, found ? shuntData.q : Float.NaN, writeValues, q);
        write(shuntId, "expectedQ", found, found ? shuntData.expectedQ : Float.NaN, writeValues, expectedQ);
        if (verbose) {
            write(shuntId, "p", found, found ? shuntData.p : Float.NaN, writeValues, p);
            write(shuntId, "currentSectionCount", found, found ? shuntData.currentSectionCount : -1, writeValues, currentSectionCount);
            write(shuntId, "maximumSectionCount", found, found ? shuntData.maximumSectionCount : -1, writeValues, maximumSectionCount);
            write(shuntId, "bPerSection", found, found ? shuntData.bPerSection : Float.NaN, writeValues, bPerSection);
            write(shuntId, "v", found, found ? shuntData.v : Float.NaN, writeValues, v);
            write(shuntId, CONNECTED, found, found ? shuntData.connected : false, writeValues, connected);
            write(shuntId, "qMax", found, found ? shuntData.qMax : Float.NaN, writeValues, qMax);
            write(shuntId, "nominalV", found, found ? shuntData.nominalV : Float.NaN, writeValues, nominalV);
            writeValidated(shuntId, VALIDATION, found, found ? shuntData.validated : false, writeValues, validated);
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

    private void writeValidated(String id, String label, boolean writeFirst, boolean first, boolean writeSecond, boolean second) throws IOException {
        formatter.writeCell(id).writeCell(label);
        if (compareResults) {
            formatter = writeFirst ? formatter.writeCell(first ? SUCCESS : FAIL) : formatter.writeEmptyCell();
        }
        formatter = writeSecond ? formatter.writeCell(second ? SUCCESS : FAIL) : formatter.writeEmptyCell();
    }
}
