package com.powsybl.cgmes.conversion.update.elements16;

import java.util.Collections;
import java.util.Map;
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
            entry("p0", new CgmesPredicateDetails("cim:EnergyConsumer.p", "_SSH", false, value)),
            entry("p", new CgmesPredicateDetails("cim:EnergyConsumer.p", "_SSH", false, value)),
            entry("q0", new CgmesPredicateDetails("cim:EnergyConsumer.q", "_SSH", false, value)),
            entry("q", new CgmesPredicateDetails("cim:EnergyConsumer.q", "_SSH", false, value)),
            entry("equipmentContainer",
                new CgmesPredicateDetails("cim:Equipment.EquipmentContainer", "_EQ", true, value)))
            .collect(entriesToMap()));
    }

    static Map<String, String> getValues(IidmChange change, CgmesModel cgmes) {
        if (!(change.getIdentifiable() instanceof Load)) {
            throw new ConversionException("Cannot cast the identifiable into the element");
        }
        Load load = (Load) change.getIdentifiable();
        return ImmutableMap.<String, String>builder()
            .put("rdfType", getRdfType(change.getIdentifiableId(), cgmes.energyConsumers()))
            .put("name", load.getName())
            .put("voltageLevelId", load.getTerminal().getVoltageLevel().getId())
            .put("p0", String.valueOf(load.getP0()))
            .put("q0", String.valueOf(load.getQ0()))
            .build();
    }

    // TODO elena, what is default EnergyConsumer type?
    static String getRdfType(String currId, PropertyBags energyConsumers) {
        for (PropertyBag pb : energyConsumers) {
            if (pb.getId("EnergyConsumer").equals(currId)) {
                return pb.getLocal("type");
            } else {
                continue;
            }
        }
        return null;
    }
}
