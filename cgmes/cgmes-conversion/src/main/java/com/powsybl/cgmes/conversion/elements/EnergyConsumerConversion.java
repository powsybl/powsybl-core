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
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.LoadDetail;
import com.powsybl.iidm.network.extensions.LoadDetailAdder;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public class EnergyConsumerConversion extends AbstractConductingEquipmentConversion {

    public EnergyConsumerConversion(PropertyBag ec, Context context) {
        super("EnergyConsumer", ec, context);
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
        double pFixed = p.asDouble("pFixed", 0.0);
        double qFixed = p.asDouble("qFixed", 0.0);
        LoadAdder adder = voltageLevel().newLoad()
                .setP0(pFixed)
                .setQ0(qFixed)
                .setLoadType(loadType);
        identify(adder);
        connect(adder);
        model(adder);
        Load load = adder.add();
        addAliasesAndProperties(load);
        convertedTerminals(load.getTerminal());
        setLoadDetail(loadKind, load);

        addSpecificProperties(load, loadKind, pFixed, qFixed);
    }

    @Override
    public void update(Network network) {
        Load load = network.getLoad(id);
        if (load == null) {
            return;
        }
        updateTerminalData(network, load);
        double pFixed = Double.parseDouble(load.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "pFixed"));
        double qFixed = Double.parseDouble(load.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "qFixed"));
        load.setP0(p0(pFixed)).setQ0(q0(qFixed));

        String loadKind = load.getProperty(Conversion.PROPERTY_CGMES_ORIGINAL_CLASS);
        updateLoadDetail(loadKind, load);
    }

    private static void addSpecificProperties(Load load, String loadKind, double pFixed, double qFixed) {
        load.setProperty(Conversion.PROPERTY_CGMES_ORIGINAL_CLASS, loadKind);
        load.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "pFixed", String.valueOf(pFixed));
        load.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "qFixed", String.valueOf(qFixed));
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

    private static void setLoadDetail(String type, Load load) {
        if (type.equals("ConformLoad")) { // ConformLoad represent loads that follow a daily load change pattern where the pattern can be used to scale the load with a system load
            load.newExtension(LoadDetailAdder.class)
                    .withFixedActivePower(0)
                    .withFixedReactivePower(0)
                    .withVariableActivePower((float) load.getP0())
                    .withVariableReactivePower((float) load.getQ0())
                    .add();
        } else if (type.equals("NonConformLoad")) { // does not participate in scaling
            load.newExtension(LoadDetailAdder.class)
                    .withFixedActivePower((float) load.getP0())
                    .withFixedReactivePower((float) load.getQ0())
                    .withVariableActivePower(0)
                    .withVariableReactivePower(0)
                    .add();
        }
        // else: EnergyConsumer - undefined
    }

    private static void updateLoadDetail(String type, Load load) {
        if (type == null) {
            return;
        }
        LoadDetail loadDetail = load.getExtension(LoadDetail.class);
        if (loadDetail == null) {
            return;
        }
        if (type.equals("ConformLoad")) { // ConformLoad represent loads that follow a daily load change pattern where the pattern can be used to scale the load with a system load
            loadDetail.setVariableActivePower((float) load.getP0())
                            .setVariableReactivePower((float) load.getQ0());
        } else if (type.equals("NonConformLoad")) { // does not participate in scaling
            loadDetail.setFixedActivePower((float) load.getP0())
                            .setFixedReactivePower((float) load.getQ0());
        }
    }

    private double p0(double pFixed) {
        return powerFlow().defined() ? powerFlow().p() : pFixed;
    }

    private double q0(double qFixed) {
        return powerFlow().defined() ? powerFlow().q() : qFixed;
    }

    private final String loadKind;
}
