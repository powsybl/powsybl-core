//package com.powsybl.cgmes.conversion.update.elements14;
//
//import java.util.Iterator;
//import java.util.UUID;
//
//import com.google.common.collect.ArrayListMultimap;
//import com.google.common.collect.Multimap;
//import com.powsybl.cgmes.conversion.update.CgmesPredicateDetails;
//import com.powsybl.cgmes.conversion.update.ConversionMapper;
//import com.powsybl.cgmes.conversion.update.IidmChange;
//import com.powsybl.cgmes.model.CgmesModel;
//import com.powsybl.iidm.network.VoltageLevel;
//import com.powsybl.triplestore.api.PropertyBag;
//import com.powsybl.triplestore.api.PropertyBags;
//
//public class VoltageLevelToVoltageLevel implements ConversionMapper {
//
//    public VoltageLevelToVoltageLevel(IidmChange change, CgmesModel cgmes) {
//        this.change = change;
//        this.cgmes = cgmes;
//        this.baseVoltageId = getBaseVoltageId();
//    }
//
//    @Override
//    public Multimap<String, CgmesPredicateDetails> converter() {
//
//        final Multimap<String, CgmesPredicateDetails> map = ArrayListMultimap.create();
//        VoltageLevel newVoltageLevel = (VoltageLevel) change.getIdentifiable();
//
//        map.put("rdfType", new CgmesPredicateDetails("rdf:type", "_EQ", false, "cim:VoltageLevel"));
//
//        String name = newVoltageLevel.getName();
//        if (name != null) {
//            map.put("name", new CgmesPredicateDetails("cim:IdentifiedObject.name", "_EQ", false, name));
//        }
//
//        double highVoltageLimit = newVoltageLevel.getHighVoltageLimit();
//        if (!String.valueOf(highVoltageLimit).equals("NaN")) {
//            map.put("highVoltageLimit", new CgmesPredicateDetails("cim:VoltageLevel.highVoltageLimit", "_EQ", false,
//                String.valueOf(highVoltageLimit)));
//        }
//
//        double lowVoltageLimit = newVoltageLevel.getLowVoltageLimit();
//        if (!String.valueOf(lowVoltageLimit).equals("NaN")) {
//            map.put("lowVoltageLimit", new CgmesPredicateDetails("cim:VoltageLevel.lowVoltageLimit", "_EQ", false,
//                String.valueOf(lowVoltageLimit)));
//        }
//
//        String substationId = newVoltageLevel.getSubstation().getId();
//        if (substationId != null) {
//            map.put("Substation", new CgmesPredicateDetails(
//                "cim:VoltageLevel.MemberOf_Substation", "_EQ", true, substationId));
//        }
//
//        map.put("voltageLevelBaseVoltage", new CgmesPredicateDetails(
//            "cim:VoltageLevel.BaseVoltage", "_EQ", true, baseVoltageId));
//        /**
//         * Create BaseVoltage element
//         */
//        map.put("rdfTypeBV", new CgmesPredicateDetails("rdf:type", "_EQ", false, "cim:BaseVoltage", baseVoltageId));
//
//        map.put("nameBV", new CgmesPredicateDetails("cim:IdentifiedObject.name", "_EQ", false, name.concat("_BV"),
//            baseVoltageId));
//
//        double nominalVoltage = newVoltageLevel.getNominalV();
//        if (!String.valueOf(nominalVoltage).equals("NaN")) {
//            map.put("nominalV", new CgmesPredicateDetails("cim:BaseVoltage.nominalVoltage", "_EQ", false,
//                String.valueOf(nominalVoltage), baseVoltageId));
//        }
//        return map;
//    }
//
//    /**
//     * Check if BaseVoltage element already exists in grid, if yes - returns its id,
//     * otherwise new id if new element is created
//     *
//     * @return the base voltage id
//     */
//    private String getBaseVoltageId() {
//        String currId = this.change.getIdentifiableId();
//        PropertyBags voltageLevels = cgmes.voltageLevels();
//        Iterator i = voltageLevels.iterator();
//        while (i.hasNext()) {
//            PropertyBag pb = (PropertyBag) i.next();
//            if (pb.getId("VoltageLevel").equals(currId)) {
//                return pb.getId("BaseVoltage");
//            } else {
//                continue;
//            }
//        }
//        return UUID.randomUUID().toString();
//    }
//
//    private String baseVoltageId;
//    private IidmChange change;
//    private CgmesModel cgmes;
//
//}
