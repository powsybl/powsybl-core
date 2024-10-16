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
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.LoadDetail;
import com.powsybl.iidm.network.extensions.LoadDetailAdder;
import com.powsybl.triplestore.api.PropertyBag;

import java.util.List;
import java.util.Objects;

import static com.powsybl.cgmes.conversion.Conversion.Config.DefaultValue.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public class EnergyConsumerConversion extends AbstractConductingEquipmentConversion {

    private static final String P_FIXED = "pFixed";
    private static final String Q_FIXED = "qFixed";

    public EnergyConsumerConversion(PropertyBag ec, Context context) {
        super(CgmesNames.ENERGY_CONSUMER, ec, context);
        loadKind = ec.getLocal("type");
        this.load = null;
    }

    public EnergyConsumerConversion(PropertyBag es, PropertyBag cgmesTerminal, Load load, Context context) {
        super(CgmesNames.ENERGY_CONSUMER, es, cgmesTerminal, context);
        this.loadKind = null;
        this.load = load;
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
        double pFixed = p.asDouble(P_FIXED, 0.0);
        double qFixed = p.asDouble(Q_FIXED, 0.0);
        LoadAdder adder = voltageLevel().newLoad()
                .setLoadType(loadType);
        identify(adder);
        connection(adder);
        model(adder);
        Load load = adder.add();
        addAliasesAndProperties(load);
        mappingTerminals(load.getTerminal());
        setLoadDetail(loadKind, load, pFixed, qFixed);

        addSpecificProperties(load, pFixed, qFixed);
    }

    private void addSpecificProperties(Load newLoad, double pFixed, double qFixed) {
        newLoad.setProperty(Conversion.PROPERTY_CGMES_ORIGINAL_CLASS, loadKind);
        newLoad.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + P_FIXED, String.valueOf(pFixed));
        newLoad.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + Q_FIXED, String.valueOf(qFixed));
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

    private static void setLoadDetail(String type, Load load, double pFixed, double qFixed) {
        if (type.equals("ConformLoad")) { // ConformLoad represent loads that follow a daily load change pattern where the pattern can be used to scale the load with a system load
            load.newExtension(LoadDetailAdder.class)
                    .withFixedActivePower(0)
                    .withFixedReactivePower(0)
                    .withVariableActivePower((float) pFixed)
                    .withVariableReactivePower((float) qFixed)
                    .add();
        } else if (type.equals("NonConformLoad")) { // does not participate in scaling
            load.newExtension(LoadDetailAdder.class)
                    .withFixedActivePower((float) pFixed)
                    .withFixedReactivePower((float) qFixed)
                    .withVariableActivePower(0)
                    .withVariableReactivePower(0)
                    .add();
        }
        // else: EnergyConsumer - undefined
    }

    @Override
    public void update(Network network) {
        Objects.requireNonNull(load);
        updateTerminals(context, load.getTerminal());

        double pFixed = Double.parseDouble(load.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + P_FIXED));
        double qFixed = Double.parseDouble(load.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + Q_FIXED));
        load.setP0(updatedP0().orElse(defaultP(pFixed, load.getP0(), getDefaultValue(context))))
                .setQ0(qupdatedQ0().orElse(defaultQ(qFixed, load.getQ0(), getDefaultValue(context))));

        String loadKind = load.getProperty(Conversion.PROPERTY_CGMES_ORIGINAL_CLASS);
        updateLoadDetail(loadKind, pFixed, qFixed);
    }

    private static Conversion.Config.DefaultValue getDefaultValue(Context context) {
        return selectDefaultValue(List.of(EQ, PREVIOUS, ZERO, NAN), context);
    }

    private void updateLoadDetail(String type, double pFixed, double qFixed) {
        if (type == null) {
            return;
        }
        LoadDetail loadDetail = load.getExtension(LoadDetail.class);
        if (loadDetail == null) {
            return;
        }
        if (type.equals("ConformLoad")) { // ConformLoad represent loads that follow a daily load change pattern where the pattern can be used to scale the load with a system load
            loadDetail.setVariableActivePower(Double.isFinite(load.getP0()) ? (float) load.getP0() : (float) pFixed)
                    .setVariableReactivePower(Double.isFinite(load.getQ0()) ? (float) load.getQ0() : (float) qFixed);
        } else if (type.equals("NonConformLoad")) { // does not participate in scaling
            loadDetail.setFixedActivePower(Double.isFinite(load.getP0()) ? (float) load.getP0() : (float) pFixed)
                    .setFixedReactivePower(Double.isFinite(load.getQ0()) ? (float) load.getQ0() : (float) qFixed);
        }
    }

    private final String loadKind;
    private final Load load;
}
