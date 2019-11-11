package com.powsybl.cgmes.conversion.update.elements14;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableMap;
import com.powsybl.cgmes.conversion.ConversionException;
import com.powsybl.cgmes.conversion.update.AbstractIidmToCgmes;
import com.powsybl.cgmes.conversion.update.CgmesPredicateDetails;
import com.powsybl.cgmes.conversion.update.IidmChange;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.iidm.network.Load;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

public class LoadToEnergyConsumer extends AbstractIidmToCgmes {

    private LoadToEnergyConsumer() {
    }

    public static Map<String, CgmesPredicateDetails> mapIidmAtrribute() {
        return Collections.unmodifiableMap(Stream.of(
            entry("p0", new CgmesPredicateDetails("cim:LoadResponseCharacteristic.pConstantPower", "_EQ", false,
                value, newSubject)),
            entry("q0", new CgmesPredicateDetails("cim:LoadResponseCharacteristic.qConstantPower", "_EQ", false,
                value, newSubject)),
            entry("exponentModelLRC", new CgmesPredicateDetails(
                "cim:LoadResponseCharacteristic.exponentModel", "_EQ", false, "false", newSubject)),
            entry("energyConsumerLoadResponse", new CgmesPredicateDetails("cim:EnergyConsumer.LoadResponse",
                "_EQ", true, value)))
            .collect(entriesToMap()));
    }

    static Map<String, String> getValues(IidmChange change, CgmesModel cgmes) {
        if (!(change.getIdentifiable() instanceof Load)) {
            throw new ConversionException("Cannot cast the identifiable into the element");
        }
        Load load = (Load) change.getIdentifiable();
        return ImmutableMap.of(
            "name", load.getName(),
            "voltageLevelId", load.getTerminal().getVoltageLevel().getId(),
            "p0", String.valueOf(load.getP0()),
            "q0", String.valueOf(load.getQ0()),
            "newSubject", getLoadResponseCharacteristicId(change.getIdentifiableId(), cgmes.energyConsumers()));
    }

    /**
     * Check if EnergyConsumer.LoadResponse element already exists in grid, if yes -
     * returns the id
     *
     */
    static String getLoadResponseCharacteristicId(String currId, PropertyBags energyConsumers) {
        for (PropertyBag pb : energyConsumers) {
            if (pb.getId("EnergyConsumer").equals(currId)) {
                return pb.getId("LoadResponse");
            } else {
                continue;
            }
        }
        return UUID.randomUUID().toString();
    }
}
