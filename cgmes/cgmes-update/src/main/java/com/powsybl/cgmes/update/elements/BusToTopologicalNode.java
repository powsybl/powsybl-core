package com.powsybl.cgmes.update.elements;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.update.CgmesPredicateDetails;
import com.powsybl.cgmes.update.ConversionMapper;
import com.powsybl.cgmes.update.IidmChange;
import com.powsybl.cgmes.update.IidmToCgmes;
import com.powsybl.iidm.network.Bus;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

public class BusToTopologicalNode extends IidmToCgmes implements ConversionMapper {

    public BusToTopologicalNode(IidmChange change, CgmesModel cgmes) {
        super(change,cgmes);
    }

    @Override
    public Map<String, Object> mapIidmToCgmesPredicates() {
        return Collections.unmodifiableMap(Stream.of(
            entry("name", new CgmesPredicateDetails("cim:IdentifiedObject.name", "_TP", false)))
            .collect(entriesToMap()));
    }

    @Override
    public Map<CgmesPredicateDetails, String> getAllCgmesDetailsOnCreate() {

        Map<CgmesPredicateDetails, String> allCgmesDetails = new HashMap<CgmesPredicateDetails, String>();
        Bus newBus = (Bus) change.getIdentifiable();

        CgmesPredicateDetails rdfType = new CgmesPredicateDetails("rdf:type", "_TP", false);
        allCgmesDetails.put(rdfType, "cim:TopologicalNode");

        String name = newBus.getName();
        if (name != null) {
            allCgmesDetails.put((CgmesPredicateDetails) mapIidmToCgmesPredicates().get("name"),
                name);
        }

        String baseVoltageId = getBaseVoltageId(newBus);
        CgmesPredicateDetails baseVoltage = new CgmesPredicateDetails(
            "cim:TopologicalNode.BaseVoltage", "_EQ", true);
        if (!baseVoltageId.equals("NaN")) {
            allCgmesDetails.put(baseVoltage, baseVoltageId);
        }
        return allCgmesDetails;
    }

    private String getBaseVoltageId(Bus bus) {
        String voltageLevelId = bus.getVoltageLevel().getId();
        PropertyBags voltageLevels = cgmes.voltageLevels();
        Iterator i = voltageLevels.iterator();
        while (i.hasNext()) {
            PropertyBag pb = (PropertyBag) i.next();
            if (pb.getId("VoltageLevel").equals(voltageLevelId)) {
                return pb.getId("BaseVoltage");
            } else {
                continue;
            }
        }
        return UUID.randomUUID().toString();
    }

}
