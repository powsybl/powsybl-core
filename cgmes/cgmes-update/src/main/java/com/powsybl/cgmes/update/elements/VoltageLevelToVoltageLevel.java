package com.powsybl.cgmes.update.elements;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import com.powsybl.cgmes.update.IidmChange;
import com.powsybl.cgmes.update.IidmToCgmes;
import com.powsybl.cgmes.update.CgmesPredicateDetails;
import com.powsybl.cgmes.update.ConversionMapper;
import com.powsybl.iidm.network.VoltageLevel;

public class VoltageLevelToVoltageLevel extends IidmToCgmes implements ConversionMapper {

    public VoltageLevelToVoltageLevel(IidmChange change) {
        super(change);
    }

    public static Map<String, Object> mapIidmToCgmesPredicates() {
        return Collections.unmodifiableMap(Stream.of(
            entry("name", new CgmesPredicateDetails("cim:IdentifiedObject.name", "_EQ", false)),
            entry("BaseVoltage", new CgmesPredicateDetails("cim:VoltageLevel.BaseVoltage", "_EQ", true)),
            entry("highVoltageLimit", new CgmesPredicateDetails("cim:VoltageLevel.highVoltageLimit", "_EQ", false)),
            entry("lowVoltageLimit", new CgmesPredicateDetails("cim:VoltageLevel.lowVoltageLimit", "_EQ", false)),
            entry("Substation", new CgmesPredicateDetails("cim:VoltageLevel.MemberOf_Substation", "_EQ", true)),
            entry("nominalV", new CgmesPredicateDetails("cim:BaseVoltage.nominalVoltage", "_EQ", false)))
            .collect(entriesToMap()));
    }

    @Override
    public Map<CgmesPredicateDetails, String> getAllCgmesDetails() {

        Map<CgmesPredicateDetails, String> allCgmesDetails = new HashMap<CgmesPredicateDetails, String>();

        VoltageLevel newVoltageLevel = (VoltageLevel) change.getIdentifiable();

        CgmesPredicateDetails rdfType = new CgmesPredicateDetails("rdf:type", "_EQ", false);
        allCgmesDetails.put(rdfType, "cim:VoltageLevel");

        String name = newVoltageLevel.getName();
        if (name != null) {
            allCgmesDetails.put((CgmesPredicateDetails) mapIidmToCgmesPredicates().get("name"),
                name);
        }

        double highVoltageLimit = newVoltageLevel.getHighVoltageLimit();
        if (!String.valueOf(highVoltageLimit).equals("NaN")) {
            allCgmesDetails.put((CgmesPredicateDetails) mapIidmToCgmesPredicates().get("highVoltageLimit"),
                String.valueOf(highVoltageLimit));
        }

        double lowVoltageLimit = newVoltageLevel.getLowVoltageLimit();
        if (!String.valueOf(lowVoltageLimit).equals("NaN")) {
            allCgmesDetails.put((CgmesPredicateDetails) mapIidmToCgmesPredicates().get("lowVoltageLimit"),
                String.valueOf(lowVoltageLimit));
        }

        String substation = newVoltageLevel.getSubstation().getId();
        if (substation != null) {
            allCgmesDetails.put((CgmesPredicateDetails) mapIidmToCgmesPredicates().get("Substation"),
                substation.toString());
        }

        return allCgmesDetails;
    }

}
