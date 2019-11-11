package com.powsybl.cgmes.conversion.update.elements16;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import com.powsybl.cgmes.conversion.ConversionException;
import com.powsybl.cgmes.conversion.update.AbstractIidmToCgmes;
import com.powsybl.cgmes.conversion.update.CgmesPredicateDetails;
import com.powsybl.cgmes.conversion.update.ConversionMapper;
import com.powsybl.cgmes.conversion.update.IidmChange;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.iidm.network.Load;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

public class LoadToEnergyConsumer extends AbstractIidmToCgmes implements ConversionMapper {

    private LoadToEnergyConsumer(){
    }

    public static Map<String, CgmesPredicateDetails> converter() {
        return  Collections.unmodifiableMap(Stream.of(
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
        return ImmutableMap.of(
            "rdfType", getRdfType(change.getIdentifiableId(), cgmes.energyConsumers()),
            "name", load.getName(),
            "voltageLevelId", load.getTerminal().getVoltageLevel().getId(),
            "p0", String.valueOf(load.getP0()),
            "q0", String.valueOf(load.getQ0()));
    }

    //TODO elena, what is default EnergyConsumer type?
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
