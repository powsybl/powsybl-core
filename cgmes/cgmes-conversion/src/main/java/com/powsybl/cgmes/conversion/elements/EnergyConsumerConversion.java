/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.elements;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.cgmes.model.PowerFlow;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.LoadDetail;
import com.powsybl.iidm.network.extensions.LoadDetailAdder;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public class EnergyConsumerConversion extends AbstractConductingEquipmentConversion {

    public EnergyConsumerConversion(PropertyBag ec, Context context) {
        super(CgmesNames.ENERGY_CONSUMER, ec, context);
        loadKind = ec.getLocal("type");
    }

    @Override
    public void convert() {
        LoadType loadType;
        if (loadKind.equals("StationSupply")) {
            loadType = LoadType.AUXILIARY;
        } else if (id.contains("fict")) {
            loadType = LoadType.FICTITIOUS;
        } else {
            loadType = LoadType.UNDEFINED;
        }
        double pFixed = p.asDouble(CgmesNames.P_FIXED, 0.0);
        double qFixed = p.asDouble(CgmesNames.Q_FIXED, 0.0);
        LoadAdder adder = voltageLevel().newLoad()
                .setLoadType(loadType);
        identify(adder);
        connectWithOnlyEq(adder);
        model(adder);
        Load newLoad = adder.add();
        addAliasesAndProperties(newLoad);
        convertedTerminalsWithOnlyEq(newLoad.getTerminal());
        setLoadDetail(loadKind, newLoad, pFixed, qFixed);

        addSpecificProperties(newLoad, pFixed, qFixed);
    }

    private void addSpecificProperties(Load newLoad, double pFixed, double qFixed) {
        newLoad.setProperty(Conversion.PROPERTY_CGMES_ORIGINAL_CLASS, loadKind);
        newLoad.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.P_FIXED, String.valueOf(pFixed));
        newLoad.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.Q_FIXED, String.valueOf(qFixed));
    }

    private void model(LoadAdder adder) {
        p.asBoolean("exponentModel").ifPresent(exponentModel -> {
            if (Boolean.TRUE.equals(exponentModel)) {
                double pVoltageExponent = p.asDouble("pVoltageExponent", 0);
                double qVoltageExponent = p.asDouble("qVoltageExponent", 0);
                boolean constantPower = pVoltageExponent == 0 && qVoltageExponent == 0;
                if (!constantPower) {
                    adder.newExponentialModel()
                            .setNp(pVoltageExponent)
                            .setNq(qVoltageExponent)
                            .add();
                }
            } else {
                addZipModel(adder);
            }
        });
    }

    private void addZipModel(LoadAdder adder) {
        double pConstantPower = p.asDouble("pConstantPower");
        double pConstantCurrent = p.asDouble("pConstantCurrent");
        double pConstantImpedance = p.asDouble("pConstantImpedance");
        double qConstantPower = p.asDouble("qConstantPower");
        double qConstantCurrent = p.asDouble("qConstantCurrent");
        double qConstantImpedance = p.asDouble("qConstantImpedance");
        // as far as only one of the 3 coefficient is not defined we cannot rely on the others
        if (!Double.isNaN(pConstantPower) && !Double.isNaN(pConstantCurrent) && !Double.isNaN(pConstantImpedance)
            && !Double.isNaN(qConstantPower) && !Double.isNaN(qConstantCurrent) && !Double.isNaN(qConstantImpedance)) {
            boolean constantPower = pConstantPower == 1 && pConstantCurrent == 0 && pConstantImpedance == 0
                && qConstantPower == 1 && qConstantCurrent == 0 && qConstantImpedance == 0;
            if (!constantPower) {
                // if sum of coefficient is not equals to one, rescale values
                double pSum = pConstantPower + pConstantCurrent + pConstantImpedance;
                if (Math.abs(pSum - 1d) > ZipLoadModelAdder.SUM_EPSILON) {
                    pConstantPower = pConstantPower / pSum;
                    pConstantCurrent = pConstantCurrent / pSum;
                    pConstantImpedance = pConstantImpedance / pSum;
                    fixed("active coefficients of zip load", "sum of pConstantPower, pConstantCurrent and pConstantImpedance is not equals to 1");
                }
                double qSum = qConstantPower + qConstantCurrent + qConstantImpedance;
                if (Math.abs(qSum - 1d) > ZipLoadModelAdder.SUM_EPSILON) {
                    qConstantPower = qConstantPower / qSum;
                    qConstantCurrent = qConstantCurrent / qSum;
                    qConstantImpedance = qConstantImpedance / qSum;
                    fixed("reactive coefficients of zip load", "sum of qConstantPower, qConstantCurrent and qConstantImpedance is not equals to 1");
                }
                adder.newZipModel()
                    .setC0p(pConstantPower)
                    .setC1p(pConstantCurrent)
                    .setC2p(pConstantImpedance)
                    .setC0q(qConstantPower)
                    .setC1q(qConstantCurrent)
                    .setC2q(qConstantImpedance)
                    .add();
            }
        }
    }

    private static void setLoadDetail(String type, Load newLoad, double pFixed, double qFixed) {
        if (type.equals("ConformLoad")) { // ConformLoad represent loads that follow a daily load change pattern where the pattern can be used to scale the load with a system load
            newLoad.newExtension(LoadDetailAdder.class)
                    .withFixedActivePower(0)
                    .withFixedReactivePower(0)
                    .withVariableActivePower((float) pFixed)
                    .withVariableReactivePower((float) qFixed)
                    .add();
        } else if (type.equals("NonConformLoad")) { // does not participate in scaling
            newLoad.newExtension(LoadDetailAdder.class)
                    .withFixedActivePower((float) pFixed)
                    .withFixedReactivePower((float) qFixed)
                    .withVariableActivePower(0)
                    .withVariableReactivePower(0)
                    .add();
        }
        // else: EnergyConsumer - undefined
    }

    public static void update(Load load, PropertyBag cgmesData, Context context) {
        updateTerminals(load, context, load.getTerminal());

        double pFixed = Double.parseDouble(load.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.P_FIXED, "0.0"));
        double qFixed = Double.parseDouble(load.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.Q_FIXED, "0.0"));

        PowerFlow updatedPowerFlow = updatedPowerFlow(load, cgmesData, context);
        load.setP0(updatedPowerFlow.defined() ? updatedPowerFlow.p() : getDefaultP0(load, pFixed, context));
        load.setQ0(updatedPowerFlow.defined() ? updatedPowerFlow.q() : getDefaultQ0(load, qFixed, context));

        updateLoadDetail(load, load.getProperty(Conversion.PROPERTY_CGMES_ORIGINAL_CLASS), pFixed, qFixed);
    }

    private static double getDefaultP0(Load load, double pFixed, Context context) {
        return getDefaultValue(pFixed, load.getP0(), 0.0, Double.NaN, context);
    }

    private static double getDefaultQ0(Load load, double qFixed, Context context) {
        return getDefaultValue(qFixed, load.getQ0(), 0.0, Double.NaN, context);
    }

    private static void updateLoadDetail(Load load, String type, double pFixed, double qFixed) {
        if (type == null) {
            return;
        }
        LoadDetail loadDetail = load.getExtension(LoadDetail.class);
        if (loadDetail == null) {
            return;
        }
        if (type.equals("ConformLoad")) { // ConformLoad represent loads that follow a daily load change pattern where the pattern can be used to scale the load with a system load
            loadDetail.setVariableActivePower(Double.isFinite(load.getP0()) ? load.getP0() : pFixed)
                    .setVariableReactivePower(Double.isFinite(load.getQ0()) ? load.getQ0() : qFixed);
        } else if (type.equals("NonConformLoad")) { // does not participate in scaling
            loadDetail.setFixedActivePower(Double.isFinite(load.getP0()) ? load.getP0() : pFixed)
                    .setFixedReactivePower(Double.isFinite(load.getQ0()) ? load.getQ0() : qFixed);
        }
    }

    private final String loadKind;
}
