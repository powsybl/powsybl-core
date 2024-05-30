/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.elements;

import com.powsybl.cgmes.conversion.*;
import com.powsybl.cgmes.model.PowerFlow;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.ActivePowerControl;
import com.powsybl.iidm.network.extensions.ActivePowerControlAdder;
import com.powsybl.iidm.network.extensions.ReferencePriority;

import com.powsybl.triplestore.api.PropertyBag;

import static com.powsybl.cgmes.model.CgmesNames.SYNCHRONOUS_MACHINE;
import java.util.Arrays;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public class SynchronousMachineConversion extends AbstractReactiveLimitsOwnerConversion {

    private final boolean isCondenser;

    public SynchronousMachineConversion(PropertyBag sm, Context context) {
        super(SYNCHRONOUS_MACHINE, sm, context);
        String type = p.getLocal("type");
        isCondenser = type != null && type.endsWith("Kind.condenser");
    }

    @Override
    public void convert() {
        // If it is a generator, default values for minP and maxP give unlimited range
        // If it is a condenser, default values for minP and maxP are 0
        double minP = p.asDouble("minP", isCondenser ? 0 : -Double.MAX_VALUE);
        double maxP = p.asDouble("maxP", isCondenser ? 0 : Double.MAX_VALUE);
        double ratedS = p.asDouble("ratedS");
        ratedS = ratedS > 0 ? ratedS : Double.NaN;

        // Default targetP from initial P defined in EQ GeneratingUnit. Removed since CGMES 3.0
        double targetP = p.asDouble("initialP", 0);
        double targetQ = 0;

        GeneratorAdder adder = voltageLevel().newGenerator();
        RegulatingControlMappingForGenerators.initialize(adder);
        setMinPMaxP(adder, minP, maxP);
        adder.setTargetP(targetP)
                .setTargetQ(targetQ)
                .setEnergySource(energySourceFromGeneratingUnitType())
                .setRatedS(ratedS);
        identify(adder);
        connect(adder);
        Generator g = adder.add();
        addAliasesAndProperties(g);
        convertedTerminals(g.getTerminal());
        convertReactiveLimits(g);
        int referencePriority = p.asInt("referencePriority", 0);
        if (referencePriority > 0) {
            ReferencePriority.set(g, referencePriority);
        }
        if (!isCondenser) {
            convertGenerator(g);
        }

        context.regulatingControlMapping().forGenerators().add(g.getId(), p);
        addSpecificProperties(g, p);
    }

    private static void addSpecificProperties(Generator generator, PropertyBag p) {
        generator.setProperty(Conversion.PROPERTY_CGMES_ORIGINAL_CLASS, SYNCHRONOUS_MACHINE);
        String type = p.getLocal("type");
        if (type != null) {
            generator.setProperty(Conversion.PROPERTY_CGMES_SYNCHRONOUS_MACHINE_TYPE, type.replace("SynchronousMachineKind.", ""));
        }
    }

    private void convertGenerator(Generator g) {
        String generatingUnit = p.getId("GeneratingUnit");
        if (generatingUnit != null) {
            g.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "GeneratingUnit", generatingUnit);
        }
        addSpecificGeneratingUnitProperties(g, p);
    }

    private static void addSpecificGeneratingUnitProperties(Generator generator, PropertyBag p) {
        String hydroPlantStorageType = p.getLocal("hydroPlantStorageType");
        if (hydroPlantStorageType != null) {
            generator.setProperty(Conversion.PROPERTY_HYDRO_PLANT_STORAGE_TYPE, hydroPlantStorageType.replace("HydroPlantStorageKind.", ""));
        }
        String fossilFuelType = String.join(";",
                Arrays.stream(p.getLocals("fossilFuelTypeList", ";"))
                        .map(ff -> ff.replace("FuelType.", ""))
                        .toList());
        if (!fossilFuelType.isEmpty()) {
            generator.setProperty(Conversion.PROPERTY_FOSSIL_FUEL_TYPE, fossilFuelType);
        }
    }

    private EnergySource energySourceFromGeneratingUnitType() {
        String gut = p.getLocal("generatingUnitType");
        EnergySource es = EnergySource.OTHER;
        if (gut != null) {
            if (gut.contains("HydroGeneratingUnit")) {
                es = EnergySource.HYDRO;
            } else if (gut.contains("NuclearGeneratingUnit")) {
                es = EnergySource.NUCLEAR;
            } else if (gut.contains("ThermalGeneratingUnit")) {
                es = EnergySource.THERMAL;
            } else if (gut.contains("WindGeneratingUnit")) {
                es = EnergySource.WIND;
            } else if (gut.contains("SolarGeneratingUnit")) {
                es = EnergySource.SOLAR;
            }
        }
        return es;
    }

    @Override
    public void update(Network network) {
        Generator generator = network.getGenerator(id);
        if (generator == null) {
            return;
        }
        updateTerminalData(generator);
        PowerFlow f = powerFlow();
        if (f.defined()) {
            double targetP = -f.p();
            double targetQ = -f.q();
            generator.setTargetP(targetP).setTargetQ(targetQ);
        }

        String generatingUnitId = generator.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "GeneratingUnit");
        if (generatingUnitId != null) {
            updateGenerator(generator, generatingUnitId);
        }

        String operatingMode = p.getLocal("operatingMode");
        if (operatingMode != null) {
            generator.setProperty(Conversion.PROPERTY_CGMES_SYNCHRONOUS_MACHINE_OPERATING_MODE, operatingMode.replace("SynchronousMachineOperatingMode.", ""));
        }
        boolean controlEnabled = p.asBoolean("controlEnabled", false);
        updateRegulatingControlForGenerator(generator, controlEnabled);
    }

    private void updateGenerator(Generator g, String generatingUnitId) {
        double normalPF = context.generatingUnitUpdate().getNormalPF(generatingUnitId).orElse(Double.NaN);
        if (!Double.isNaN(normalPF)) {
            ActivePowerControl activePowerControl = g.getExtension(ActivePowerControl.class);
            if (activePowerControl != null) {
                activePowerControl.setParticipationFactor(normalPF);
            } else if (context.config().createActivePowerControlExtension()) {
                g.newExtension(ActivePowerControlAdder.class)
                        .withParticipate(true)
                        .withParticipationFactor(normalPF)
                        .add();
            } else {
                g.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "normalPF", String.valueOf(normalPF));
            }
        }
    }
}
