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
import com.powsybl.cgmes.conversion.RegulatingControlMappingForGenerators;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.cgmes.model.PowerFlow;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.ActivePowerControlAdder;
import com.powsybl.iidm.network.extensions.ReferencePriority;

import com.powsybl.triplestore.api.PropertyBag;
import java.util.Arrays;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public class SynchronousMachineConversion extends AbstractReactiveLimitsOwnerConversion {

    private final boolean isCondenser;

    public SynchronousMachineConversion(PropertyBag sm, Context context) {
        super(CgmesNames.SYNCHRONOUS_MACHINE, sm, context);
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
        PowerFlow f = powerFlow();

        // Default targetP from initial P defined in EQ GeneratingUnit. Removed since CGMES 3.0
        double targetP = p.asDouble("initialP", 0);
        double targetQ = 0;
        // Flow values may come from Terminal or Equipment (SSH RotatingMachine)
        if (f.defined()) {
            targetP = -f.p();
            targetQ = -f.q();
        }

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
        generator.setProperty(Conversion.PROPERTY_CGMES_ORIGINAL_CLASS, CgmesNames.SYNCHRONOUS_MACHINE);
        String type = p.getLocal("type");
        if (type != null) {
            generator.setProperty(Conversion.PROPERTY_CGMES_SYNCHRONOUS_MACHINE_TYPE, type.replace("SynchronousMachineKind.", ""));
        }
        String operatingMode = p.getLocal("operatingMode");
        if (operatingMode != null) {
            generator.setProperty(Conversion.PROPERTY_CGMES_SYNCHRONOUS_MACHINE_OPERATING_MODE, operatingMode.replace("SynchronousMachineOperatingMode.", ""));
        }
    }

    private void convertGenerator(Generator g) {
        double normalPF = p.asDouble("normalPF");
        if (!Double.isNaN(normalPF)) {
            if (context.config().createActivePowerControlExtension()) {
                g.newExtension(ActivePowerControlAdder.class)
                        .withParticipate(true)
                        .withParticipationFactor(normalPF)
                        .add();
            } else {
                g.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "normalPF", String.valueOf(normalPF));
            }
        }
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
        String windGenUnitType = p.getLocal("windGenUnitType");
        if (windGenUnitType != null) {
            generator.setProperty(Conversion.PROPERTY_WIND_GEN_UNIT_TYPE, windGenUnitType.replace("WindGenUnitKind.", ""));
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
}
