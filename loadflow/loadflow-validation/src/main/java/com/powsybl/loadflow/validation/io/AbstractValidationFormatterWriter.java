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
    protected Map<String, Validated<BusData>> busesData = new HashMap<>();
    protected Map<String, Validated<GeneratorData>> generatorsData = new HashMap<>();
    protected Map<String, ValidatedSvc> svcsData = new HashMap<>();
    protected Map<String, Validated<ShuntData>> shuntsData = new HashMap<>();
    protected Map<String, ValidatedFlow> flowsData = new HashMap<>();
    protected Map<String, ValidatedTransformer> twtsData = new HashMap<>();
    protected Map<String, ValidatedTransformer3W> twts3wData = new HashMap<>();

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
    public void writeBranch(String branchId, double p1, double p1Calc, double q1, double q1Calc, double p2, double p2Calc, double q2, double q2Calc,
                            double r, double x, double g1, double g2, double b1, double b2, double rho1, double rho2, double alpha1, double alpha2,
                            double u1, double u2, double theta1, double theta2, double z, double y, double ksi, int phaseAngleClock, boolean connected1, boolean connected2,
                            boolean mainComponent1, boolean mainComponent2, boolean validated) throws IOException {
        Objects.requireNonNull(branchId);
        ValidatedFlow emptyValidatedFlow = new ValidatedFlow(branchId, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN,
                Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN,
                Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, 0, false, false, false, false, false);
        if (compareResults) {
            if (preLoadflowValidationCompleted) {
                boolean found = flowsData.containsKey(branchId);
                ValidatedFlow validatedFlow = found ? flowsData.get(branchId) : emptyValidatedFlow;
                writeBranch(branchId, p1, p1Calc, q1, q1Calc, p2, p2Calc, q2, q2Calc, r, x, g1, g2, b1, b2, rho1, rho2, alpha1, alpha2, u1, u2,
                        theta1, theta2, z, y, ksi, phaseAngleClock, connected1, connected2, mainComponent1, mainComponent2, validated, validatedFlow, found, true);
                flowsData.remove(branchId);
            } else {
                flowsData.put(branchId, new ValidatedFlow(branchId, p1, p1Calc, q1, q1Calc, p2, p2Calc, q2, q2Calc, r, x, g1, g2, b1, b2, rho1, rho2, alpha1, alpha2,
                        u1, u2, theta1, theta2, z, y, ksi, phaseAngleClock, connected1, connected2, mainComponent1, mainComponent2, validated));
            }
        } else {
            writeBranch(branchId, p1, p1Calc, q1, q1Calc, p2, p2Calc, q2, q2Calc, r, x, g1, g2, b1, b2, rho1, rho2, alpha1, alpha2, u1, u2,
                    theta1, theta2, z, y, ksi, phaseAngleClock, connected1, connected2, mainComponent1, mainComponent2, validated, emptyValidatedFlow, false, true);
        }
    }

    protected abstract void writeBranch(String branchId, double p1, double p1Calc, double q1, double q1Calc, double p2, double p2Calc, double q2, double q2Calc,
                                        double r, double x, double g1, double g2, double b1, double b2, double rho1, double rho2, double alpha1, double alpha2,
                                        double u1, double u2, double theta1, double theta2, double z, double y, double ksi, int phaseAngleClock, boolean connected1, boolean connected2,
                                        boolean mainComponent1, boolean mainComponent2, boolean validated, ValidatedFlow validatedFlow, boolean found, boolean writeValues) throws IOException;

    @Override
    public void writeGenerator(Validated<GeneratorData> validatedGeneratorData) throws IOException {
        Objects.requireNonNull(validatedGeneratorData);
        String generatorId = validatedGeneratorData.data().generatorId();
        Validated<GeneratorData> emptyValidatedGenerator = GeneratorData.createEmptyValidated(generatorId);
        if (compareResults) {
            if (preLoadflowValidationCompleted) {
                boolean found = generatorsData.containsKey(generatorId);
                Validated<GeneratorData> validatedGenerator = found ? generatorsData.get(generatorId) : emptyValidatedGenerator;
                writeGenerator(validatedGeneratorData, validatedGenerator, found, true);
                generatorsData.remove(generatorId);
            } else {
                generatorsData.put(generatorId, validatedGeneratorData);
            }
        } else {
            writeGenerator(validatedGeneratorData, emptyValidatedGenerator, false, true);
        }
    }

    protected abstract void writeGenerator(Validated<GeneratorData> v, Validated<GeneratorData> validatedGenerator, boolean found, boolean writeValues) throws IOException;

    @Override
    public void writeBus(Validated<BusData> v) throws IOException {
        Objects.requireNonNull(v);
        String busId = v.data().busId();
        Validated<BusData> emptyValidatedBus = BusData.createEmptyValidated(busId);
        if (compareResults) {
            if (preLoadflowValidationCompleted) {
                boolean found = busesData.containsKey(busId);
                Validated<BusData> validatedBus = found ? busesData.get(busId) : emptyValidatedBus;
                writeBus(v, validatedBus, found, true);
                busesData.remove(busId);
            } else {
                busesData.put(busId, v);
            }
        } else {
            writeBus(v, emptyValidatedBus, false, true);
        }
    }

    protected abstract void writeBus(Validated<BusData> v, Validated<BusData> validatedBus, boolean found, boolean writeValues) throws IOException;

    @Override
    public void writeSvc(String svcId, double p, double q, double vControlled, double vController, double nominalVcontroller, double reactivePowerSetpoint, double voltageSetpoint,
                         boolean connected, RegulationMode regulationMode, boolean regulating, double bMin, double bMax, boolean mainComponent, boolean validated) throws IOException {
        Objects.requireNonNull(svcId);
        ValidatedSvc emptyValidatedSvc = new ValidatedSvc(svcId, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, false, RegulationMode.VOLTAGE, false, Double.NaN, Double.NaN, false, false);
        if (compareResults) {
            if (preLoadflowValidationCompleted) {
                boolean found = svcsData.containsKey(svcId);
                ValidatedSvc validatedSvc = found ? svcsData.get(svcId) : emptyValidatedSvc;
                writeSvc(svcId, p, q, vControlled, vController, nominalVcontroller, reactivePowerSetpoint, voltageSetpoint, connected, regulationMode, regulating, bMin, bMax, mainComponent, validated, validatedSvc, found, true);
                svcsData.remove(svcId);
            } else {
                svcsData.put(svcId, new ValidatedSvc(svcId, p, q, vControlled, vController, nominalVcontroller, reactivePowerSetpoint, voltageSetpoint, connected, regulationMode, regulating, bMin, bMax, mainComponent, validated));
            }
        } else {
            writeSvc(svcId, p, q, vControlled, vController, nominalVcontroller, reactivePowerSetpoint, voltageSetpoint, connected, regulationMode, regulating, bMin, bMax, mainComponent, validated, emptyValidatedSvc, false, true);
        }
    }

    protected abstract void writeSvc(String svcId, double p, double q, double vControlled, double vController, double nominalVcontroller, double reactivePowerSetpoint, double voltageSetpoint,
                                     boolean connected, RegulationMode regulationMode, boolean regulating, double bMin, double bMax, boolean mainComponent, boolean validated,
                                     ValidatedSvc validatedSvc, boolean found, boolean writeValues) throws IOException;

    @Override
    public void writeShunt(Validated<ShuntData> validatedShuntData) throws IOException {
        Objects.requireNonNull(validatedShuntData);
        String shuntId = validatedShuntData.data().shuntId();
        Validated<ShuntData> emptyValidatedShunt = ShuntData.createEmptyValidated(shuntId);
        if (compareResults) {
            if (preLoadflowValidationCompleted) {
                boolean found = shuntsData.containsKey(shuntId);
                Validated<ShuntData> validatedShunt = found ? shuntsData.get(shuntId) : emptyValidatedShunt;
                writeShunt(validatedShuntData, validatedShunt, found, true);
                shuntsData.remove(shuntId);
            } else {
                shuntsData.put(shuntId, validatedShuntData);
            }
        } else {
            writeShunt(validatedShuntData, emptyValidatedShunt, false, true);
        }
    }

    protected abstract void writeShunt(Validated<ShuntData> v, Validated<ShuntData> validatedShunt, boolean found, boolean writeValues) throws IOException;

    @Override
    public void writeT2wt(String twtId, double error, double upIncrement, double downIncrement, double rho, double rhoPreviousStep, double rhoNextStep,
                          int tapPosition, int lowTapPosition, int highTapPosition, double targetV, TwoSides regulatedSide, double v, boolean connected,
                          boolean mainComponent, boolean validated) throws IOException {
        Objects.requireNonNull(twtId);
        ValidatedTransformer emptyTwtData = new ValidatedTransformer(twtId, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN,
                Double.NaN, -1, -1, -1, Double.NaN, TwoSides.ONE, Double.NaN, false, false, false);
        if (compareResults) {
            if (preLoadflowValidationCompleted) {
                boolean found = twtsData.containsKey(twtId);
                ValidatedTransformer twtData = found ? twtsData.get(twtId) : emptyTwtData;
                writeT2wt(twtId, error, upIncrement, downIncrement, rho, rhoPreviousStep, rhoNextStep, tapPosition, lowTapPosition,
                        highTapPosition, targetV, regulatedSide, v, connected, mainComponent, validated, twtData, found, true);
                twtsData.remove(twtId);
            } else {
                twtsData.put(twtId, new ValidatedTransformer(twtId, error, upIncrement, downIncrement, rho, rhoPreviousStep, rhoNextStep, tapPosition,
                        lowTapPosition, highTapPosition, targetV, regulatedSide, v, connected, mainComponent, validated));
            }
        } else {
            writeT2wt(twtId, error, upIncrement, downIncrement, rho, rhoPreviousStep, rhoNextStep, tapPosition, lowTapPosition,
                    highTapPosition, targetV, regulatedSide, v, connected, mainComponent, validated, emptyTwtData, false, true);
        }
    }

    protected abstract void writeT2wt(String twtId, double error, double upIncrement, double downIncrement, double rho, double rhoPreviousStep, double rhoNextStep,
                                      int tapPosition, int lowTapPosition, int highTapPosition, double targetV, TwoSides regulatedSide, double v, boolean connected,
                                      boolean mainComponent, boolean validated, ValidatedTransformer twtData, boolean found, boolean writeValues) throws IOException;

    @Override
    public void writeT3wt(String twtId, TwtData twtData, boolean validated) throws IOException {
        Objects.requireNonNull(twtId);
        Objects.requireNonNull(twtData);
        if (compareResults) {
            if (preLoadflowValidationCompleted) {
                boolean found = twts3wData.containsKey(twtId);
                ValidatedTransformer3W validatedTransformer3W = found ? twts3wData.get(twtId) : new ValidatedTransformer3W(twtId, null, false);
                writeT3wt(twtId, new ValidatedTransformer3W(twtId, twtData, validated), validatedTransformer3W, found, true);
                twts3wData.remove(twtId);
            } else {
                twts3wData.put(twtId, new ValidatedTransformer3W(twtId, twtData, validated));
            }
        } else {
            writeT3wt(twtId, new ValidatedTransformer3W(twtId, twtData, validated), new ValidatedTransformer3W(twtId, null, false), false, true);
        }
    }

    protected abstract void writeT3wt(String twtId, ValidatedTransformer3W validatedTransformer3W1, ValidatedTransformer3W validatedTransformer3W2, boolean found, boolean writeValues) throws IOException;

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
        flowsData.values().forEach(validatedFlow -> {
            try {
                writeBranch(validatedFlow.branchId(), Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN,
                        Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN,
                        Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, 0, false, false, false, false, false, validatedFlow, true, false);
            } catch (IOException e) {
                LOGGER.error("Error writing data of branch {}: {}", validatedFlow.branchId(), e.getMessage());
            }
        });
    }

    private void writeGeneratorsData() {
        generatorsData.values().forEach(validatedGenerator -> {
            try {
                writeGenerator(GeneratorData.createEmptyValidated(validatedGenerator.data().generatorId()),
                        validatedGenerator, true, false);
            } catch (IOException e) {
                LOGGER.error("Error writing data of generator {}: {}", validatedGenerator.data().generatorId(), e.getMessage());
            }
        });
    }

    private void writeBusesData() {
        busesData.values().forEach(validatedBus -> {
            try {
                writeBus(BusData.createEmptyValidated(validatedBus.data().busId()), validatedBus, true, false);
            } catch (IOException e) {
                LOGGER.error("Error writing data of bus {}: {}", validatedBus.data().busId(), e.getMessage());
            }
        });
    }

    private void writeSvcsData() {
        svcsData.values().forEach(validatedSvc -> {
            try {
                writeSvc(validatedSvc.svcId(), Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN,
                        false, validatedSvc.regulationMode(), false, Double.NaN, Double.NaN, false, false, validatedSvc, true, false);
            } catch (IOException e) {
                LOGGER.error("Error writing data of svc {}: {}", validatedSvc.svcId(), e.getMessage());
            }
        });
    }

    private void writeShuntsData() {
        shuntsData.values().forEach(validatedShunt -> {
            try {
                writeShunt(ShuntData.createEmptyValidated(validatedShunt.data().shuntId()),
                        validatedShunt, true, false);
            } catch (IOException e) {
                LOGGER.error("Error writing data of shunt {}: {}", validatedShunt.data().shuntId(), e.getMessage());
            }
        });
    }

    private void writeTwtsData() {
        twtsData.values().forEach(twtData -> {
            try {
                writeT2wt(twtData.twtId(), Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN,
                        -1, -1, -1, Double.NaN, TwoSides.ONE, Double.NaN, false, false, false, twtData, true, false);
            } catch (IOException e) {
                LOGGER.error("Error writing data of twt {}: {}", twtData.twtId(), e.getMessage());
            }
        });
    }

    private void writeTwts3wData() {
        twts3wData.values().forEach(twtData -> {
            try {
                writeT3wt(twtData.twtId(), new ValidatedTransformer3W(twtData.twtId(), null, false), twtData, true, false);
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
