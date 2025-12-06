/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.loadflow.validation.io;

import com.powsybl.commons.io.table.Column;
import com.powsybl.commons.io.table.TableFormatter;
import com.powsybl.commons.io.table.TableFormatterConfig;
import com.powsybl.commons.io.table.TableFormatterFactory;
import com.powsybl.iidm.network.StaticVarCompensator.RegulationMode;
import com.powsybl.iidm.network.TwoSides;
import com.powsybl.iidm.network.util.TwtData;
import com.powsybl.loadflow.validation.ValidationType;
import com.powsybl.loadflow.validation.data.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.it>}
 */
public abstract class AbstractValidationFormatterWriter implements ValidationWriter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractValidationFormatterWriter.class);

    protected static final String SUCCESS = "success";
    protected static final String FAIL = "fail";
    protected static final String VALIDATION = "validation";
    protected static final String CONNECTED = "connected";
    protected static final String MAIN_COMPONENT = "mainComponent";
    protected static final String POST_COMPUTATION_SUFFIX = "_postComp";
    protected static final String NOMINAL_V = "nominalV";
    protected static final String NETWORK_P1 = "network_p1";
    protected static final String EXPECTED_P1 = "expected_p1";
    protected static final String NETWORK_Q1 = "network_q1";
    protected static final String EXPECTED_Q1 = "expected_q1";
    protected static final String NETWORK_P2 = "network_p2";
    protected static final String EXPECTED_P2 = "expected_p2";
    protected static final String NETWORK_Q2 = "network_q2";
    protected static final String EXPECTED_Q2 = "expected_q2";
    protected static final String NETWORK_P3 = "network_p3";
    protected static final String EXPECTED_P3 = "expected_p3";
    protected static final String NETWORK_Q3 = "network_q3";
    protected static final String EXPECTED_Q3 = "expected_q3";
    protected static final String THETA1 = "theta1";
    protected static final String THETA2 = "theta2";
    protected static final String THETA3 = "theta3";

    protected ValidationType validationType;
    protected boolean compareResults;
    protected TableFormatter formatter;
    protected String invalidString;
    protected boolean preLoadflowValidationCompleted = false;
    protected Map<String, ValidatedBusData> busesData = new HashMap<>();
    protected Map<String, ValidatedGeneratorData> generatorsData = new HashMap<>();
    protected Map<String, ValidatedSvcData> svcsData = new HashMap<>();
    protected Map<String, ValidatedShuntData> shuntsData = new HashMap<>();
    protected Map<String, ValidatedFlowData> flowsData = new HashMap<>();
    protected Map<String, ValidatedTransformerData> twtsData = new HashMap<>();
    protected Map<String, ValidatedTransformer3WData> twts3wData = new HashMap<>();

    protected TableFormatter createTableFormatter(String id, Class<? extends TableFormatterFactory> formatterFactoryClass,
                                                  TableFormatterConfig formatterConfig, Writer writer, ValidationType validationType) {
        try {
            TableFormatterFactory factory = formatterFactoryClass.getDeclaredConstructor().newInstance();
            return factory.create(writer, id + " " + validationType + " check", formatterConfig, getColumns());
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
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
                      double u1, double u2, double theta1, double theta2, double z, double y, double ksi, int phaseAngleClock, boolean connected1, boolean connected2,
                      boolean mainComponent1, boolean mainComponent2, boolean validated) throws IOException {
        Objects.requireNonNull(branchId);
        ValidatedFlowData emptyValidatedFlowData = new ValidatedFlowData(branchId, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN,
                Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN,
                Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, 0, false, false, false, false, false);
        if (compareResults) {
            if (preLoadflowValidationCompleted) {
                boolean found = flowsData.containsKey(branchId);
                ValidatedFlowData validatedFlowData = found ? flowsData.get(branchId) : emptyValidatedFlowData;
                write(branchId, p1, p1Calc, q1, q1Calc, p2, p2Calc, q2, q2Calc, r, x, g1, g2, b1, b2, rho1, rho2, alpha1, alpha2, u1, u2,
                        theta1, theta2, z, y, ksi, phaseAngleClock, connected1, connected2, mainComponent1, mainComponent2, validated, validatedFlowData, found, true);
                flowsData.remove(branchId);
            } else {
                flowsData.put(branchId, new ValidatedFlowData(branchId, p1, p1Calc, q1, q1Calc, p2, p2Calc, q2, q2Calc, r, x, g1, g2, b1, b2, rho1, rho2, alpha1, alpha2,
                        u1, u2, theta1, theta2, z, y, ksi, phaseAngleClock, connected1, connected2, mainComponent1, mainComponent2, validated));
            }
        } else {
            write(branchId, p1, p1Calc, q1, q1Calc, p2, p2Calc, q2, q2Calc, r, x, g1, g2, b1, b2, rho1, rho2, alpha1, alpha2, u1, u2,
                    theta1, theta2, z, y, ksi, phaseAngleClock, connected1, connected2, mainComponent1, mainComponent2, validated, emptyValidatedFlowData, false, true);
        }
    }

    protected abstract void write(String branchId, double p1, double p1Calc, double q1, double q1Calc, double p2, double p2Calc, double q2, double q2Calc,
                                  double r, double x, double g1, double g2, double b1, double b2, double rho1, double rho2, double alpha1, double alpha2,
                                  double u1, double u2, double theta1, double theta2, double z, double y, double ksi, int phaseAngleClock, boolean connected1, boolean connected2,
                                  boolean mainComponent1, boolean mainComponent2, boolean validated, ValidatedFlowData validatedFlowData, boolean found, boolean writeValues) throws IOException;

    @Override
    public void write(String generatorId, double p, double q, double v, double targetP, double targetQ, double targetV, double expectedP, boolean connected,
                      boolean voltageRegulatorOn, double minP, double maxP, double minQ, double maxQ, boolean mainComponent, boolean validated) throws IOException {
        Objects.requireNonNull(generatorId);
        ValidatedGeneratorData emptyValidatedGeneratorData = new ValidatedGeneratorData(generatorId, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN,
                Double.NaN, false, false, Double.NaN, Double.NaN, Double.NaN, Double.NaN, false, false);
        if (compareResults) {
            if (preLoadflowValidationCompleted) {
                boolean found = generatorsData.containsKey(generatorId);
                ValidatedGeneratorData validatedGeneratorData = found ? generatorsData.get(generatorId) : emptyValidatedGeneratorData;
                write(generatorId, p, q, v, targetP, targetQ, targetV, expectedP, connected, voltageRegulatorOn,
                        minP, maxP, minQ, maxQ, mainComponent, validated, validatedGeneratorData, found, true);
                generatorsData.remove(generatorId);
            } else {
                generatorsData.put(generatorId, new ValidatedGeneratorData(generatorId, p, q, v, targetP, targetQ, targetV, expectedP, connected,
                        voltageRegulatorOn, minP, maxP, minQ, maxQ, mainComponent, validated));
            }
        } else {
            write(generatorId, p, q, v, targetP, targetQ, targetV, expectedP, connected, voltageRegulatorOn,
                    minP, maxP, minQ, maxQ, mainComponent, validated, emptyValidatedGeneratorData, false, true);
        }
    }

    protected abstract void write(String generatorId, double p, double q, double v, double targetP, double targetQ, double targetV, double expectedP,
                                  boolean connected, boolean voltageRegulatorOn, double minP, double maxP, double minQ, double maxQ, boolean mainComponent,
                                  boolean validated, ValidatedGeneratorData validatedGeneratorData, boolean found, boolean writeValues) throws IOException;

    @Override
    public void write(String busId, double incomingP, double incomingQ, double loadP, double loadQ, double genP, double genQ, double batP, double batQ,
                      double shuntP, double shuntQ, double svcP, double svcQ, double vscCSP, double vscCSQ, double lineP, double lineQ,
                      double danglingLineP, double danglingLineQ, double twtP, double twtQ, double tltP, double tltQ, boolean mainComponent,
                      boolean validated) throws IOException {
        Objects.requireNonNull(busId);
        ValidatedBusData emptyValidatedBusData = new ValidatedBusData(busId, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN,
                Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN,
                Double.NaN, Double.NaN, false, false);
        if (compareResults) {
            if (preLoadflowValidationCompleted) {
                boolean found = busesData.containsKey(busId);
                ValidatedBusData validatedBusData = found ? busesData.get(busId) : emptyValidatedBusData;
                write(busId, incomingP, incomingQ, loadP, loadQ, genP, genQ, batP, batQ, shuntP, shuntQ, svcP, svcQ, vscCSP, vscCSQ,
                        lineP, lineQ, danglingLineP, danglingLineQ, twtP, twtQ, tltP, tltQ, mainComponent, validated, validatedBusData, found, true);
                busesData.remove(busId);
            } else {
                busesData.put(busId, new ValidatedBusData(busId, incomingP, incomingQ, loadP, loadQ, genP, genQ, batP, batQ, shuntP, shuntQ, svcP, svcQ, vscCSP, vscCSQ,
                        lineP, lineQ, danglingLineP, danglingLineQ, twtP, twtQ, tltP, tltQ, mainComponent, validated));
            }
        } else {
            write(busId, incomingP, incomingQ, loadP, loadQ, genP, genQ, batP, batQ, shuntP, shuntQ, svcP, svcQ, vscCSP, vscCSQ, lineP, lineQ,
                    danglingLineP, danglingLineQ, twtP, twtQ, tltP, tltQ, mainComponent, validated, emptyValidatedBusData, false, true);
        }
    }

    protected abstract void write(String busId, double incomingP, double incomingQ, double loadP, double loadQ, double genP, double genQ, double batP, double batQ,
                                  double shuntP, double shuntQ, double svcP, double svcQ, double vscCSP, double vscCSQ, double lineP, double lineQ,
                                  double danglingLineP, double danglingLineQ, double twtP, double twtQ, double tltP, double tltQ, boolean mainComponent,
                                  boolean validated, ValidatedBusData validatedBusData, boolean found, boolean writeValues) throws IOException;

    @Override
    public void write(String svcId, double p, double q, double vControlled, double vController, double nominalVcontroller, double reactivePowerSetpoint, double voltageSetpoint,
                      boolean connected, RegulationMode regulationMode, boolean regulating, double bMin, double bMax, boolean mainComponent, boolean validated) throws IOException {
        Objects.requireNonNull(svcId);
        ValidatedSvcData emptyValidatedSvcData = new ValidatedSvcData(svcId, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, false, RegulationMode.VOLTAGE, false, Double.NaN, Double.NaN, false, false);
        if (compareResults) {
            if (preLoadflowValidationCompleted) {
                boolean found = svcsData.containsKey(svcId);
                ValidatedSvcData validatedSvcData = found ? svcsData.get(svcId) : emptyValidatedSvcData;
                write(svcId, p, q, vControlled, vController, nominalVcontroller, reactivePowerSetpoint, voltageSetpoint, connected, regulationMode, regulating, bMin, bMax, mainComponent, validated, validatedSvcData, found, true);
                svcsData.remove(svcId);
            } else {
                svcsData.put(svcId, new ValidatedSvcData(svcId, p, q, vControlled, vController, nominalVcontroller, reactivePowerSetpoint, voltageSetpoint, connected, regulationMode, regulating, bMin, bMax, mainComponent, validated));
            }
        } else {
            write(svcId, p, q, vControlled, vController, nominalVcontroller, reactivePowerSetpoint, voltageSetpoint, connected, regulationMode, regulating, bMin, bMax, mainComponent, validated, emptyValidatedSvcData, false, true);
        }
    }

    protected abstract void write(String svcId, double p, double q, double vControlled, double vController, double nominalVcontroller, double reactivePowerSetpoint, double voltageSetpoint,
                                  boolean connected, RegulationMode regulationMode, boolean regulating, double bMin, double bMax, boolean mainComponent, boolean validated,
                                  ValidatedSvcData validatedSvcData, boolean found, boolean writeValues) throws IOException;

    @Override
    public void write(String shuntId, double q, double expectedQ, double p, int currentSectionCount, int maximumSectionCount,
                      double bPerSection, double v, boolean connected, double qMax, double nominalV, boolean mainComponent, boolean validated) throws IOException {
        Objects.requireNonNull(shuntId);
        ValidatedShuntData emptyValidatedShuntData = new ValidatedShuntData(shuntId, Double.NaN, Double.NaN, Double.NaN, -1, -1, Double.NaN, Double.NaN, false, Double.NaN, Double.NaN, false, false);
        if (compareResults) {
            if (preLoadflowValidationCompleted) {
                boolean found = shuntsData.containsKey(shuntId);
                ValidatedShuntData validatedShuntData = found ? shuntsData.get(shuntId) : emptyValidatedShuntData;
                write(shuntId, q, expectedQ, p, currentSectionCount, maximumSectionCount,
                        bPerSection, v, connected, qMax, nominalV, mainComponent, validated, validatedShuntData, found, true);
                shuntsData.remove(shuntId);
            } else {
                shuntsData.put(shuntId, new ValidatedShuntData(shuntId, q, expectedQ, p, currentSectionCount, maximumSectionCount,
                        bPerSection, v, connected, qMax, nominalV, mainComponent, validated));
            }
        } else {
            write(shuntId, q, expectedQ, p, currentSectionCount, maximumSectionCount, bPerSection, v, connected, qMax, nominalV, mainComponent, validated, emptyValidatedShuntData, false, true);
        }
    }

    protected abstract void write(String shuntId, double q, double expectedQ, double p, int currentSectionCount, int maximumSectionCount,
                                  double bPerSection, double v, boolean connected, double qMax, double nominalV, boolean mainComponent,
                                  boolean validated, ValidatedShuntData validatedShuntData, boolean found, boolean writeValues) throws IOException;

    @Override
    public void write(String twtId, double error, double upIncrement, double downIncrement, double rho, double rhoPreviousStep, double rhoNextStep,
                      int tapPosition, int lowTapPosition, int highTapPosition, double targetV, TwoSides regulatedSide, double v, boolean connected,
                      boolean mainComponent, boolean validated) throws IOException {
        Objects.requireNonNull(twtId);
        ValidatedTransformerData emptyTwtData = new ValidatedTransformerData(twtId, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN,
                Double.NaN, -1, -1, -1, Double.NaN, TwoSides.ONE, Double.NaN, false, false, false);
        if (compareResults) {
            if (preLoadflowValidationCompleted) {
                boolean found = twtsData.containsKey(twtId);
                ValidatedTransformerData twtData = found ? twtsData.get(twtId) : emptyTwtData;
                write(twtId, error, upIncrement, downIncrement, rho, rhoPreviousStep, rhoNextStep, tapPosition, lowTapPosition,
                        highTapPosition, targetV, regulatedSide, v, connected, mainComponent, validated, twtData, found, true);
                twtsData.remove(twtId);
            } else {
                twtsData.put(twtId, new ValidatedTransformerData(twtId, error, upIncrement, downIncrement, rho, rhoPreviousStep, rhoNextStep, tapPosition,
                        lowTapPosition, highTapPosition, targetV, regulatedSide, v, connected, mainComponent, validated));
            }
        } else {
            write(twtId, error, upIncrement, downIncrement, rho, rhoPreviousStep, rhoNextStep, tapPosition, lowTapPosition,
                    highTapPosition, targetV, regulatedSide, v, connected, mainComponent, validated, emptyTwtData, false, true);
        }
    }

    protected abstract void write(String twtId, double error, double upIncrement, double downIncrement, double rho, double rhoPreviousStep, double rhoNextStep,
                                  int tapPosition, int lowTapPosition, int highTapPosition, double targetV, TwoSides regulatedSide, double v, boolean connected,
                                  boolean mainComponent, boolean validated, ValidatedTransformerData twtData, boolean found, boolean writeValues) throws IOException;

    @Override
    public void write(String twtId, TwtData twtData, boolean validated) throws IOException {
        Objects.requireNonNull(twtId);
        Objects.requireNonNull(twtData);
        if (compareResults) {
            if (preLoadflowValidationCompleted) {
                boolean found = twts3wData.containsKey(twtId);
                ValidatedTransformer3WData validatedTransformer3WData = found ? twts3wData.get(twtId) : new ValidatedTransformer3WData(twtId, null, false);
                write(twtId, new ValidatedTransformer3WData(twtId, twtData, validated), validatedTransformer3WData, found, true);
                twts3wData.remove(twtId);
            } else {
                twts3wData.put(twtId, new ValidatedTransformer3WData(twtId, twtData, validated));
            }
        } else {
            write(twtId, new ValidatedTransformer3WData(twtId, twtData, validated), new ValidatedTransformer3WData(twtId, null, false), false, true);
        }
    }

    protected abstract void write(String twtId, ValidatedTransformer3WData validatedTransformer3WData1, ValidatedTransformer3WData validatedTransformer3WData2, boolean found, boolean writeValues) throws IOException;

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
            case TWTS3W:
                writeTwts3wData();
                break;
            default:
                throw new IllegalStateException("Unexpected ValidationType value: " + validationType);
        }
    }

    private void writeFlowsData() {
        flowsData.values().forEach(validatedFlowData -> {
            try {
                write(validatedFlowData.branchId(), Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN,
                        Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN,
                        Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, 0, false, false, false, false, false, validatedFlowData, true, false);
            } catch (IOException e) {
                LOGGER.error("Error writing data of branch {}: {}", validatedFlowData.branchId(), e.getMessage());
            }
        });
    }

    private void writeGeneratorsData() {
        generatorsData.values().forEach(validatedGeneratorData -> {
            try {
                write(validatedGeneratorData.generatorId(), Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN,
                        Double.NaN, Double.NaN, false, false, Double.NaN, Double.NaN, Double.NaN, Double.NaN, false, false,
                        validatedGeneratorData, true, false);
            } catch (IOException e) {
                LOGGER.error("Error writing data of generator {}: {}", validatedGeneratorData.generatorId(), e.getMessage());
            }
        });
    }

    private void writeBusesData() {
        busesData.values().forEach(validatedBusData -> {
            try {
                write(validatedBusData.busId(), Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN,
                        Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN,
                        Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, false, false, validatedBusData, true, false);
            } catch (IOException e) {
                LOGGER.error("Error writing data of bus {}: {}", validatedBusData.busId(), e.getMessage());
            }
        });
    }

    private void writeSvcsData() {
        svcsData.values().forEach(validatedSvcData -> {
            try {
                write(validatedSvcData.svcId(), Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN,
                        false, validatedSvcData.regulationMode(), false, Double.NaN, Double.NaN, false, false, validatedSvcData, true, false);
            } catch (IOException e) {
                LOGGER.error("Error writing data of svc {}: {}", validatedSvcData.svcId(), e.getMessage());
            }
        });
    }

    private void writeShuntsData() {
        shuntsData.values().forEach(validatedShuntData -> {
            try {
                write(validatedShuntData.shuntId(), Double.NaN, Double.NaN, Double.NaN, -1, -1, Double.NaN,
                        Double.NaN, false, Double.NaN, Double.NaN, false, false, validatedShuntData, true, false);
            } catch (IOException e) {
                LOGGER.error("Error writing data of shunt {}: {}", validatedShuntData.shuntId(), e.getMessage());
            }
        });
    }

    private void writeTwtsData() {
        twtsData.values().forEach(twtData -> {
            try {
                write(twtData.twtId(), Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN,
                        -1, -1, -1, Double.NaN, TwoSides.ONE, Double.NaN, false, false, false, twtData, true, false);
            } catch (IOException e) {
                LOGGER.error("Error writing data of twt {}: {}", twtData.twtId(), e.getMessage());
            }
        });
    }

    private void writeTwts3wData() {
        twts3wData.values().forEach(twtData -> {
            try {
                write(twtData.twtId(), new ValidatedTransformer3WData(twtData.twtId(), null, false), twtData, true, false);
            } catch (IOException e) {
                LOGGER.error("Error writing data of t3wt {}: {}", twtData.twtId(), e.getMessage());
            }
        });
    }

    @Override
    public void close() throws IOException {
        formatter.close();
    }

}
