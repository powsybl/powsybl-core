package com.powsybl.cgmes.update.elements16;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import com.powsybl.cgmes.update.IidmChange;
import com.powsybl.cgmes.update.IidmToCgmes16;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.update.CgmesPredicateDetails;
import com.powsybl.cgmes.update.ConversionMapper;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

public class VoltageLevelToVoltageLevel extends IidmToCgmes16 implements ConversionMapper {

    public VoltageLevelToVoltageLevel(IidmChange change, CgmesModel cgmes) {
        super(change, cgmes);
    }

    @Override
    public Map<String, Object> mapIidmToCgmesPredicates() {
        return Collections.unmodifiableMap(Stream.of(
            entry("name", new CgmesPredicateDetails("cim:IdentifiedObject.name", "_EQ", false)),
            entry("highVoltageLimit", new CgmesPredicateDetails("cim:VoltageLevel.highVoltageLimit", "_EQ", false)),
            entry("lowVoltageLimit", new CgmesPredicateDetails("cim:VoltageLevel.lowVoltageLimit", "_EQ", false)),
            entry("Substation", new CgmesPredicateDetails("cim:VoltageLevel.MemberOf_Substation", "_EQ", true)),
            entry("nominalV", new CgmesPredicateDetails("cim:BaseVoltage.nominalVoltage", "_EQ", false, baseVoltageId)))
            .collect(entriesToMap()));
    }

    @Override
    public Map<CgmesPredicateDetails, String> getAllCgmesDetailsOnCreate() {

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

        CgmesPredicateDetails voltageLevelBaseVoltage = new CgmesPredicateDetails("cim:VoltageLevel.BaseVoltage", "_EQ",
            true);
        allCgmesDetails.put(voltageLevelBaseVoltage, baseVoltageId);

        /**
         * Create BaseVoltage element
         */
        CgmesPredicateDetails rdfTypeBV = new CgmesPredicateDetails("rdf:type", "_EQ", false, baseVoltageId);
        allCgmesDetails.put(rdfTypeBV, "cim:BaseVoltage");

        CgmesPredicateDetails nameBV = new CgmesPredicateDetails("cim:IdentifiedObject.name", "_EQ", false,
            baseVoltageId);
        allCgmesDetails.put(nameBV, name.concat("_BV"));

        double nominalVoltage = newVoltageLevel.getNominalV();
        if (!String.valueOf(nominalVoltage).equals("NaN")) {
            allCgmesDetails.put((CgmesPredicateDetails) mapIidmToCgmesPredicates().get("nominalV"),
                String.valueOf(nominalVoltage));
        }

        return allCgmesDetails;
    }

    /**
     * Check if BaseVoltage element already exists in grid, if yes - returns the id
     *
     * @return the base voltage id
     */
    private String getBaseVoltageId() {

        PropertyBags voltageLevels = cgmes.voltageLevels();
        Iterator i = voltageLevels.iterator();
        while (i.hasNext()) {
            PropertyBag pb = (PropertyBag) i.next();
            if (pb.getId("VoltageLevel").equals(change.getIdentifiableId())) {
                return pb.getId("BaseVoltage");
            } else {
                continue;
            }
        }
        return UUID.randomUUID().toString();
    }

    private String baseVoltageId = getBaseVoltageId();
}
