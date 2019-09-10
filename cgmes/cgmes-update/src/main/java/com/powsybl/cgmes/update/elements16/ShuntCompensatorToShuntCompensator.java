package com.powsybl.cgmes.update.elements16;

import java.util.HashMap;
import java.util.Map;

import com.powsybl.cgmes.update.CgmesPredicateDetails;
import com.powsybl.cgmes.update.ConversionMapper;
import com.powsybl.cgmes.update.IidmChange;
import com.powsybl.cgmes.update.IidmToCgmes16;
import com.powsybl.iidm.network.ShuntCompensator;

public class ShuntCompensatorToShuntCompensator implements ConversionMapper {

    public ShuntCompensatorToShuntCompensator(IidmChange change) {
        this.change = change;
    }

    @Override
    public Map<String, CgmesPredicateDetails> mapIidmToCgmesPredicates() {

        final Map<String, CgmesPredicateDetails> map = new HashMap<>();
        ShuntCompensator newShuntCompensator = (ShuntCompensator) change.getIdentifiable();

        map.put("rdfType", new CgmesPredicateDetails("rdf:type", "_EQ", false, "cim:LinearShuntCompensator"));

        String name = newShuntCompensator.getName();
        if (name != null) {
            map.put("name", new CgmesPredicateDetails("cim:IdentifiedObject.name", "_EQ", false, name));
        }

        String voltageLevelId = newShuntCompensator.getTerminal().getVoltageLevel().getId();
        if (!voltageLevelId.equals("NaN")) {
            map.put("equipmentContainer", new CgmesPredicateDetails(
                "cim:Equipment.EquipmentContainer", "_EQ", true, voltageLevelId));
        }

        double bPerSection = newShuntCompensator.getbPerSection();
        map.put("bPerSection", new CgmesPredicateDetails(
            "cim:LinearShuntCompensator.bPerSection", "_EQ", false, String.valueOf(bPerSection)));

        double maximumSectionCount = newShuntCompensator.getMaximumSectionCount();
        map.put("maximumSectionCount", new CgmesPredicateDetails(
            "cim:ShuntCompensator.maximumSections", "_EQ", false, String.valueOf(maximumSectionCount)));

        double nominalVoltage = newShuntCompensator.getTerminal().getVoltageLevel().getNominalV();
        map.put("nomU", new CgmesPredicateDetails(
            "cim:ShuntCompensator.nomU", "_EQ", false, String.valueOf(nominalVoltage)));

        double currentSectionCount = newShuntCompensator.getCurrentSectionCount();
        map.put("normalSections", new CgmesPredicateDetails(
            "cim:ShuntCompensator.normalSections", "_EQ", false, String.valueOf(currentSectionCount)));

        return map;
    }

    private IidmChange change;
}
