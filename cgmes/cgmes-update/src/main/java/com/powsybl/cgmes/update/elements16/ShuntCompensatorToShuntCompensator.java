package com.powsybl.cgmes.update.elements16;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import com.powsybl.cgmes.update.CgmesPredicateDetails;
import com.powsybl.cgmes.update.ConversionMapper;
import com.powsybl.cgmes.update.IidmChange;
import com.powsybl.cgmes.update.IidmToCgmes;
import com.powsybl.iidm.network.ShuntCompensator;

public class ShuntCompensatorToShuntCompensator extends IidmToCgmes implements ConversionMapper {

    public ShuntCompensatorToShuntCompensator(IidmChange change) {
        super(change);
    }

    @Override
    public Map<String, Object> mapIidmToCgmesPredicates() {
        return Collections.unmodifiableMap(Stream.of(
            entry("name", new CgmesPredicateDetails("cim:IdentifiedObject.name", "_EQ", false)),
            entry("bPerSection", new CgmesPredicateDetails("cim:LinearShuntCompensator.bPerSection", "_EQ", false)),
            entry("maximumSectionCount",
                new CgmesPredicateDetails("cim:ShuntCompensator.maximumSections", "_EQ", false)))
            .collect(entriesToMap()));
    }

    @Override
    public Map<CgmesPredicateDetails, String> getAllCgmesDetailsOnCreate() {

        Map<CgmesPredicateDetails, String> allCgmesDetails = new HashMap<CgmesPredicateDetails, String>();

        ShuntCompensator newShuntCompensator = (ShuntCompensator) change.getIdentifiable();

        CgmesPredicateDetails rdfType = new CgmesPredicateDetails("rdf:type", "_EQ", false);
        allCgmesDetails.put(rdfType, "cim:LinearShuntCompensator");

        String name = newShuntCompensator.getName();
        if (name != null) {
            allCgmesDetails.put((CgmesPredicateDetails) mapIidmToCgmesPredicates().get("name"),
                name);
        }
        
        String voltageLevelId = newShuntCompensator.getTerminal().getVoltageLevel().getId();
        CgmesPredicateDetails equipmentContainer = new CgmesPredicateDetails(
            "cim:Equipment.EquipmentContainer", "_EQ", true);
        if (!voltageLevelId.equals("NaN")) {
            allCgmesDetails.put(equipmentContainer, voltageLevelId);
        }

        double bPerSection = newShuntCompensator.getbPerSection();
        allCgmesDetails.put((CgmesPredicateDetails) mapIidmToCgmesPredicates().get("bPerSection"),
            String.valueOf(bPerSection));

        double maximumSectionCount = newShuntCompensator.getbPerSection();
        allCgmesDetails.put((CgmesPredicateDetails) mapIidmToCgmesPredicates().get("maximumSectionCount"),
            String.valueOf(maximumSectionCount));

        return allCgmesDetails;
    }

}
