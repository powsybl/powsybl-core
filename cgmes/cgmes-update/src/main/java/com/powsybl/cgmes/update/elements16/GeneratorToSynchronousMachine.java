package com.powsybl.cgmes.update.elements16;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import com.powsybl.cgmes.update.IidmChange;
import com.powsybl.cgmes.update.IidmToCgmes;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.update.CgmesPredicateDetails;
import com.powsybl.cgmes.update.ConversionMapper;
import com.powsybl.iidm.network.Generator;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

public class GeneratorToSynchronousMachine extends IidmToCgmes implements ConversionMapper {
    public GeneratorToSynchronousMachine(IidmChange change, CgmesModel cgmes) {
        super(change, cgmes);
    }

    @Override
    public Map<String, Object> mapIidmToCgmesPredicates() {
        return Collections.unmodifiableMap(Stream.of(
            entry("name", new CgmesPredicateDetails("cim:IdentifiedObject.name", "_EQ", false)),
            entry("minQ", new CgmesPredicateDetails("cim:SynchronousMachine.minQ", "_EQ", false)),
            entry("maxQ", new CgmesPredicateDetails("cim:SynchronousMachine.maxQ", "_EQ", false)),
            entry("qPercent", new CgmesPredicateDetails("cim:SynchronousMachine.qPercent", "_EQ", false)),
            entry("ratedS", new CgmesPredicateDetails("cim:RotatingMachine.ratedS", "_EQ", false)),
            entry("targetP", new CgmesPredicateDetails("cim:GeneratingUnit.initialP", "_EQ", false, generatingUnitId)),
            entry("minP",
                new CgmesPredicateDetails("cim:GeneratingUnit.minOperatingP", "_EQ", false, generatingUnitId)),
            entry("maxP",
                new CgmesPredicateDetails("cim:GeneratingUnit.maxOperatingP", "_EQ", false, generatingUnitId)))
            .collect(entriesToMap()));
    }

    @Override
    public Map<CgmesPredicateDetails, String> getAllCgmesDetailsOnCreate() {

        Map<CgmesPredicateDetails, String> allCgmesDetails = new HashMap<CgmesPredicateDetails, String>();

        Generator newGenerator = (Generator) change.getIdentifiable();

        CgmesPredicateDetails rdfType = new CgmesPredicateDetails("rdf:type", "_EQ", false);
        allCgmesDetails.put(rdfType, "cim:SynchronousMachine");

        String name = newGenerator.getName();
        if (name != null) {
            allCgmesDetails.put((CgmesPredicateDetails) mapIidmToCgmesPredicates().get("name"),
                name);
        }

        double ratedS = newGenerator.getRatedS();
        if (!String.valueOf(ratedS).equals("NaN")) {
            allCgmesDetails.put((CgmesPredicateDetails) mapIidmToCgmesPredicates().get("ratedS"),
                String.valueOf(ratedS));
        }

        CgmesPredicateDetails generatingUnit = new CgmesPredicateDetails(
            "cim:RotatingMachine.GeneratingUnit", "_EQ", true);
        allCgmesDetails.put(generatingUnit, generatingUnitId);

        /**
         * Create GeneratingUnit element
         */
        CgmesPredicateDetails rdfTypeGU = new CgmesPredicateDetails("rdf:type", "_EQ", false,
            generatingUnitId);
        allCgmesDetails.put(rdfTypeGU, "cim:ThermalGeneratingUnit");

        CgmesPredicateDetails nameGU = new CgmesPredicateDetails("cim:IdentifiedObject.name", "_EQ", false,
            generatingUnitId);
        allCgmesDetails.put(nameGU, name.concat("_GU"));

        double targetP = newGenerator.getTargetP();
        if (!String.valueOf(targetP).equals("NaN")) {
            allCgmesDetails.put((CgmesPredicateDetails) mapIidmToCgmesPredicates().get("targetP"),
                String.valueOf(targetP));
        }

        double maxP = newGenerator.getMaxP();
        if (!String.valueOf(maxP).equals("NaN")) {
            allCgmesDetails.put((CgmesPredicateDetails) mapIidmToCgmesPredicates().get("maxP"),
                String.valueOf(maxP));
        }

        double minP = newGenerator.getMinP();
        if (!String.valueOf(minP).equals("NaN")) {
            allCgmesDetails.put((CgmesPredicateDetails) mapIidmToCgmesPredicates().get("minP"),
                String.valueOf(minP));
        }

        return allCgmesDetails;
    }

    /**
     * Check if GeneratingUnit element already exists in grid, if yes - returns the
     * id
     *
     */
    private String getGeneratingUnitId() {

        PropertyBags synchronousMachines = cgmes.synchronousMachines();
        Iterator i = synchronousMachines.iterator();
        while (i.hasNext()) {
            PropertyBag pb = (PropertyBag) i.next();
            if (pb.getId("SynchronousMachine").equals(change.getIdentifiableId())) {
                return pb.getId("GeneratingUnit");
            } else {
                continue;
            }
        }
        return UUID.randomUUID().toString();
    }

    private String generatingUnitId = getGeneratingUnitId();

}