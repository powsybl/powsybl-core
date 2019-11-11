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
import com.powsybl.iidm.network.ShuntCompensator;

public class ShuntCompensatorToShuntCompensator extends AbstractIidmToCgmes {

    private ShuntCompensatorToShuntCompensator() {
    }

    public static Map<String, CgmesPredicateDetails> mapIidmAtrribute() {
        return Collections.unmodifiableMap(Stream.of(
            entry("equipmentContainer", new CgmesPredicateDetails("cim:Equipment.EquipmentContainer", "_EQ", true,  value)),
            entry("bPerSection", new CgmesPredicateDetails("cim:LinearShuntCompensator.bPerSection", "_EQ", false, value)),
            entry("maximumSectionCount", new CgmesPredicateDetails("cim:ShuntCompensator.maximumSections", "_EQ", false, value)),
            entry("nomU", new CgmesPredicateDetails("cim:ShuntCompensator.nomU", "_EQ", false, value)),
            entry("normalSections", new CgmesPredicateDetails("cim:ShuntCompensator.normalSections", "_EQ", false, value)))
            .collect(entriesToMap()));
    }

    static Map<String, String> getValues(IidmChange change, CgmesModel cgmes) {
        if (!(change.getIdentifiable() instanceof ShuntCompensator)) {
            throw new ConversionException("Cannot cast the identifiable into the element");
        }
        ShuntCompensator shunt = (ShuntCompensator) change.getIdentifiable();
        return ImmutableMap.<String, String>builder()
            .put("rdfType", "cim:LinearShuntCompensator")
            .put("name", shunt.getName())
            .put("voltageLevelId", shunt.getTerminal().getVoltageLevel().getId())
            .put("bPerSection", String.valueOf(shunt.getbPerSection()))
            .put("maximumSectionCount", String.valueOf(shunt.getMaximumSectionCount()))
            .put("nomU", shunt.getName())
            .put("nominalVoltage", String.valueOf(shunt.getTerminal().getVoltageLevel().getNominalV()))
            .put("normalSections", String.valueOf(shunt.getCurrentSectionCount()))
            .build();
    }
}
