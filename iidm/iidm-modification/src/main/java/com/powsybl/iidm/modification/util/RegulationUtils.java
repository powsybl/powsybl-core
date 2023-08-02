package com.powsybl.iidm.modification.util;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ShuntCompensator;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.stream.Stream;

public final class RegulationUtils {
    private RegulationUtils() {
    }

    public static Stream<Generator> getRegulatingGenerators(Network network, Bus regulatingBus) {
        return network.getGeneratorStream().filter(Generator::isVoltageRegulatorOn)
            .filter(g -> regulatingBus.equals(g.getRegulatingTerminal().getBusView().getBus()));
    }

    public static Stream<ShuntCompensator> getRegulatingShuntCompensators(Network network, Bus regulatingBus) {
        return network.getShuntCompensatorStream().filter(ShuntCompensator::isVoltageRegulatorOn)
            .filter(shunt -> regulatingBus.equals(shunt.getRegulatingTerminal().getBusView().getBus()));
    }

    private static OptionalDouble getTargetVForRegulatingElement(Network network, Bus regulatingBus, String shuntId,
                                                                 String generatorId) {
        Optional<Double> optDouble = getRegulatingShuntCompensators(network, regulatingBus).filter(
                sc -> !sc.getId().equals(shuntId)).map(ShuntCompensator::getTargetV).findFirst()
            .or(() -> getRegulatingGenerators(network, regulatingBus).filter(
                generator -> !generator.getId().equals(generatorId)).map(Generator::getTargetV).findFirst())
            .or(() -> Double.isNaN(regulatingBus.getV()) ? Optional.empty() : Optional.of(regulatingBus.getV()));
        return optDouble.isPresent() ? OptionalDouble.of(optDouble.get()) : OptionalDouble.empty();
    }

    public static OptionalDouble getTargetVForRegulatingShunt(Network network, Bus regulatingBus, String shuntId) {
        return getTargetVForRegulatingElement(network, regulatingBus, shuntId, null);
    }

    public static OptionalDouble getTargetVForRegulatingGenerator(Network network, Bus regulatingBus,
                                                                  String generatorId) {
        return getTargetVForRegulatingElement(network, regulatingBus, null, generatorId);
    }

    public static OptionalDouble getTargetVForRegulatingShunt(ShuntCompensator shuntCompensator) {
        Bus regulatingBus = shuntCompensator.getRegulatingTerminal().getBusView().getBus();
        if (regulatingBus != null) {
            return RegulationUtils.getTargetVForRegulatingShunt(shuntCompensator.getNetwork(), regulatingBus,
                shuntCompensator.getId());
        }
        return OptionalDouble.empty();
    }

    public static OptionalDouble getTargetVForRegulatingGenerator(Generator generator) {
        Bus regulatingBus = generator.getRegulatingTerminal().getBusView().getBus();
        if (regulatingBus != null) {
            return RegulationUtils.getTargetVForRegulatingGenerator(generator.getNetwork(), regulatingBus,
                generator.getId());
        }
        return OptionalDouble.empty();
    }
}
