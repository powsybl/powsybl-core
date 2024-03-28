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

import com.powsybl.iidm.network.ThreeSides;
import org.apache.commons.lang3.ArrayUtils;

import com.powsybl.commons.io.table.Column;
import com.powsybl.commons.io.table.TableFormatter;
import com.powsybl.commons.io.table.TableFormatterConfig;
import com.powsybl.commons.io.table.TableFormatterFactory;
import com.powsybl.iidm.network.TwoSides;
import com.powsybl.iidm.network.StaticVarCompensator.RegulationMode;
import com.powsybl.iidm.network.util.TwtData;
import com.powsybl.loadflow.validation.ValidationType;

/**
 *
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.it>}
 */
public class ValidationFormatterCsvWriter extends AbstractValidationFormatterWriter {

    private final boolean verbose;

    public ValidationFormatterCsvWriter(String id, Class<? extends TableFormatterFactory> formatterFactoryClass,
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

    public ValidationFormatterCsvWriter(String id, Class<? extends TableFormatterFactory> formatterFactoryClass,
                                   Writer writer, boolean verbose, ValidationType validationType, boolean compareResults) {
        this(id, formatterFactoryClass, TableFormatterConfig.load(), writer, verbose, validationType, compareResults);
    }

    protected Column[] getColumns() {
        switch (validationType) {
            case FLOWS:
                return getFlowColumns();
            case GENERATORS:
                return getGeneratorColumns();
            case BUSES:
                return getBusColumns();
            case SVCS:
                return getSvcColumns();
            case SHUNTS:
                return getShuntColumns();
            case TWTS:
                return getTwtColumns();
            case TWTS3W:
                return getTwt3wColumns();
            default:
                throw new IllegalStateException("Unexpected ValidationType value: " + validationType);
        }
    }

    private Column[] getFlowColumns() {
        Column[] flowColumns = new Column[] {
            new Column("id"),
            new Column(NETWORK_P1),
            new Column(EXPECTED_P1),
            new Column(NETWORK_Q1),
            new Column(EXPECTED_Q1),
            new Column(NETWORK_P2),
            new Column(EXPECTED_P2),
            new Column(NETWORK_Q2),
            new Column(EXPECTED_Q2)
        };
        if (verbose) {
            flowColumns = ArrayUtils.addAll(flowColumns,
                                            new Column("r"),
                                            new Column("x"),
                                            new Column("g1"),
                                            new Column("g2"),
                                            new Column("b1"),
                                            new Column("b2"),
                                            new Column("rho1"),
                                            new Column("rho2"),
                                            new Column("alpha1"),
                                            new Column("alpha2"),
                                            new Column("u1"),
                                            new Column("u2"),
                                            new Column(THETA1),
                                            new Column(THETA2),
                                            new Column("z"),
                                            new Column("y"),
                                            new Column("ksi"),
                                            new Column("phaseAngleClock"),
                                            new Column(CONNECTED + "1"),
                                            new Column(CONNECTED + "2"),
                                            new Column(MAIN_COMPONENT + "1"),
                                            new Column(MAIN_COMPONENT + "2"),
                                            new Column(VALIDATION));
        }
        if (compareResults) {
            flowColumns = ArrayUtils.addAll(flowColumns,
                                            new Column(NETWORK_P1 + POST_COMPUTATION_SUFFIX),
                                            new Column(EXPECTED_P1 + POST_COMPUTATION_SUFFIX),
                                            new Column(NETWORK_Q1 + POST_COMPUTATION_SUFFIX),
                                            new Column(EXPECTED_Q1 + POST_COMPUTATION_SUFFIX),
                                            new Column(NETWORK_P2 + POST_COMPUTATION_SUFFIX),
                                            new Column(EXPECTED_P2 + POST_COMPUTATION_SUFFIX),
                                            new Column(NETWORK_Q2 + POST_COMPUTATION_SUFFIX),
                                            new Column(EXPECTED_Q2 + POST_COMPUTATION_SUFFIX));
            if (verbose) {
                flowColumns = ArrayUtils.addAll(flowColumns,
                                                new Column("r" + POST_COMPUTATION_SUFFIX),
                                                new Column("x" + POST_COMPUTATION_SUFFIX),
                                                new Column("g1" + POST_COMPUTATION_SUFFIX),
                                                new Column("g2" + POST_COMPUTATION_SUFFIX),
                                                new Column("b1" + POST_COMPUTATION_SUFFIX),
                                                new Column("b2" + POST_COMPUTATION_SUFFIX),
                                                new Column("rho1" + POST_COMPUTATION_SUFFIX),
                                                new Column("rho2" + POST_COMPUTATION_SUFFIX),
                                                new Column("alpha1" + POST_COMPUTATION_SUFFIX),
                                                new Column("alpha2" + POST_COMPUTATION_SUFFIX),
                                                new Column("u1" + POST_COMPUTATION_SUFFIX),
                                                new Column("u2" + POST_COMPUTATION_SUFFIX),
                                                new Column(THETA1 + POST_COMPUTATION_SUFFIX),
                                                new Column(THETA2 + POST_COMPUTATION_SUFFIX),
                                                new Column("z" + POST_COMPUTATION_SUFFIX),
                                                new Column("y" + POST_COMPUTATION_SUFFIX),
                                                new Column("ksi" + POST_COMPUTATION_SUFFIX),
                                                new Column("phaseAngleClock" + POST_COMPUTATION_SUFFIX),
                                                new Column(CONNECTED + "1" + POST_COMPUTATION_SUFFIX),
                                                new Column(CONNECTED + "2" + POST_COMPUTATION_SUFFIX),
                                                new Column(MAIN_COMPONENT + "1" + POST_COMPUTATION_SUFFIX),
                                                new Column(MAIN_COMPONENT + "2" + POST_COMPUTATION_SUFFIX),
                                                new Column(VALIDATION + POST_COMPUTATION_SUFFIX));
            }
        }
        return flowColumns;
    }

    private Column[] getGeneratorColumns() {
        Column[] generatorColumns = new Column[] {
            new Column("id"),
            new Column("p"),
            new Column("q"),
            new Column("v"),
            new Column("targetP"),
            new Column("targetQ"),
            new Column("targetV"),
            new Column("expectedP"),
        };
        if (verbose) {
            generatorColumns = ArrayUtils.addAll(generatorColumns,
                                                 new Column(CONNECTED),
                                                 new Column("voltageRegulatorOn"),
                                                 new Column("minP"),
                                                 new Column("maxP"),
                                                 new Column("minQ"),
                                                 new Column("maxQ"),
                                                 new Column(MAIN_COMPONENT),
                                                 new Column(VALIDATION));
        }
        if (compareResults) {
            generatorColumns = ArrayUtils.addAll(generatorColumns,
                                                 new Column("p" + POST_COMPUTATION_SUFFIX),
                                                 new Column("q" + POST_COMPUTATION_SUFFIX),
                                                 new Column("v" + POST_COMPUTATION_SUFFIX),
                                                 new Column("targetP" + POST_COMPUTATION_SUFFIX),
                                                 new Column("targetQ" + POST_COMPUTATION_SUFFIX),
                                                 new Column("targetV" + POST_COMPUTATION_SUFFIX),
                                                 new Column("expectedP" + POST_COMPUTATION_SUFFIX));
            if (verbose) {
                generatorColumns = ArrayUtils.addAll(generatorColumns,
                                                     new Column(CONNECTED + POST_COMPUTATION_SUFFIX),
                                                     new Column("voltageRegulatorOn" + POST_COMPUTATION_SUFFIX),
                                                     new Column("minP" + POST_COMPUTATION_SUFFIX),
                                                     new Column("maxP" + POST_COMPUTATION_SUFFIX),
                                                     new Column("minQ" + POST_COMPUTATION_SUFFIX),
                                                     new Column("maxQ" + POST_COMPUTATION_SUFFIX),
                                                     new Column(MAIN_COMPONENT + POST_COMPUTATION_SUFFIX),
                                                     new Column(VALIDATION + POST_COMPUTATION_SUFFIX));
            }
        }
        return generatorColumns;
    }

    private Column[] getBusColumns() {
        Column[] busColumns = new Column[] {
            new Column("id"),
            new Column("incomingP"),
            new Column("incomingQ"),
            new Column("loadP"),
            new Column("loadQ")
        };
        if (verbose) {
            busColumns = ArrayUtils.addAll(busColumns,
                                           new Column("genP"),
                                           new Column("genQ"),
                                           new Column("shuntP"),
                                           new Column("shuntQ"),
                                           new Column("svcP"),
                                           new Column("svcQ"),
                                           new Column("vscCSP"),
                                           new Column("vscCSQ"),
                                           new Column("lineP"),
                                           new Column("lineQ"),
                                           new Column("danglingLineP"),
                                           new Column("danglingLineQ"),
                                           new Column("twtP"),
                                           new Column("twtQ"),
                                           new Column("tltP"),
                                           new Column("tltQ"),
                                           new Column(MAIN_COMPONENT),
                                           new Column(VALIDATION));
        }
        if (compareResults) {
            busColumns = ArrayUtils.addAll(busColumns,
                                           new Column("incomingP" + POST_COMPUTATION_SUFFIX),
                                           new Column("incomingQ" + POST_COMPUTATION_SUFFIX),
                                           new Column("loadP" + POST_COMPUTATION_SUFFIX),
                                           new Column("loadQ" + POST_COMPUTATION_SUFFIX));
            if (verbose) {
                busColumns = ArrayUtils.addAll(busColumns,
                                               new Column("genP" + POST_COMPUTATION_SUFFIX),
                                               new Column("genQ" + POST_COMPUTATION_SUFFIX),
                                               new Column("shuntP" + POST_COMPUTATION_SUFFIX),
                                               new Column("shuntQ" + POST_COMPUTATION_SUFFIX),
                                               new Column("svcP" + POST_COMPUTATION_SUFFIX),
                                               new Column("svcQ" + POST_COMPUTATION_SUFFIX),
                                               new Column("vscCSP" + POST_COMPUTATION_SUFFIX),
                                               new Column("vscCSQ" + POST_COMPUTATION_SUFFIX),
                                               new Column("lineP" + POST_COMPUTATION_SUFFIX),
                                               new Column("lineQ" + POST_COMPUTATION_SUFFIX),
                                               new Column("danglingLineP" + POST_COMPUTATION_SUFFIX),
                                               new Column("danglingLineQ" + POST_COMPUTATION_SUFFIX),
                                               new Column("twtP" + POST_COMPUTATION_SUFFIX),
                                               new Column("twtQ" + POST_COMPUTATION_SUFFIX),
                                               new Column("tltP" + POST_COMPUTATION_SUFFIX),
                                               new Column("tltQ" + POST_COMPUTATION_SUFFIX),
                                               new Column(MAIN_COMPONENT + POST_COMPUTATION_SUFFIX),
                                               new Column(VALIDATION + POST_COMPUTATION_SUFFIX));
            }
        }
        return busColumns;
    }

    private Column[] getSvcColumns() {
        Column[] svcColumns = new Column[] {
            new Column("id"),
            new Column("p"),
            new Column("q"),
            new Column("vControlled"),
            new Column("vController"),
            new Column(NOMINAL_V),
            new Column("reactivePowerSetpoint"),
            new Column("voltageSetpoint")
        };
        if (verbose) {
            svcColumns = ArrayUtils.addAll(svcColumns,
                                           new Column(CONNECTED),
                                           new Column("regulationMode"),
                                           new Column("bMin"),
                                           new Column("bMax"),
                                           new Column(MAIN_COMPONENT),
                                           new Column(VALIDATION));
        }
        if (compareResults) {
            svcColumns = ArrayUtils.addAll(svcColumns,
                                           new Column("p" + POST_COMPUTATION_SUFFIX),
                                           new Column("q" + POST_COMPUTATION_SUFFIX),
                                           new Column("vControlled" + POST_COMPUTATION_SUFFIX),
                                           new Column("vController" + POST_COMPUTATION_SUFFIX),
                                           new Column(NOMINAL_V + POST_COMPUTATION_SUFFIX),
                                           new Column("reactivePowerSetpoint" + POST_COMPUTATION_SUFFIX),
                                           new Column("voltageSetpoint" + POST_COMPUTATION_SUFFIX));
            if (verbose) {
                svcColumns = ArrayUtils.addAll(svcColumns,
                                               new Column(CONNECTED + POST_COMPUTATION_SUFFIX),
                                               new Column("regulationMode" + POST_COMPUTATION_SUFFIX),
                                               new Column("bMin" + POST_COMPUTATION_SUFFIX),
                                               new Column("bMax" + POST_COMPUTATION_SUFFIX),
                                               new Column(MAIN_COMPONENT + POST_COMPUTATION_SUFFIX),
                                               new Column(VALIDATION + POST_COMPUTATION_SUFFIX));
            }
        }
        return svcColumns;
    }

    private Column[] getShuntColumns() {
        Column[] shuntColumns = new Column[] {
            new Column("id"),
            new Column("q"),
            new Column("expectedQ"),
        };
        if (verbose) {
            shuntColumns = ArrayUtils.addAll(shuntColumns,
                                             new Column("p"),
                                             new Column("currentSectionCount"),
                                             new Column("maximumSectionCount"),
                                             new Column("bPerSection"),
                                             new Column("v"),
                                             new Column(CONNECTED),
                                             new Column("qMax"),
                                             new Column(NOMINAL_V),
                                             new Column(MAIN_COMPONENT),
                                             new Column(VALIDATION));
        }
        if (compareResults) {
            shuntColumns = ArrayUtils.addAll(shuntColumns,
                                             new Column("q" + POST_COMPUTATION_SUFFIX),
                                             new Column("expectedQ" + POST_COMPUTATION_SUFFIX));
            if (verbose) {
                shuntColumns = ArrayUtils.addAll(shuntColumns,
                                                 new Column("p" + POST_COMPUTATION_SUFFIX),
                                                 new Column("currentSectionCount" + POST_COMPUTATION_SUFFIX),
                                                 new Column("maximumSectionCount" + POST_COMPUTATION_SUFFIX),
                                                 new Column("bPerSection" + POST_COMPUTATION_SUFFIX),
                                                 new Column("v" + POST_COMPUTATION_SUFFIX),
                                                 new Column(CONNECTED + POST_COMPUTATION_SUFFIX),
                                                 new Column("qMax" + POST_COMPUTATION_SUFFIX),
                                                 new Column(NOMINAL_V + POST_COMPUTATION_SUFFIX),
                                                 new Column(MAIN_COMPONENT + POST_COMPUTATION_SUFFIX),
                                                 new Column(VALIDATION + POST_COMPUTATION_SUFFIX));
            }
        }
        return shuntColumns;
    }

    private Column[] getTwtColumns() {
        Column[] twtColumns = new Column[] {
            new Column("id"),
            new Column("error"),
            new Column("upIncrement"),
            new Column("downIncrement")
        };
        if (verbose) {
            twtColumns = ArrayUtils.addAll(twtColumns,
                                           new Column("rho"),
                                           new Column("rhoPreviousStep"),
                                           new Column("rhoNextStep"),
                                           new Column("tapPosition"),
                                           new Column("lowTapPosition"),
                                           new Column("highTapPosition"),
                                           new Column("tapChangerTargetV"),
                                           new Column("regulatedSide"),
                                           new Column("v"),
                                           new Column(CONNECTED),
                                           new Column(MAIN_COMPONENT),
                                           new Column(VALIDATION));
        }
        if (compareResults) {
            twtColumns = ArrayUtils.addAll(twtColumns,
                                           new Column("error" + POST_COMPUTATION_SUFFIX),
                                           new Column("upIncrement" + POST_COMPUTATION_SUFFIX),
                                           new Column("downIncrement" + POST_COMPUTATION_SUFFIX));
            if (verbose) {
                twtColumns = ArrayUtils.addAll(twtColumns,
                                               new Column("rho" + POST_COMPUTATION_SUFFIX),
                                               new Column("rhoPreviousStep" + POST_COMPUTATION_SUFFIX),
                                               new Column("rhoNextStep" + POST_COMPUTATION_SUFFIX),
                                               new Column("tapPosition" + POST_COMPUTATION_SUFFIX),
                                               new Column("lowTapPosition" + POST_COMPUTATION_SUFFIX),
                                               new Column("highTapPosition" + POST_COMPUTATION_SUFFIX),
                                               new Column("tapChangerTargetV" + POST_COMPUTATION_SUFFIX),
                                               new Column("regulatedSide" + POST_COMPUTATION_SUFFIX),
                                               new Column("v" + POST_COMPUTATION_SUFFIX),
                                               new Column(CONNECTED + POST_COMPUTATION_SUFFIX),
                                               new Column(MAIN_COMPONENT + POST_COMPUTATION_SUFFIX),
                                               new Column(VALIDATION + POST_COMPUTATION_SUFFIX));
            }
        }
        return twtColumns;
    }

    private Column[] getTwt3wColumns() {
        Column[] twt3wColumns = new Column[] {
            new Column("id"),
            new Column(NETWORK_P1),
            new Column(EXPECTED_P1),
            new Column(NETWORK_Q1),
            new Column(EXPECTED_Q1),
            new Column(NETWORK_P2),
            new Column(EXPECTED_P2),
            new Column(NETWORK_Q2),
            new Column(EXPECTED_Q2),
            new Column(NETWORK_P3),
            new Column(EXPECTED_P3),
            new Column(NETWORK_Q3),
            new Column(EXPECTED_Q3)
        };
        if (verbose) {
            twt3wColumns = ArrayUtils.addAll(twt3wColumns,
                                            new Column("u1"),
                                            new Column("u2"),
                                            new Column("u3"),
                                            new Column("starU"),
                                            new Column(THETA1),
                                            new Column(THETA2),
                                            new Column(THETA3),
                                            new Column("starTheta"),
                                            new Column("g11"),
                                            new Column("b11"),
                                            new Column("g12"),
                                            new Column("b12"),
                                            new Column("g21"),
                                            new Column("b21"),
                                            new Column("g22"),
                                            new Column("b22"),
                                            new Column("g31"),
                                            new Column("b31"),
                                            new Column("g32"),
                                            new Column("b32"),
                                            new Column("r1"),
                                            new Column("r2"),
                                            new Column("r3"),
                                            new Column("x1"),
                                            new Column("x2"),
                                            new Column("x3"),
                                            new Column("ratedU1"),
                                            new Column("ratedU2"),
                                            new Column("ratedU3"),
                                            new Column("phaseAngleClock2"),
                                            new Column("phaseAngleClock3"),
                                            new Column("ratedU0"),
                                            new Column(CONNECTED + "1"),
                                            new Column(CONNECTED + "2"),
                                            new Column(CONNECTED + "3"),
                                            new Column(MAIN_COMPONENT + "1"),
                                            new Column(MAIN_COMPONENT + "2"),
                                            new Column(MAIN_COMPONENT + "3"),
                                            new Column(VALIDATION));
        }
        if (compareResults) {
            twt3wColumns = ArrayUtils.addAll(twt3wColumns,
                                            new Column(NETWORK_P1 + POST_COMPUTATION_SUFFIX),
                                            new Column(EXPECTED_P1 + POST_COMPUTATION_SUFFIX),
                                            new Column(NETWORK_Q1 + POST_COMPUTATION_SUFFIX),
                                            new Column(EXPECTED_Q1 + POST_COMPUTATION_SUFFIX),
                                            new Column(NETWORK_P2 + POST_COMPUTATION_SUFFIX),
                                            new Column(EXPECTED_P2 + POST_COMPUTATION_SUFFIX),
                                            new Column(NETWORK_Q2 + POST_COMPUTATION_SUFFIX),
                                            new Column(EXPECTED_Q2 + POST_COMPUTATION_SUFFIX),
                                            new Column(NETWORK_P3 + POST_COMPUTATION_SUFFIX),
                                            new Column(EXPECTED_P3 + POST_COMPUTATION_SUFFIX),
                                            new Column(NETWORK_Q3 + POST_COMPUTATION_SUFFIX),
                                            new Column(EXPECTED_Q3 + POST_COMPUTATION_SUFFIX));
            if (verbose) {
                twt3wColumns = ArrayUtils.addAll(twt3wColumns,
                                                new Column("u1" + POST_COMPUTATION_SUFFIX),
                                                new Column("u2" + POST_COMPUTATION_SUFFIX),
                                                new Column("u3" + POST_COMPUTATION_SUFFIX),
                                                new Column("starU" + POST_COMPUTATION_SUFFIX),
                                                new Column(THETA1 + POST_COMPUTATION_SUFFIX),
                                                new Column(THETA2 + POST_COMPUTATION_SUFFIX),
                                                new Column(THETA3 + POST_COMPUTATION_SUFFIX),
                                                new Column("starTheta" + POST_COMPUTATION_SUFFIX),
                                                new Column("g11" + POST_COMPUTATION_SUFFIX),
                                                new Column("b11" + POST_COMPUTATION_SUFFIX),
                                                new Column("g12" + POST_COMPUTATION_SUFFIX),
                                                new Column("b12" + POST_COMPUTATION_SUFFIX),
                                                new Column("g21" + POST_COMPUTATION_SUFFIX),
                                                new Column("b21" + POST_COMPUTATION_SUFFIX),
                                                new Column("g22" + POST_COMPUTATION_SUFFIX),
                                                new Column("b22" + POST_COMPUTATION_SUFFIX),
                                                new Column("g31" + POST_COMPUTATION_SUFFIX),
                                                new Column("b31" + POST_COMPUTATION_SUFFIX),
                                                new Column("g32" + POST_COMPUTATION_SUFFIX),
                                                new Column("b32" + POST_COMPUTATION_SUFFIX),
                                                new Column("r1" + POST_COMPUTATION_SUFFIX),
                                                new Column("r2" + POST_COMPUTATION_SUFFIX),
                                                new Column("r3" + POST_COMPUTATION_SUFFIX),
                                                new Column("x1" + POST_COMPUTATION_SUFFIX),
                                                new Column("x2" + POST_COMPUTATION_SUFFIX),
                                                new Column("x3" + POST_COMPUTATION_SUFFIX),
                                                new Column("ratedU1" + POST_COMPUTATION_SUFFIX),
                                                new Column("ratedU2" + POST_COMPUTATION_SUFFIX),
                                                new Column("ratedU3" + POST_COMPUTATION_SUFFIX),
                                                new Column("phaseAngleClock2" + POST_COMPUTATION_SUFFIX),
                                                new Column("phaseAngleClock3" + POST_COMPUTATION_SUFFIX),
                                                new Column("ratedU0" + POST_COMPUTATION_SUFFIX),
                                                new Column(CONNECTED + "1" + POST_COMPUTATION_SUFFIX),
                                                new Column(CONNECTED + "2" + POST_COMPUTATION_SUFFIX),
                                                new Column(CONNECTED + "3" + POST_COMPUTATION_SUFFIX),
                                                new Column(MAIN_COMPONENT + "1" + POST_COMPUTATION_SUFFIX),
                                                new Column(MAIN_COMPONENT + "2" + POST_COMPUTATION_SUFFIX),
                                                new Column(MAIN_COMPONENT + "3" + POST_COMPUTATION_SUFFIX),
                                                new Column(VALIDATION + POST_COMPUTATION_SUFFIX));
            }
        }
        return twt3wColumns;
    }

    @Override
    protected void write(String branchId, double p1, double p1Calc, double q1, double q1Calc, double p2, double p2Calc, double q2, double q2Calc,
                         double r, double x, double g1, double g2, double b1, double b2, double rho1, double rho2, double alpha1, double alpha2,
                         double u1, double u2, double theta1, double theta2, double z, double y, double ksi, int phaseAngleClock, boolean connected1, boolean connected2,
                         boolean mainComponent1, boolean mainComponent2, boolean validated, FlowData flowData, boolean found, boolean writeValues) throws IOException {
        formatter.writeCell(branchId);
        if (compareResults) {
            formatter = found ?
                        write(found, flowData.p1, flowData.p1Calc, flowData.q1, flowData.q1Calc, flowData.p2, flowData.p2Calc, flowData.q2, flowData.q2Calc,
                              flowData.r, flowData.x, flowData.g1, flowData.g2, flowData.b1, flowData.b2, flowData.rho1, flowData.rho2, flowData.alpha1, flowData.alpha2,
                              flowData.u1, flowData.u2, flowData.theta1, flowData.theta2, flowData.z, flowData.y, flowData.ksi, flowData.phaseAngleClock, flowData.connected1, flowData.connected2,
                              flowData.mainComponent1, flowData.mainComponent2, flowData.validated) :
                        write(found, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN,
                              Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN,
                              Double.NaN, Double.NaN, Double.NaN, Double.NaN, 0, false, false, false, false, false);
        }
        formatter = write(writeValues, p1, p1Calc, q1, q1Calc, p2, p2Calc, q2, q2Calc, r, x, g1, g2, b1, b2, rho1, rho2, alpha1, alpha2,
                          u1, u2, theta1, theta2, z, y, ksi, phaseAngleClock, connected1, connected2, mainComponent1, mainComponent2, validated);
    }

    private TableFormatter write(boolean writeValues, double p1, double p1Calc, double q1, double q1Calc, double p2, double p2Calc, double q2, double q2Calc,
                                 double r, double x, double g1, double g2, double b1, double b2, double rho1, double rho2, double alpha1, double alpha2,
                                 double u1, double u2, double theta1, double theta2, double z, double y, double ksi, int phaseAngleClock, boolean connected1, boolean connected2,
                                 boolean mainComponent1, boolean mainComponent2, boolean validated) throws IOException {
        formatter = writeValues ?
                    formatter.writeCell(p1)
                             .writeCell(p1Calc)
                             .writeCell(q1)
                             .writeCell(q1Calc)
                             .writeCell(p2)
                             .writeCell(p2Calc)
                             .writeCell(q2)
                             .writeCell(q2Calc) :
                    formatter.writeEmptyCells(8);
        if (verbose) {
            formatter = writeValues ?
                        formatter.writeCell(r)
                                 .writeCell(x)
                                 .writeCell(g1)
                                 .writeCell(g2)
                                 .writeCell(b1)
                                 .writeCell(b2)
                                 .writeCell(rho1)
                                 .writeCell(rho2)
                                 .writeCell(alpha1)
                                 .writeCell(alpha2)
                                 .writeCell(u1)
                                 .writeCell(u2)
                                 .writeCell(theta1)
                                 .writeCell(theta2)
                                 .writeCell(z)
                                 .writeCell(y)
                                 .writeCell(ksi)
                                 .writeCell(phaseAngleClock)
                                 .writeCell(connected1)
                                 .writeCell(connected2)
                                 .writeCell(mainComponent1)
                                 .writeCell(mainComponent2)
                                 .writeCell(getValidated(validated)) :
                        formatter.writeEmptyCells(23);
        }
        return formatter;
    }

    @Override
    protected void write(String generatorId, double p, double q, double v, double targetP, double targetQ, double targetV, double expectedP,
                         boolean connected, boolean voltageRegulatorOn, double minP, double maxP, double minQ, double maxQ, boolean mainComponent,
                         boolean validated, GeneratorData generatorData, boolean found, boolean writeValues) throws IOException {
        formatter.writeCell(generatorId);
        if (compareResults) {
            formatter = found ?
                        write(found, generatorData.p, generatorData.q, generatorData.v, generatorData.targetP, generatorData.targetQ, generatorData.targetV,
                              generatorData.expectedP, generatorData.connected, generatorData.voltageRegulatorOn, generatorData.minP, generatorData.maxP, generatorData.minQ,
                              generatorData.maxQ, generatorData.mainComponent, generatorData.validated) :
                        write(found, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, false, false, Double.NaN, Double.NaN, Double.NaN, Double.NaN, false, false);
        }
        formatter = write(writeValues, p, q, v, targetP, targetQ, targetV, expectedP, connected, voltageRegulatorOn, minP, maxP, minQ, maxQ, mainComponent, validated);
    }

    private TableFormatter write(boolean writeValues, double p, double q, double v, double targetP, double targetQ, double targetV, double expectedP, boolean connected,
                                 boolean voltageRegulatorOn, double minP, double maxP, double minQ, double maxQ, boolean mainComponent, boolean validated) throws IOException {
        formatter = writeValues ?
                    formatter.writeCell(-p)
                             .writeCell(-q)
                             .writeCell(v)
                             .writeCell(targetP)
                             .writeCell(targetQ)
                             .writeCell(targetV)
                             .writeCell(expectedP) :
                    formatter.writeEmptyCells(7);
        if (verbose) {
            formatter = writeValues ?
                        formatter.writeCell(connected)
                                 .writeCell(voltageRegulatorOn)
                                 .writeCell(minP)
                                 .writeCell(maxP)
                                 .writeCell(minQ)
                                 .writeCell(maxQ)
                                 .writeCell(mainComponent)
                                 .writeCell(getValidated(validated)) :
                        formatter.writeEmptyCells(8);
        }
        return formatter;
    }

    private TableFormatter write(boolean writeValues, double incomingP, double incomingQ, double loadP, double loadQ, double genP, double genQ,
                                 double shuntP, double shuntQ, double svcP, double svcQ, double vscCSP, double vscCSQ, double lineP, double lineQ,
                                 double danglingLineP, double danglingLineQ, double twtP, double twtQ, double tltP, double tltQ, boolean mainComponent,
                                 boolean validated) throws IOException {
        formatter = writeValues ?
                    formatter.writeCell(incomingP)
                             .writeCell(incomingQ)
                             .writeCell(loadP)
                             .writeCell(loadQ) :
                    formatter.writeEmptyCells(4);
        if (verbose) {
            formatter = writeValues ?
                        formatter.writeCell(genP)
                                 .writeCell(genQ)
                                 .writeCell(shuntP)
                                 .writeCell(shuntQ)
                                 .writeCell(svcP)
                                 .writeCell(svcQ)
                                 .writeCell(vscCSP)
                                 .writeCell(vscCSQ)
                                 .writeCell(lineP)
                                 .writeCell(lineQ)
                                 .writeCell(danglingLineP)
                                 .writeCell(danglingLineQ)
                                 .writeCell(twtP)
                                 .writeCell(twtQ)
                                 .writeCell(tltP)
                                 .writeCell(tltQ)
                                 .writeCell(mainComponent)
                                 .writeCell(getValidated(validated)) :
                        formatter.writeEmptyCells(18);
        }
        return formatter;
    }

    @Override
    protected void write(String busId, double incomingP, double incomingQ, double loadP, double loadQ, double genP, double genQ, double batP, double batQ, double shuntP, double shuntQ,
                         double svcP, double svcQ, double vscCSP, double vscCSQ, double lineP, double lineQ, double danglingLineP, double danglingLineQ,
                         double twtP, double twtQ, double tltP, double tltQ, boolean mainComponent, boolean validated, BusData busData, boolean found,
                         boolean writeValues) throws IOException {
        formatter.writeCell(busId);
        if (compareResults) {
            formatter = found ?
                    write(found, busData.incomingP, busData.incomingQ, busData.loadP, busData.loadQ, busData.genP, busData.genQ,
                            busData.shuntP, busData.shuntQ, busData.svcP, busData.svcQ, busData.vscCSP, busData.vscCSQ,
                            busData.lineP, busData.lineQ, busData.danglingLineP, busData.danglingLineQ, busData.twtP, busData.twtQ,
                            busData.tltP, busData.tltQ, busData.mainComponent, busData.validated) :
                    write(found, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN,
                            Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN,
                            Double.NaN, false, false);
        }
        formatter = write(writeValues, incomingP, incomingQ, loadP, loadQ, genP, genQ, shuntP, shuntQ, svcP, svcQ, vscCSP, vscCSQ,
                lineP, lineQ, danglingLineP, danglingLineQ, twtP, twtQ, tltP, tltQ, mainComponent, validated);
    }

    @Override
    protected void write(String svcId, double p, double q, double vControlled, double vController, double nominalVcontroller, double reactivePowerSetpoint, double voltageSetpoint,
                         boolean connected, RegulationMode regulationMode, double bMin, double bMax, boolean mainComponent, boolean validated,
                         SvcData svcData, boolean found, boolean writeValues) throws IOException {
        formatter.writeCell(svcId);
        if (compareResults) {
            formatter = found ?
                        write(found, svcData.p, svcData.q, svcData.vControlled, svcData.vController, svcData.nominalVcontroller, svcData.reactivePowerSetpoint, svcData.voltageSetpoint,
                              svcData.connected, svcData.regulationMode, svcData.bMin, svcData.bMax, svcData.mainComponent, svcData.validated) :
                        write(found, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, false, null, Double.NaN, Double.NaN, false, false);
        }
        formatter = write(writeValues, p, q, vControlled, vController, nominalVcontroller, reactivePowerSetpoint, voltageSetpoint, connected, regulationMode, bMin, bMax, mainComponent, validated);
    }

    private TableFormatter write(boolean writeValues, double p, double q, double vControlled, double vController, double nominalVcontroller, double reactivePowerSetpoint, double voltageSetpoint,
                                 boolean connected, RegulationMode regulationMode, double bMin, double bMax, boolean mainComponent, boolean validated) throws IOException {
        formatter = writeValues ?
                    formatter.writeCell(-p)
                             .writeCell(-q)
                             .writeCell(vControlled)
                             .writeCell(vController)
                             .writeCell(nominalVcontroller)
                             .writeCell(reactivePowerSetpoint)
                             .writeCell(voltageSetpoint) :
                    formatter.writeEmptyCells(7);
        if (verbose) {
            formatter = writeValues ?
                        formatter.writeCell(connected)
                                 .writeCell(regulationMode.name())
                                 .writeCell(bMin)
                                 .writeCell(bMax)
                                 .writeCell(mainComponent)
                                 .writeCell(getValidated(validated)) :
                        formatter.writeEmptyCells(6);
        }
        return formatter;
    }

    protected void write(String shuntId, double q, double expectedQ, double p, int currentSectionCount, int maximumSectionCount,
                         double bPerSection, double v, boolean connected, double qMax, double nominalV, boolean mainComponent,
                         boolean validated, ShuntData shuntData, boolean found, boolean writeValues) throws IOException {
        formatter.writeCell(shuntId);
        if (compareResults) {
            formatter = found ?
                        write(found, shuntData.q, shuntData.expectedQ, shuntData.p, shuntData.currentSectionCount, shuntData.maximumSectionCount,
                              shuntData.bPerSection, shuntData.v, shuntData.connected, shuntData.qMax, shuntData.nominalV, shuntData.mainComponent, shuntData.validated) :
                        write(found, Double.NaN, Double.NaN, Double.NaN, -1, -1, Double.NaN, Double.NaN, false, Double.NaN, Double.NaN, false, false);
        }
        write(writeValues, q, expectedQ, p, currentSectionCount, maximumSectionCount, bPerSection, v, connected, qMax, nominalV, mainComponent, validated);
    }

    private TableFormatter write(boolean writeValues, double q, double expectedQ, double p, int currentSectionCount, int maximumSectionCount,
                                 double bPerSection, double v, boolean connected, double qMax, double nominalV, boolean mainComponent, boolean validated) throws IOException {
        formatter = writeValues ?
                    formatter.writeCell(q)
                             .writeCell(expectedQ) :
                    formatter.writeEmptyCells(2);
        if (verbose) {
            formatter = writeValues ?
                        formatter.writeCell(p)
                                 .writeCell(currentSectionCount)
                                 .writeCell(maximumSectionCount)
                                 .writeCell(bPerSection)
                                 .writeCell(v)
                                 .writeCell(connected)
                                 .writeCell(qMax)
                                 .writeCell(nominalV)
                                 .writeCell(mainComponent)
                                 .writeCell(getValidated(validated)) :
                        formatter.writeEmptyCells(10);
        }
        return formatter;
    }

    @Override
    protected void write(String twtId, double error, double upIncrement, double downIncrement, double rho, double rhoPreviousStep, double rhoNextStep,
                         int tapPosition, int lowTapPosition, int highTapPosition, double targetV, TwoSides regulatedSide, double v, boolean connected,
                         boolean mainComponent, boolean validated, TransformerData twtData, boolean found, boolean writeValues) throws IOException {
        formatter.writeCell(twtId);
        if (compareResults) {
            formatter = found ?
                        write(found, twtData.error, twtData.upIncrement, twtData.downIncrement, twtData.rho, twtData.rhoPreviousStep, twtData.rhoNextStep,
                              twtData.tapPosition, twtData.lowTapPosition, twtData.highTapPosition, twtData.targetV, twtData.regulatedSide, twtData.v,
                              twtData.connected, twtData.mainComponent, twtData.validated) :
                        write(found, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, -1, -1, -1, Double.NaN, TwoSides.ONE, Double.NaN, false, false, false);
        }
        write(writeValues, error, upIncrement, downIncrement, rho, rhoPreviousStep, rhoNextStep, tapPosition, lowTapPosition, highTapPosition, targetV,
              regulatedSide, v, connected, mainComponent, validated);
    }

    private TableFormatter write(boolean writeValues, double error, double upIncrement, double downIncrement, double rho, double rhoPreviousStep, double rhoNextStep,
                                 int tapPosition, int lowTapPosition, int highTapPosition, double targetV, TwoSides regulatedSide, double v, boolean connected,
                                 boolean mainComponent, boolean validated) throws IOException {
        formatter = writeValues ?
                    formatter.writeCell(error)
                             .writeCell(upIncrement)
                             .writeCell(downIncrement) :
                    formatter.writeEmptyCells(3);
        if (verbose) {
            formatter = writeValues ?
                        formatter.writeCell(rho)
                                 .writeCell(rhoPreviousStep)
                                 .writeCell(rhoNextStep)
                                 .writeCell(tapPosition)
                                 .writeCell(lowTapPosition)
                                 .writeCell(highTapPosition)
                                 .writeCell(targetV)
                                 .writeCell(regulatedSide != null ? regulatedSide.name() : invalidString)
                                 .writeCell(v)
                                 .writeCell(connected)
                                 .writeCell(mainComponent)
                                 .writeCell(getValidated(validated)) :
                        formatter.writeEmptyCells(12);
        }
        return formatter;
    }

    @Override
    protected void write(String twtId, Transformer3WData transformer3wData1, Transformer3WData transformer3wData2, boolean found, boolean writeValues) throws IOException {
        formatter.writeCell(twtId);
        if (compareResults) {
            formatter = write(found, transformer3wData2.twtData, transformer3wData2.validated);
        }
        write(writeValues, transformer3wData1.twtData, transformer3wData1.validated);
    }

    private TableFormatter write(boolean writeValues, TwtData twtData, boolean validated) throws IOException {
        formatter = writeValues ?
                    formatter.writeCell(twtData.getP(ThreeSides.ONE))
                             .writeCell(twtData.getComputedP(ThreeSides.ONE))
                             .writeCell(twtData.getQ(ThreeSides.ONE))
                             .writeCell(twtData.getComputedQ(ThreeSides.ONE))
                             .writeCell(twtData.getP(ThreeSides.TWO))
                             .writeCell(twtData.getComputedP(ThreeSides.TWO))
                             .writeCell(twtData.getQ(ThreeSides.TWO))
                             .writeCell(twtData.getComputedQ(ThreeSides.TWO))
                             .writeCell(twtData.getP(ThreeSides.THREE))
                             .writeCell(twtData.getComputedP(ThreeSides.THREE))
                             .writeCell(twtData.getQ(ThreeSides.THREE))
                             .writeCell(twtData.getComputedQ(ThreeSides.THREE)) :
                    formatter.writeEmptyCells(12);
        if (verbose) {
            formatter = writeValues ?
                        formatter.writeCell(twtData.getU(ThreeSides.ONE))
                                 .writeCell(twtData.getU(ThreeSides.TWO))
                                 .writeCell(twtData.getU(ThreeSides.THREE))
                                 .writeCell(twtData.getStarU())
                                 .writeCell(twtData.getTheta(ThreeSides.ONE))
                                 .writeCell(twtData.getTheta(ThreeSides.TWO))
                                 .writeCell(twtData.getTheta(ThreeSides.THREE))
                                 .writeCell(twtData.getStarTheta())
                                 .writeCell(twtData.getG1(ThreeSides.ONE))
                                 .writeCell(twtData.getB1(ThreeSides.ONE))
                                 .writeCell(twtData.getG2(ThreeSides.ONE))
                                 .writeCell(twtData.getB2(ThreeSides.ONE))
                                 .writeCell(twtData.getG1(ThreeSides.TWO))
                                 .writeCell(twtData.getB1(ThreeSides.TWO))
                                 .writeCell(twtData.getG2(ThreeSides.TWO))
                                 .writeCell(twtData.getB2(ThreeSides.TWO))
                                 .writeCell(twtData.getG1(ThreeSides.THREE))
                                 .writeCell(twtData.getB1(ThreeSides.THREE))
                                 .writeCell(twtData.getG2(ThreeSides.THREE))
                                 .writeCell(twtData.getB2(ThreeSides.THREE))
                                 .writeCell(twtData.getR(ThreeSides.ONE))
                                 .writeCell(twtData.getR(ThreeSides.TWO))
                                 .writeCell(twtData.getR(ThreeSides.THREE))
                                 .writeCell(twtData.getX(ThreeSides.ONE))
                                 .writeCell(twtData.getX(ThreeSides.TWO))
                                 .writeCell(twtData.getX(ThreeSides.THREE))
                                 .writeCell(twtData.getRatedU(ThreeSides.ONE))
                                 .writeCell(twtData.getRatedU(ThreeSides.TWO))
                                 .writeCell(twtData.getRatedU(ThreeSides.THREE))
                                 .writeCell(twtData.getPhaseAngleClock2())
                                 .writeCell(twtData.getPhaseAngleClock3())
                                 .writeCell(twtData.getRatedU0())
                                 .writeCell(twtData.isConnected(ThreeSides.ONE))
                                 .writeCell(twtData.isConnected(ThreeSides.TWO))
                                 .writeCell(twtData.isConnected(ThreeSides.THREE))
                                 .writeCell(twtData.isMainComponent(ThreeSides.ONE))
                                 .writeCell(twtData.isMainComponent(ThreeSides.TWO))
                                 .writeCell(twtData.isMainComponent(ThreeSides.THREE))
                                 .writeCell(getValidated(validated)) :
                        formatter.writeEmptyCells(39);
        }
        return formatter;
    }

}
