package com.powsybl.ucte.converter.export.elements;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.ReactiveLimits;
import com.powsybl.ucte.network.UcteNode;
import com.powsybl.ucte.network.UcteNodeTypeCode;
import com.powsybl.ucte.network.UctePowerPlantType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.powsybl.ucte.converter.util.UcteConverterConstants.DEFAULT_POWER_LIMIT;
import static com.powsybl.ucte.converter.util.UcteConverterConstants.POWER_PLANT_TYPE_PROPERTY_KEY;

/**
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
public final class GeneratorUcteExport {

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
        List<Double> voltageReferences = new ArrayList<>();
        List<Double> minPs = new ArrayList<>();
        List<Double> maxPs = new ArrayList<>();
        List<Double> minQs = new ArrayList<>();
        List<Double> maxQs = new ArrayList<>();

        UcteNodeTypeCode nodeType = UcteNodeTypeCode.PQ;
        List<UctePowerPlantType> powerPlantTypes = new ArrayList<>();

        for (Generator generator : bus.getGenerators()) {
            if (!Double.isNaN(generator.getTargetP())) {
                activePowerGeneration += generator.getTargetP();
            }
            if (!Double.isNaN(generator.getTargetQ())) {
                reactivePowerGeneration += generator.getTargetQ();
            }
            if (!Double.isNaN(generator.getTargetV())) {
                voltageReferences.add(getTargetV(generator));
            }
            if (generator.isVoltageRegulatorOn()) {
                // If one of the generators regulates voltage, then the node is a PU node.
                nodeType = UcteNodeTypeCode.PU;
            }
            minPs.add(generator.getMinP());
            maxPs.add(generator.getMaxP());
            ReactiveLimits reactiveLimits = generator.getReactiveLimits();
            minQs.add(reactiveLimits.getMinQ(activePowerGeneration));
            maxQs.add(reactiveLimits.getMaxQ(activePowerGeneration));
            powerPlantTypes.add(energySourceToUctePowerPlantType(generator));
        }

        ucteNode.setActivePowerGeneration(activePowerGeneration != 0 ? -activePowerGeneration : 0);
        ucteNode.setReactivePowerGeneration(reactivePowerGeneration != 0 ? -reactivePowerGeneration : 0);
        ucteNode.setVoltageReference(getVoltageReference(voltageReferences, bus.getVoltageLevel().getNominalV()));
        ucteNode.setPowerPlantType(getUctePowerPlantType(powerPlantTypes));
        ucteNode.setTypeCode(nodeType);
        // for minP, maxP, minQ, maxQ, sum the values on each generator unless it is equal to Double.MAX_VALUE or DEFAULT_POWER_LIMIT (equivalent to undefined)
        // Default value if all limits are undefined is Double.NaN because these fields are optional in UCTE
        // TODO: remove the DEFAULT_POWER_LIMIT in UCTEImporter
        Double minP = minPs.stream().filter(minp -> minp != -Double.MAX_VALUE && minp != -DEFAULT_POWER_LIMIT).reduce(Double::sum).orElse(Double.NaN);
        Double maxP = maxPs.stream().filter(maxp -> maxp != Double.MAX_VALUE && maxp != DEFAULT_POWER_LIMIT).reduce(Double::sum).orElse(Double.NaN);
        Double minQ = minQs.stream().filter(minq -> minq != -Double.MAX_VALUE && minq != -DEFAULT_POWER_LIMIT).reduce(Double::sum).orElse(Double.NaN);
        Double maxQ = maxQs.stream().filter(maxq -> maxq != Double.MAX_VALUE && maxq != DEFAULT_POWER_LIMIT).reduce(Double::sum).orElse(Double.NaN);
        ucteNode.setMinimumPermissibleActivePowerGeneration(-minP);
        ucteNode.setMaximumPermissibleActivePowerGeneration(-maxP);
        ucteNode.setMinimumPermissibleReactivePowerGeneration(-minQ);
        ucteNode.setMaximumPermissibleReactivePowerGeneration(-maxQ);
    }

    private static UctePowerPlantType getUctePowerPlantType(List<UctePowerPlantType> powerPlantTypes) {
        if (powerPlantTypes.stream().distinct().count() > 1) {
            // If all generators do not have the same UctePowerPlantType, then set it to F.
            return UctePowerPlantType.F;
        } else if (powerPlantTypes.stream().distinct().count() == 1) {
            return powerPlantTypes.get(0);
        }
        // If powerPlantTypes is empty, it means that no generator was found on the bus, so we don't fill this field out
        return null;
    }

    private static double getVoltageReference(List<Double> voltageReferences, double nominalV) {
        return voltageReferences.stream().filter(v -> !Double.isNaN(v))
            .distinct()
            .min(Comparator.comparingDouble(v -> Math.abs(v - nominalV))) // If all generators do not have the same targetV, take the one closest to the nominalV of the VL
            .orElse(Double.NaN);
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
