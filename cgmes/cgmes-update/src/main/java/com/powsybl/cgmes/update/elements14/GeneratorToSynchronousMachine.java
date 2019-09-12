package com.powsybl.cgmes.update.elements14;

import java.util.Iterator;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.update.CgmesPredicateDetails;
import com.powsybl.cgmes.update.ConversionMapper;
import com.powsybl.cgmes.update.IidmChange;
import com.powsybl.iidm.network.Generator;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

public class GeneratorToSynchronousMachine implements ConversionMapper {
    public GeneratorToSynchronousMachine(IidmChange change, CgmesModel cgmes) {
        this.change = change;
        this.cgmes = cgmes;
        this.generatingUnitId = getGeneratingUnitId();
    }

    @Override
    public Multimap<String, CgmesPredicateDetails> mapIidmToCgmesPredicates() {

        final Multimap<String, CgmesPredicateDetails> map = ArrayListMultimap.create();
        Generator newGenerator = (Generator) change.getIdentifiable();

        map.put("rdfType", new CgmesPredicateDetails("rdf:type", "_EQ", false, "cim:SynchronousMachine"));

        String name = newGenerator.getName();
        if (name != null) {
            map.put("name", new CgmesPredicateDetails("cim:IdentifiedObject.name", "_EQ", false, name));
        }

        double ratedS = newGenerator.getRatedS();
        if (!String.valueOf(ratedS).equals("NaN")) {
            map.put("ratedS",
                new CgmesPredicateDetails("cim:SynchronousMachine.ratedS", "_EQ", false, String.valueOf(ratedS)));
        }

        map.put("minQ", new CgmesPredicateDetails("cim:SynchronousMachine.minQ", "_EQ", false, String.valueOf(0.0)));

        map.put("maxQ", new CgmesPredicateDetails("cim:SynchronousMachine.maxQ", "_EQ", false, String.valueOf(0.0)));

        map.put("generatingUnit", new CgmesPredicateDetails(
            "cim:SynchronousMachine.MemberOf_GeneratingUnit", "_EQ", true, generatingUnitId));
        /**
         * Create GeneratingUnit element
         */
        map.put("rdfTypeGU", new CgmesPredicateDetails("rdf:type", "_EQ", false, "cim:GeneratingUnit",
            generatingUnitId));

        if (name != null) {
            map.put("nameGU", new CgmesPredicateDetails("cim:IdentifiedObject.name", "_EQ", false, name.concat("_GU"),
                generatingUnitId));
        }
        double targetP = newGenerator.getTargetP();
        if (!String.valueOf(targetP).equals("NaN")) {
            map.put("targetP", new CgmesPredicateDetails("cim:GeneratingUnit.nominalP", "_EQ", false,
                String.valueOf(targetP), generatingUnitId));
        }

        double maxP = newGenerator.getMaxP();
        if (!String.valueOf(maxP).equals("NaN")) {
            map.put("maxP",
                new CgmesPredicateDetails("cim:GeneratingUnit.maxOperatingP", "_EQ", false, String.valueOf(maxP),
                    generatingUnitId));
        }

        double minP = newGenerator.getMinP();
        if (!String.valueOf(minP).equals("NaN")) {
            map.put("minP",
                new CgmesPredicateDetails("cim:GeneratingUnit.minOperatingP", "_EQ", false, String.valueOf(minP),
                    generatingUnitId));
        }

        return map;
    }

    /**
     * Check if GeneratingUnit element already exists in grid, if yes - returns the
     * id
     *
     */
    private String getGeneratingUnitId() {
        String currId = change.getIdentifiableId();
        PropertyBags synchronousMachines = cgmes.synchronousMachines();
        Iterator i = synchronousMachines.iterator();
        while (i.hasNext()) {
            PropertyBag pb = (PropertyBag) i.next();
            if (pb.getId("SynchronousMachine").equals(currId)) {
                return pb.getId("GeneratingUnit");
            } else {
                continue;
            }
        }
        return currId.concat("_GU");
    }

    private IidmChange change;
    private CgmesModel cgmes;
    private String generatingUnitId;

}
