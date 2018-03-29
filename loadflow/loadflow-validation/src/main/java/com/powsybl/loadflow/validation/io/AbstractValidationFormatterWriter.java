/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow.validation.io;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.commons.io.table.Column;
import com.powsybl.commons.io.table.TableFormatter;
import com.powsybl.commons.io.table.TableFormatterConfig;
import com.powsybl.commons.io.table.TableFormatterFactory;
import com.powsybl.iidm.network.StaticVarCompensator.RegulationMode;
import com.powsybl.iidm.network.TwoTerminalsConnectable.Side;
import com.powsybl.loadflow.validation.ValidationType;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.it>
 */
public abstract class AbstractValidationFormatterWriter implements ValidationWriter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractValidationFormatterWriter.class);

    protected static final String SUCCESS = "success";
    protected static final String FAIL = "fail";
    protected static final String VALIDATION = "validation";
    protected static final String CONNECTED = "connected";
    protected static final String POST_LF_SUFFIX = "_postLF";

    protected ValidationType validationType;
    protected boolean compareResults;
    protected TableFormatter formatter;
    protected boolean preLoadflowValidationCompleted = false;
    protected Map<String, BusData> busesData = new HashMap<>();
    protected Map<String, GeneratorData> generatorsData = new HashMap<>();
    protected Map<String, SvcData> svcsData = new HashMap<>();
    protected Map<String, ShuntData> shuntsData = new HashMap<>();
    protected Map<String, FlowData> flowsData = new HashMap<>();
    protected Map<String, TransformerData> twtsData = new HashMap<>();

    protected TableFormatter createTableFormatter(String id, Class<? extends TableFormatterFactory> formatterFactoryClass,
                                                  TableFormatterConfig formatterConfig, Writer writer, ValidationType validationType) {
        try {
            TableFormatterFactory factory = formatterFactoryClass.newInstance();
            return factory.create(writer, id + " " + validationType + " check", formatterConfig, getColumns());
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }
    }

    protected abstract Column[] getColumns();

    protected String getValidated(boolean validated) {
        return validated ? SUCCESS : FAIL;
    }

    @Override
    public void write(String branchId, double p1, double p1Calc, double q1, double q1Calc, double p2, double p2Calc, double q2, double q2Calc,
                      double r, double x, double g1, double g2, double b1, double b2, double rho1, double rho2, double alpha1, double alpha2,
                      double u1, double u2, double theta1, double theta2, double z, double y, double ksi, boolean connected1, boolean connected2,
                      boolean mainComponent1, boolean mainComponent2, boolean validated) throws IOException {
        Objects.requireNonNull(branchId);
        FlowData emptyFlowData = new FlowData(branchId, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN,
                                              Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN,
                                              Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, false, false, false, false, false);
        if (compareResults) {
            if (preLoadflowValidationCompleted) {
                boolean found = flowsData.containsKey(branchId);
                FlowData flowData = found ? flowsData.get(branchId) : emptyFlowData;
                write(branchId, p1, p1Calc, q1, q1Calc, p2, p2Calc, q2, q2Calc, r, x, g1, g2, b1, b2, rho1, rho2, alpha1, alpha2, u1, u2,
                      theta1, theta2, z, y, ksi, connected1, connected2, mainComponent1, mainComponent2, validated, flowData, found, true);
                flowsData.remove(branchId);
            } else {
                flowsData.put(branchId, new FlowData(branchId, p1, p1Calc, q1, q1Calc, p2, p2Calc, q2, q2Calc, r, x, g1, g2, b1, b2, rho1, rho2, alpha1, alpha2,
                                                     u1, u2, theta1, theta2, z, y, ksi, connected1, connected2, mainComponent1, mainComponent2, validated));
            }
        } else {
            write(branchId, p1, p1Calc, q1, q1Calc, p2, p2Calc, q2, q2Calc, r, x, g1, g2, b1, b2, rho1, rho2, alpha1, alpha2, u1, u2,
                  theta1, theta2, z, y, ksi, connected1, connected2, mainComponent1, mainComponent2, validated, emptyFlowData, false, true);
        }
    }

    protected abstract void write(String branchId, double p1, double p1Calc, double q1, double q1Calc, double p2, double p2Calc, double q2, double q2Calc,
                                  double r, double x, double g1, double g2, double b1, double b2, double rho1, double rho2, double alpha1, double alpha2,
                                  double u1, double u2, double theta1, double theta2, double z, double y, double ksi, boolean connected1, boolean connected2,
                                  boolean mainComponent1, boolean mainComponent2, boolean validated, FlowData flowData, boolean found, boolean writeValues) throws IOException;

    @Override
    public void write(String generatorId, float p, float q, float v, float targetP, float targetQ, float targetV,
                      boolean connected, boolean voltageRegulatorOn, float minQ, float maxQ, boolean validated) throws IOException {
        Objects.requireNonNull(generatorId);
        GeneratorData emptyGeneratorData = new GeneratorData(generatorId, Float.NaN, Float.NaN, Float.NaN, Float.NaN, Float.NaN,
                                                             Float.NaN, false, false, Float.NaN, Float.NaN, false);
        if (compareResults) {
            if (preLoadflowValidationCompleted) {
                boolean found = generatorsData.containsKey(generatorId);
                GeneratorData generatorData = found ? generatorsData.get(generatorId) : emptyGeneratorData;
                write(generatorId, p, q, v, targetP, targetQ, targetV, connected, voltageRegulatorOn,
                      minQ, maxQ, validated, generatorData, found, true);
                generatorsData.remove(generatorId);
            } else {
                generatorsData.put(generatorId, new GeneratorData(generatorId, p, q, v, targetP, targetQ, targetV,
                                                                  connected, voltageRegulatorOn, minQ, maxQ, validated));
            }
        } else {
            write(generatorId, p, q, v, targetP, targetQ, targetV, connected, voltageRegulatorOn,
                  minQ, maxQ, validated, emptyGeneratorData, false, true);
        }
    }

    protected abstract void write(String generatorId, float p, float q, float v, float targetP, float targetQ, float targetV,
                                  boolean connected, boolean voltageRegulatorOn, float minQ, float maxQ, boolean validated,
                                  GeneratorData generatorData, boolean found, boolean writeValues) throws IOException;

    @Override
    public void write(String busId, double incomingP, double incomingQ, double loadP, double loadQ, double genP, double genQ,
                      double shuntP, double shuntQ, double svcP, double svcQ, double vscCSP, double vscCSQ, double lineP, double lineQ,
                      double danglingLineP, double danglingLineQ, double twtP, double twtQ, double tltP, double tltQ, boolean validated) throws IOException {
        Objects.requireNonNull(busId);
        BusData emptyBusData = new BusData(busId, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN,
                                           Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN,
                                           Double.NaN, Double.NaN, false);
        if (compareResults) {
            if (preLoadflowValidationCompleted) {
                boolean found = busesData.containsKey(busId);
                BusData busData = found ? busesData.get(busId) : emptyBusData;
                write(busId, incomingP, incomingQ, loadP, loadQ, genP, genQ, shuntP, shuntQ, svcP, svcQ, vscCSP, vscCSQ,
                      lineP, lineQ, danglingLineP, danglingLineQ, twtP, twtQ, tltP, tltQ, validated, busData, found, true);
                busesData.remove(busId);
            } else {
                busesData.put(busId, new BusData(busId, incomingP, incomingQ, loadP, loadQ, genP, genQ, shuntP, shuntQ, svcP, svcQ, vscCSP, vscCSQ,
                                                 lineP, lineQ, danglingLineP, danglingLineQ, twtP, twtQ, tltP, tltQ, validated));
            }
        } else {
            write(busId, incomingP, incomingQ, loadP, loadQ, genP, genQ, shuntP, shuntQ, svcP, svcQ,
                  vscCSP, vscCSQ, lineP, lineQ, danglingLineP, danglingLineQ, twtP, twtQ, tltP, tltQ, validated, emptyBusData, false, true);
        }
    }

    protected abstract void write(String busId, double incomingP, double incomingQ, double loadP, double loadQ, double genP, double genQ,
                                  double shuntP, double shuntQ, double svcP, double svcQ, double vscCSP, double vscCSQ, double lineP, double lineQ,
                                  double danglingLineP, double danglingLineQ, double twtP, double twtQ, double tltP, double tltQ, boolean validated,
                                  BusData busData, boolean found, boolean writeValues) throws IOException;

    @Override
    public void write(String svcId, float p, float q, float v, float reactivePowerSetpoint, float voltageSetpoint,
                      boolean connected, RegulationMode regulationMode, float bMin, float bMax, boolean validated) throws IOException {
        Objects.requireNonNull(svcId);
        SvcData emptySvcData = new SvcData(svcId, Float.NaN, Float.NaN, Float.NaN, Float.NaN, Float.NaN, false, RegulationMode.OFF, Float.NaN, Float.NaN, false);
        if (compareResults) {
            if (preLoadflowValidationCompleted) {
                boolean found = svcsData.containsKey(svcId);
                SvcData svcData = found ? svcsData.get(svcId) : emptySvcData;
                write(svcId, p, q, v, reactivePowerSetpoint, voltageSetpoint, connected, regulationMode, bMin, bMax, validated, svcData, found, true);
                svcsData.remove(svcId);
            } else {
                svcsData.put(svcId, new SvcData(svcId, p, q, v, reactivePowerSetpoint, voltageSetpoint, connected, regulationMode, bMin, bMax, validated));
            }
        } else {
            write(svcId, p, q, v, reactivePowerSetpoint, voltageSetpoint, connected, regulationMode, bMin, bMax, validated, emptySvcData, false, true);
        }
    }

    protected abstract void write(String svcId, float p, float q, float v, float reactivePowerSetpoint, float voltageSetpoint,
                                 boolean connected, RegulationMode regulationMode, float bMin, float bMax, boolean validated,
                                 SvcData svcData, boolean found, boolean writeValues) throws IOException;

    @Override
    public void write(String shuntId, float q, float expectedQ, float p, int currentSectionCount, int maximumSectionCount,
                      float bPerSection, float v, boolean connected, float qMax, float nominalV, boolean validated) throws IOException {
        Objects.requireNonNull(shuntId);
        ShuntData emptyShuntData = new ShuntData(shuntId, Float.NaN, Float.NaN, Float.NaN, -1, -1, Float.NaN, Float.NaN, false, Float.NaN, Float.NaN, false);
        if (compareResults) {
            if (preLoadflowValidationCompleted) {
                boolean found = shuntsData.containsKey(shuntId);
                ShuntData shuntData = found ? shuntsData.get(shuntId) : emptyShuntData;
                write(shuntId, q, expectedQ, p, currentSectionCount, maximumSectionCount,
                      bPerSection, v, connected, qMax, nominalV, validated, shuntData, found, true);
                shuntsData.remove(shuntId);
            } else {
                shuntsData.put(shuntId, new ShuntData(shuntId, q, expectedQ, p, currentSectionCount, maximumSectionCount,
                                                      bPerSection, v, connected, qMax, nominalV, validated));
            }
        } else {
            write(shuntId, q, expectedQ, p, currentSectionCount, maximumSectionCount, bPerSection, v, connected, qMax, nominalV, validated, emptyShuntData, false, true);
        }
    }

    protected abstract void write(String shuntId, float q, float expectedQ, float p, int currentSectionCount, int maximumSectionCount,
                                  float bPerSection, float v, boolean connected, float qMax, float nominalV, boolean validated,
                                  ShuntData shuntData, boolean found, boolean writeValues) throws IOException;

    @Override
    public void write(String twtId, float error, float upIncrement, float downIncrement, float rho, float rhoPreviousStep, float rhoNextStep,
                      int tapPosition, int lowTapPosition, int highTapPosition, float targetV, Side regulatedSide, float v, boolean connected,
                      boolean mainComponent, boolean validated) throws IOException {
        Objects.requireNonNull(twtId);
        TransformerData emptyTwtData = new TransformerData(twtId, Float.NaN, Float.NaN, Float.NaN, Float.NaN, Float.NaN,
                                                           Float.NaN, -1, -1, -1, Float.NaN, Side.ONE, Float.NaN, false, false, false);
        if (compareResults) {
            if (preLoadflowValidationCompleted) {
                boolean found = twtsData.containsKey(twtId);
                TransformerData twtData = found ? twtsData.get(twtId) : emptyTwtData;
                write(twtId, error, upIncrement, downIncrement, rho, rhoPreviousStep, rhoNextStep, tapPosition, lowTapPosition,
                      highTapPosition, targetV, regulatedSide, v, connected, mainComponent, validated, twtData, found, true);
                twtsData.remove(twtId);
            } else {
                twtsData.put(twtId, new TransformerData(twtId, error, upIncrement, downIncrement, rho, rhoPreviousStep, rhoNextStep, tapPosition,
                                                        lowTapPosition, highTapPosition, targetV, regulatedSide, v, connected, mainComponent, validated));
            }
        } else {
            write(twtId, error, upIncrement, downIncrement, rho, rhoPreviousStep, rhoNextStep, tapPosition, lowTapPosition,
                  highTapPosition, targetV, regulatedSide, v, connected, mainComponent, validated, emptyTwtData, false, true);
        }
    }

    protected abstract void write(String twtId, float error, float upIncrement, float downIncrement, float rho, float rhoPreviousStep, float rhoNextStep,
                                  int tapPosition, int lowTapPosition, int highTapPosition, float targetV, Side regulatedSide, float v, boolean connected,
                                  boolean mainComponent, boolean validated, TransformerData twtData, boolean found, boolean writeValues) throws IOException;

    @Override
    public void setValidationCompleted() {
        if (!preLoadflowValidationCompleted) {
            preLoadflowValidationCompleted = true;
            return;
        }
        switch (validationType) {
            case FLOWS:
                writeFlowsData();
                break;
            case GENERATORS:
                writeGeneratorsData();
                break;
            case BUSES:
                writeBusesData();
                break;
            case SVCS:
                writeSvcsData();
                break;
            case SHUNTS:
                writeShuntsData();
                break;
            case TWTS:
                writeTwtsData();
                break;
            default:
                throw new AssertionError("Unexpected ValidationType value: " + validationType);
        }
    }

    private void writeFlowsData() {
        flowsData.values().forEach(flowData -> {
            try {
                write(flowData.branchId, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN,
                      Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN,
                      Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, false, false, false, false, false, flowData, true, false);
            } catch (IOException e) {
                LOGGER.error("Error writing data of branch {}: {}", flowData.branchId, e.getMessage());
            }
        });
    }

    private void writeGeneratorsData() {
        generatorsData.values().forEach(generatorData -> {
            try {
                write(generatorData.generatorId, Float.NaN, Float.NaN, Float.NaN, Float.NaN, Float.NaN,
                      Float.NaN, false, false, Float.NaN, Float.NaN, false, generatorData, true, false);
            } catch (IOException e) {
                LOGGER.error("Error writing data of generator {}: {}", generatorData.generatorId, e.getMessage());
            }
        });
    }

    private void writeBusesData() {
        busesData.values().forEach(busData -> {
            try {
                write(busData.busId, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN,
                      Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN,
                      Double.NaN, Double.NaN, Double.NaN, false, busData, true, false);
            } catch (IOException e) {
                LOGGER.error("Error writing data of bus {}: {}", busData.busId, e.getMessage());
            }
        });
    }

    private void writeSvcsData() {
        svcsData.values().forEach(svcData -> {
            try {
                write(svcData.svcId, Float.NaN, Float.NaN, Float.NaN, Float.NaN, Float.NaN,
                      false, RegulationMode.OFF, Float.NaN, Float.NaN, false, svcData, true, false);
            } catch (IOException e) {
                LOGGER.error("Error writing data of svc {}: {}", svcData.svcId, e.getMessage());
            }
        });
    }

    private void writeShuntsData() {
        shuntsData.values().forEach(shuntData -> {
            try {
                write(shuntData.shuntId, Float.NaN, Float.NaN, Float.NaN, -1, -1, Float.NaN,
                      Float.NaN, false, Float.NaN, Float.NaN, false, shuntData, true, false);
            } catch (IOException e) {
                LOGGER.error("Error writing data of shunt {}: {}", shuntData.shuntId, e.getMessage());
            }
        });
    }

    private void writeTwtsData() {
        twtsData.values().forEach(twtData -> {
            try {
                write(twtData.twtId, Float.NaN, Float.NaN, Float.NaN, Float.NaN, Float.NaN, Float.NaN,
                      -1, -1, -1, Float.NaN, Side.ONE, Float.NaN, false, false, false, twtData, true, false);
            } catch (IOException e) {
                LOGGER.error("Error writing data of twt {}: {}", twtData.twtId, e.getMessage());
            }
        });
    }

    @Override
    public void close() throws IOException {
        formatter.close();
    }

    class FlowData {

        final String branchId;
        final double p1;
        final double p1Calc;
        final double q1;
        final double q1Calc;
        final double p2;
        final double p2Calc;
        final double q2;
        final double q2Calc;
        final double r;
        final double x;
        final double g1;
        final double g2;
        final double b1;
        final double b2;
        final double rho1;
        final double rho2;
        final double alpha1;
        final double alpha2;
        final double u1;
        final double u2;
        final double theta1;
        final double theta2;
        final double z;
        final double y;
        final double ksi;
        final boolean connected1;
        final boolean connected2;
        final boolean mainComponent1;
        final boolean mainComponent2;
        final boolean validated;

        FlowData(String branchId, double p1, double p1Calc, double q1, double q1Calc, double p2, double p2Calc, double q2, double q2Calc,
                 double r, double x, double g1, double g2, double b1, double b2, double rho1, double rho2, double alpha1, double alpha2,
                 double u1, double u2, double theta1, double theta2, double z, double y, double ksi, boolean connected1, boolean connected2,
                 boolean mainComponent1, boolean mainComponent2, boolean validated) {
            this.branchId = Objects.requireNonNull(branchId);
            this.p1 = p1;
            this.p1Calc = p1Calc;
            this.q1 = q1;
            this.q1Calc = q1Calc;
            this.p2 = p2;
            this.p2Calc = p2Calc;
            this.q2 = q2;
            this.q2Calc = q2Calc;
            this.r = r;
            this.x = x;
            this.g1 = g1;
            this.g2 = g2;
            this.b1 = b1;
            this.b2 = b2;
            this.rho1 = rho1;
            this.rho2 = rho2;
            this.alpha1 = alpha1;
            this.alpha2 = alpha2;
            this.u1 = u1;
            this.u2 = u2;
            this.theta1 = theta1;
            this.theta2 = theta2;
            this.z = z;
            this.y = y;
            this.ksi = ksi;
            this.connected1 = connected1;
            this.connected2 = connected2;
            this.mainComponent1 = mainComponent1;
            this.mainComponent2 = mainComponent2;
            this.validated = validated;
        }

    }

    class GeneratorData {
        final String generatorId;
        final float p;
        final float q;
        final float v;
        final float targetP;
        final float targetQ;
        final float targetV;
        final boolean connected;
        final boolean voltageRegulatorOn;
        final float minQ;
        final float maxQ;
        final boolean validated;

        GeneratorData(String generatorId, float p, float q, float v, float targetP, float targetQ, float targetV,
                      boolean connected, boolean voltageRegulatorOn, float minQ, float maxQ, boolean validated) {
            this.generatorId = Objects.requireNonNull(generatorId);
            this.p = p;
            this.q = q;
            this.v = v;
            this.targetP = targetP;
            this.targetQ = targetQ;
            this.targetV = targetV;
            this.connected = connected;
            this.voltageRegulatorOn = voltageRegulatorOn;
            this.minQ = minQ;
            this.maxQ = maxQ;
            this.validated = validated;
        }

    }

    class BusData {

        final String busId;
        final double incomingP;
        final double incomingQ;
        final double loadP;
        final double loadQ;
        final double genP;
        final double genQ;
        final double shuntP;
        final double shuntQ;
        final double svcP;
        final double svcQ;
        final double vscCSP;
        final double vscCSQ;
        final double lineP;
        final double lineQ;
        final double danglingLineP;
        final double danglingLineQ;
        final double twtP;
        final double twtQ;
        final double tltP;
        final double tltQ;
        final boolean validated;

        BusData(String busId, double incomingP, double incomingQ, double loadP, double loadQ, double genP, double genQ,
                double shuntP, double shuntQ, double svcP, double svcQ, double vscCSP, double vscCSQ, double lineP, double lineQ,
                double danglingLineP, double danglingLineQ, double twtP, double twtQ, double tltP, double tltQ, boolean validated) {
            this.busId = Objects.requireNonNull(busId);
            this.incomingP = incomingP;
            this.incomingQ = incomingQ;
            this.loadP = loadP;
            this.loadQ = loadQ;
            this.genP = genP;
            this.genQ = genQ;
            this.shuntP = shuntP;
            this.shuntQ = shuntQ;
            this.svcP = svcP;
            this.svcQ = svcQ;
            this.vscCSP = vscCSP;
            this.vscCSQ = vscCSQ;
            this.lineP = lineP;
            this.lineQ = lineQ;
            this.danglingLineP = danglingLineP;
            this.danglingLineQ = danglingLineQ;
            this.twtP = twtP;
            this.twtQ = twtQ;
            this.tltP = tltP;
            this.tltQ = tltQ;
            this.validated = validated;
        }

    }

    class SvcData {

        final String svcId;
        final float p;
        final float q;
        final float v;
        final float reactivePowerSetpoint;
        final float voltageSetpoint;
        final boolean connected;
        final RegulationMode regulationMode;
        final float bMin;
        final float bMax;
        final boolean validated;

        SvcData(String svcId, float p, float q, float v, float reactivePowerSetpoint, float voltageSetpoint,
                boolean connected, RegulationMode regulationMode, float bMin, float bMax, boolean validated) {
            this.svcId = Objects.requireNonNull(svcId);
            this.p = p;
            this.q = q;
            this.v = v;
            this.reactivePowerSetpoint = reactivePowerSetpoint;
            this.voltageSetpoint = voltageSetpoint;
            this.connected = connected;
            this.regulationMode = Objects.requireNonNull(regulationMode);
            this.bMin = bMin;
            this.bMax = bMax;
            this.validated = validated;
        }

    }

    class ShuntData {

        final String shuntId;
        final float q;
        final float expectedQ;
        final float p;
        final int currentSectionCount;
        final int maximumSectionCount;
        final float bPerSection;
        final float v;
        final boolean connected;
        final float qMax;
        final float nominalV;
        final boolean validated;

        ShuntData(String shuntId, float q, float expectedQ, float p, int currentSectionCount, int maximumSectionCount,
                  float bPerSection, float v, boolean connected, float qMax, float nominalV, boolean validated) {
            this.shuntId = Objects.requireNonNull(shuntId);
            this.q = q;
            this.expectedQ = expectedQ;
            this.p = p;
            this.currentSectionCount = currentSectionCount;
            this.maximumSectionCount = maximumSectionCount;
            this.bPerSection = bPerSection;
            this.v = v;
            this.connected = connected;
            this.qMax = qMax;
            this.nominalV = nominalV;
            this.validated = validated;
        }

    }

    class TransformerData {
        final String twtId;
        final float error;
        final float upIncrement;
        final float downIncrement;
        final float rho;
        final float rhoPreviousStep;
        final float rhoNextStep;
        final int tapPosition;
        final int lowTapPosition;
        final int highTapPosition;
        final float targetV;
        final Side regulatedSide;
        final float v;
        final boolean connected;
        final boolean mainComponent;
        final boolean validated;

        TransformerData(String twtId, float error, float upIncrement, float downIncrement, float rho, float rhoPreviousStep,
                        float rhoNextStep, int tapPosition, int lowTapPosition, int highTapPosition, float targetV, Side regulatedSide,
                        float v, boolean connected, boolean mainComponent, boolean validated) {
            this.twtId = twtId;
            this.error = error;
            this.upIncrement = upIncrement;
            this.downIncrement = downIncrement;
            this.rho = rho;
            this.rhoPreviousStep = rhoPreviousStep;
            this.rhoNextStep = rhoNextStep;
            this.tapPosition = tapPosition;
            this.lowTapPosition = lowTapPosition;
            this.highTapPosition = highTapPosition;
            this.targetV = targetV;
            this.regulatedSide = Objects.requireNonNull(regulatedSide);
            this.v = v;
            this.connected = connected;
            this.mainComponent = mainComponent;
            this.validated = validated;
        }

    }

}
