package com.powsybl.ucte.converter.export.elements;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.ReactiveLimits;
import com.powsybl.ucte.network.UcteNode;
import com.powsybl.ucte.network.UcteNodeTypeCode;
import com.powsybl.ucte.network.UctePowerPlantType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.powsybl.ucte.converter.util.UcteConverterConstants.DEFAULT_POWER_LIMIT;
import static com.powsybl.ucte.converter.util.UcteConverterConstants.POWER_PLANT_TYPE_PROPERTY_KEY;

/**
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
public final class GeneratorUcteExport {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeneratorUcteExport.class);

    private GeneratorUcteExport() {
    }

    /**
     * Initialize the power generation fields from the generators connected to the specified bus.
     *
     * @param ucteNode The UCTE node to fill
     * @param bus The bus the generators are connected to
     */
    public static void convertGenerators(UcteNode ucteNode, Bus bus) {
        double activePowerGeneration = 0;
        double reactivePowerGeneration = 0;

        List<Double> localActiveVoltageReferences = new ArrayList<>();
        List<Double> remoteActiveVoltageReferences = new ArrayList<>();
        List<Double> localInactiveVoltageReferences = new ArrayList<>();
        List<Double> remoteInactiveVoltageReferences = new ArrayList<>();

        List<Double> minPs = new ArrayList<>();
        List<Double> maxPs = new ArrayList<>();
        List<Double> minQs = new ArrayList<>();
        List<Double> maxQs = new ArrayList<>();

        UcteNodeTypeCode nodeType = UcteNodeTypeCode.PQ;
        Set<UctePowerPlantType> powerPlantTypes = new HashSet<>();

        for (Generator generator : bus.getGenerators()) {
            if (!Double.isNaN(generator.getTargetP())) {
                activePowerGeneration -= generator.getTargetP();
                ReactiveLimits reactiveLimits = generator.getReactiveLimits();
                minQs.add(reactiveLimits.getMinQ(generator.getTargetP()));
                maxQs.add(reactiveLimits.getMaxQ(generator.getTargetP()));
            }
            if (!Double.isNaN(generator.getTargetQ())) {
                reactivePowerGeneration -= generator.getTargetQ();
            }
            if (!Double.isNaN(generator.getTargetV())) {
                categorizeVoltageReference(generator, localActiveVoltageReferences, remoteActiveVoltageReferences, localInactiveVoltageReferences, remoteInactiveVoltageReferences);
                if (generator.isVoltageRegulatorOn()) {
                    nodeType = UcteNodeTypeCode.PU; // If one of the generators regulates voltage, then the node is a PU node.
                }
            }
            minPs.add(generator.getMinP());
            maxPs.add(generator.getMaxP());
            powerPlantTypes.add(energySourceToUctePowerPlantType(generator));
        }

        ucteNode.setActivePowerGeneration(activePowerGeneration);
        ucteNode.setReactivePowerGeneration(reactivePowerGeneration);
        ucteNode.setVoltageReference(getVoltageReference(localActiveVoltageReferences, remoteActiveVoltageReferences, localInactiveVoltageReferences, remoteInactiveVoltageReferences, bus.getVoltageLevel().getNominalV()));
        ucteNode.setPowerPlantType(getUctePowerPlantType(powerPlantTypes, bus));
        ucteNode.setTypeCode(nodeType);
        // for minP, maxP, minQ, maxQ, sum the values on each generator unless it is equal to Double.MAX_VALUE or DEFAULT_POWER_LIMIT (equivalent to undefined)
        // Default value if one limit is undefined (MAX_VALUE or DEFAULT_POWER_LIMIT) is Double.NaN because these fields are optional in UCTE
        // TODO: remove the DEFAULT_POWER_LIMIT in UCTEImporter
        ucteNode.setMinimumPermissibleActivePowerGeneration(-computeMinPower(minPs));
        ucteNode.setMaximumPermissibleActivePowerGeneration(-computeMaxPower(maxPs));
        ucteNode.setMinimumPermissibleReactivePowerGeneration(-computeMinPower(minQs));
        ucteNode.setMaximumPermissibleReactivePowerGeneration(-computeMaxPower(maxQs));
    }

    private static void categorizeVoltageReference(Generator generator, List<Double> localActiveVoltageReferences, List<Double> remoteActiveVoltageReferences, List<Double> localInactiveVoltageReferences, List<Double> remoteInactiveVoltageReferences) {
        double targetV = getTargetV(generator);
        boolean isLocalRegulation = generator.getId().equals(generator.getRegulatingTerminal().getConnectable().getId());

        if (isLocalRegulation) {
            if (generator.isVoltageRegulatorOn()) {
                localActiveVoltageReferences.add(targetV);
            } else {
                localInactiveVoltageReferences.add(targetV);
            }
        } else {
            if (generator.isVoltageRegulatorOn()) {
                remoteActiveVoltageReferences.add(targetV);
            } else {
                remoteInactiveVoltageReferences.add(targetV);
            }
        }
    }

    private static double computeMaxPower(List<Double> powers) {
        return powers.isEmpty() || powers.contains(Double.MAX_VALUE) || powers.contains((double) DEFAULT_POWER_LIMIT) ? Double.NaN : powers.stream().reduce(Double::sum).orElse(Double.NaN);
    }

    private static double computeMinPower(List<Double> powers) {
        return powers.isEmpty() || powers.contains(-Double.MAX_VALUE) || powers.contains((double) -DEFAULT_POWER_LIMIT) ? Double.NaN : powers.stream().reduce(Double::sum).orElse(Double.NaN);
    }

    private static UctePowerPlantType getUctePowerPlantType(Set<UctePowerPlantType> powerPlantTypes, Bus bus) {
        if (powerPlantTypes == null || powerPlantTypes.isEmpty()) {
            // If powerPlantTypes is empty, it means that no generator was found on the bus, so we don't fill this field out
            return null;
        }

        if (powerPlantTypes.size() == 1) {
            return powerPlantTypes.iterator().next();
        }

        // If all generators do not have the same UctePowerPlantType, then set it to F.
        LOGGER.info("All the generators connected to bus {} do not have the same EnergySource: UctePowerPlantType of the node is set to 'F'.", bus.getId());
        return UctePowerPlantType.F;
    }

    // Voltage reference depends on the generator connected to the bus, and if the regulation is remote or local
    // Priority is given to:
    //      1. TargetV of generators regulating locally
    //      2. TargetV of generators regulating remotely
    //      3. TargetV of generators with local deactivated regulation
    //      4. TargetV of generators with remote deactivated regulation
    // In any case, the value closest to the nominalV of the voltage level is kept.
    private static double getVoltageReference(List<Double> localActiveVoltageReferences, List<Double> remoteActiveVoltageReferences, List<Double> localInactiveVoltageReferences, List<Double> remoteInactiveVoltageReferences, double nominalV) {
        return findClosestVoltageToNominalV(localActiveVoltageReferences, nominalV)
            .orElseGet(() -> findClosestVoltageToNominalV(remoteActiveVoltageReferences, nominalV)
                .orElseGet(() -> findClosestVoltageToNominalV(localInactiveVoltageReferences, nominalV)
                    .orElseGet(() -> findClosestVoltageToNominalV(remoteInactiveVoltageReferences, nominalV).orElse(Double.NaN))));
    }

    private static Optional<Double> findClosestVoltageToNominalV(List<Double> voltageReferences, double nominalV) {
        return voltageReferences.stream()
            .filter(v -> !Double.isNaN(v))
            .distinct()
            .min(Comparator.comparingDouble(v -> Math.abs(v - nominalV))); // If all generators do not have the same targetV, take the one closest to the nominalV of the VL
    }

    private static double getTargetV(Generator generator) {
        double targetV = generator.getTargetV();
        // If the generator is regulating remotely, take the local targetV
        if (!generator.getId().equals(generator.getRegulatingTerminal().getConnectable().getId())) {
            // Calculate the local targetV which should be the same value in per unit as the remote targetV
            double remoteNominalV = generator.getRegulatingTerminal().getVoltageLevel().getNominalV();
            double localNominalV = generator.getTerminal().getVoltageLevel().getNominalV();
            return targetV * localNominalV / remoteNominalV;
        }
        return targetV;
    }

    private static UctePowerPlantType energySourceToUctePowerPlantType(Generator generator) {
        if (generator.hasProperty(POWER_PLANT_TYPE_PROPERTY_KEY)) {
            return UctePowerPlantType.valueOf(generator.getProperty(POWER_PLANT_TYPE_PROPERTY_KEY));
        }
        return switch (generator.getEnergySource()) {
            case HYDRO -> UctePowerPlantType.H;
            case NUCLEAR -> UctePowerPlantType.N;
            case THERMAL -> UctePowerPlantType.C;
            case WIND -> UctePowerPlantType.W;
            default -> UctePowerPlantType.F;
        };
    }
}
