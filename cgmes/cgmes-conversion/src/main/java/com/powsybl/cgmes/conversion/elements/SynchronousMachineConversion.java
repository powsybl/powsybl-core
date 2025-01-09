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
import com.powsybl.iidm.network.extensions.ActivePowerControl;
import com.powsybl.iidm.network.extensions.ActivePowerControlAdder;

import com.powsybl.iidm.network.extensions.ReferencePriority;
import com.powsybl.triplestore.api.PropertyBag;
import java.util.Arrays;
import java.util.Optional;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public class SynchronousMachineConversion extends AbstractReactiveLimitsOwnerConversion {

    private final boolean isCondenser;

    public SynchronousMachineConversion(PropertyBag sm, Context context) {
        super(CgmesNames.SYNCHRONOUS_MACHINE, sm, context);
        String type = p.getLocal("type");
        // CIM14 uses Type.condenser, CIM16 and CIM100 use Kind.condenser
        isCondenser = type != null && type.endsWith(".condenser");
    }

    @Override
    public void convert() {
        // If it is a generator, default values for minP and maxP give unlimited range
        // If it is a condenser, default values for minP and maxP are 0
        double minP = p.asDouble("minP", isCondenser ? 0 : -Double.MAX_VALUE);
        double maxP = p.asDouble("maxP", isCondenser ? 0 : Double.MAX_VALUE);
        double ratedS = p.asDouble("ratedS");
        ratedS = ratedS > 0 ? ratedS : Double.NaN;

        GeneratorAdder adder = voltageLevel().newGenerator();
        RegulatingControlMappingForGenerators.initialize(adder);
        setMinPMaxP(adder, minP, maxP);
        adder.setEnergySource(energySourceFromGeneratingUnitType())
                .setRatedS(ratedS);
        identify(adder);
        connectWithOnlyEq(adder);
        adder.setCondenser(isCondenser);
        Generator g = adder.add();
        addAliasesAndProperties(g);
        convertedTerminalsWithOnlyEq(g.getTerminal());
        convertReactiveLimits(g);

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
        String generatingUnit = p.getId(CgmesNames.GENERATING_UNIT);
        if (generatingUnit != null) {
            g.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.GENERATING_UNIT, generatingUnit);
        }
        addSpecificGeneratingUnitProperties(g, p);
    }

    private static void addSpecificGeneratingUnitProperties(Generator generator, PropertyBag p) {
        // Default targetP from initial P defined in EQ GeneratingUnit. Removed since CGMES 3.0
        String initialP = p.getLocal(CgmesNames.INITIAL_P);
        if (initialP != null) {
            generator.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.INITIAL_P, initialP);
        }
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

    public static void update(Generator generator, PropertyBag cgmesData, Context context) {
        updateTerminals(generator, context, generator.getTerminal());

        int referencePriority = cgmesData.asInt("referencePriority", 0);
        if (referencePriority > 0) {
            ReferencePriority.set(generator, referencePriority);
        }

        double targetP = getInitialP(generator, 0.0);
        double targetQ = 0.0;
        PowerFlow updatedPowerFlow = updatedPowerFlow(generator, cgmesData, context);
        if (updatedPowerFlow.defined()) {
            targetP = -updatedPowerFlow.p();
            targetQ = -updatedPowerFlow.q();
        }
        generator.setTargetP(targetP).setTargetQ(targetQ);

        String generatingUnitId = generator.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.GENERATING_UNIT);
        if (generatingUnitId != null) {
            updateGeneratingUnit(generator, generatingUnitId, context);
        }

        String operatingMode = cgmesData.getLocal("operatingMode");
        if (operatingMode != null) {
            generator.setProperty(Conversion.PROPERTY_CGMES_SYNCHRONOUS_MACHINE_OPERATING_MODE, operatingMode.replace("SynchronousMachineOperatingMode.", ""));
        }
        boolean controlEnabled = cgmesData.asBoolean("controlEnabled", false);
        updateRegulatingControl(generator, controlEnabled, context);
    }

    private static double getInitialP(Generator generator, double defaultValue) {
        String initialP = generator.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.INITIAL_P);
        return initialP != null ? Double.parseDouble(initialP) : defaultValue;
    }

    private static void updateGeneratingUnit(Generator generator, String generatingUnitId, Context context) {
        findCgmesGeneratingUnit(generatingUnitId, context).ifPresent(generatingUnit -> {
            double normalPF = generatingUnit.asDouble(CgmesNames.NORMAL_PF);
            if (!Double.isNaN(normalPF)) {
                updateNormalPF(generator, normalPF, context);
            }
        });
    }

    private static Optional<PropertyBag> findCgmesGeneratingUnit(String generatingUnitId, Context context) {
        return generatingUnitId != null ? Optional.ofNullable(context.generatingUnit(generatingUnitId)) : Optional.empty();
    }

    private static void updateNormalPF(Generator generator, double normalPF, Context context) {
        ActivePowerControl<Generator> activePowerControl = generator.getExtension(ActivePowerControl.class);
        if (activePowerControl != null) {
            activePowerControl.setParticipationFactor(normalPF);
        } else if (context.config().createActivePowerControlExtension()) {
            generator.newExtension(ActivePowerControlAdder.class)
                    .withParticipate(true)
                    .withParticipationFactor(normalPF)
                    .add();
        } else {
            generator.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.NORMAL_PF, String.valueOf(normalPF));
        }
    }
}
