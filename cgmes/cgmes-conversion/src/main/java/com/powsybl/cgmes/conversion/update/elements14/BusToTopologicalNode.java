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
//import com.powsybl.iidm.network.Bus;
//import com.powsybl.triplestore.api.PropertyBag;
//import com.powsybl.triplestore.api.PropertyBags;
//
//public class BusToTopologicalNode implements ConversionMapper {
//
//    public BusToTopologicalNode(IidmChange change, CgmesModel cgmes) {
//        this.change = change;
//        this.cgmes = cgmes;
//        this.currId = change.getIdentifiableId();
//        this.svVoltageId = getSvVoltageId();
//        this.terminalId = getTerminalId();
//    }
//
//    @Override
//    public Multimap<String, CgmesPredicateDetails> converter() {
//
//        final Multimap<String, CgmesPredicateDetails> map = ArrayListMultimap.create();
//        Bus newBus = (Bus) change.getIdentifiable();
//
//        map.put("rdfType", new CgmesPredicateDetails("rdf:type", "_TP", false, "cim:TopologicalNode"));
//
//        String name = newBus.getName();
//        if (name != null) {
//            map.put("name", new CgmesPredicateDetails("cim:IdentifiedObject.name", "_TP", false, name));
//        }
//
//        String baseVoltageId = getBaseVoltageId(newBus);
//        if (!baseVoltageId.equals("NaN")) {
//            map.put("baseVoltage", new CgmesPredicateDetails(
//                "cim:TopologicalNode.BaseVoltage", "_TP", true, baseVoltageId));
//        }
//
//        String connectivityNodeContainerId = getVoltageId(newBus);
//        if (!connectivityNodeContainerId.equals("NaN")) {
//            map.put("connectivityNode", new CgmesPredicateDetails(
//                "cim:TopologicalNode.ConnectivityNodeContainer", "_TP", true, connectivityNodeContainerId));
//        }
//        /**
//         * Create SvVoltage element
//         */
//        map.put("rdfTypeSvVoltage", new CgmesPredicateDetails("rdf:type", "_SV", false, "cim:SvVoltage", svVoltageId));
//
//        map.put("topologicalNodeSvVoltage", new CgmesPredicateDetails("cim:SvVoltage.TopologicalNode",
//            "_SV", true, currId, svVoltageId));
//
//        double v = !String.valueOf(newBus.getV()).equals("NaN") ? newBus.getV() : 0.0;
//        map.put("v", new CgmesPredicateDetails("cim:SvVoltage.v", "_SV", false, String.valueOf(v), svVoltageId));
//
//        double angle = !String.valueOf(newBus.getAngle()).equals("NaN") ? newBus.getAngle() : 0.0;
//        map.put("angle",
//            new CgmesPredicateDetails("cim:SvVoltage.angle", "_SV", false, String.valueOf(angle), svVoltageId));
//        /**
//         * Create TP Terminal element
//         */
//        map.put("rdfTypeTerminal_TP", new CgmesPredicateDetails("rdf:type", "_TP", false, "cim:Terminal", terminalId));
//
//        map.put("TerminalTopologicalNode", new CgmesPredicateDetails(
//            "cim:Terminal.TopologicalNode", "_TP", false, currId, terminalId));
//        /**
//         * Create EQ Terminal element
//         */
//        map.put("rdfTypeTerminal_EQ", new CgmesPredicateDetails("rdf:type", "_EQ", false, "cim:Terminal", terminalId));
//
//        String terminalName = newBus.getName().concat("_TE");
//        if (terminalName != null) {
//            map.put("name",
//                new CgmesPredicateDetails("cim:IdentifiedObject.name", "_EQ", false, terminalName, terminalId));
//        }
//
//        return map;
//    }
//
//    private String getVoltageId(Bus bus) {
//        return bus.getVoltageLevel().getId();
//    }
//
//    // TODO elena check for existing TerminalID
//    private String getTerminalId() {
//        return UUID.randomUUID().toString();
//    }
//
//    private String getBaseVoltageId(Bus bus) {
//        String voltageLevelId = getVoltageId(bus);
//        PropertyBags voltageLevels = cgmes.voltageLevels();
//        Iterator i = voltageLevels.iterator();
//        while (i.hasNext()) {
//            PropertyBag pb = (PropertyBag) i.next();
//            if (pb.getId("VoltageLevel").equals(voltageLevelId)) {
//                return pb.getId("BaseVoltage");
//            } else {
//                continue;
//            }
//        }
//        return "NaN";
//    }
//
//    private String getSvVoltageId() {
//        PropertyBags topologicalNodes = cgmes.topologicalNodes();
//        Iterator i = topologicalNodes.iterator();
//        while (i.hasNext()) {
//            PropertyBag pb = (PropertyBag) i.next();
//            if (pb.getId("TopologicalNode").equals(currId)) {
//                return (pb.getId("SvVoltage") != null) ? pb.getId("SvVoltage")
//                    : UUID.randomUUID().toString();
//            } else {
//                continue;
//            }
//        }
//        return UUID.randomUUID().toString();
//    }
//
//    private IidmChange change;
//    private CgmesModel cgmes;
//    private String currId;
//    private String svVoltageId;
//    private String terminalId;
//}
