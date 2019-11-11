//package com.powsybl.cgmes.conversion.update.elements14;
//
//import com.google.common.collect.ArrayListMultimap;
//import com.google.common.collect.Multimap;
//import com.powsybl.cgmes.conversion.update.CgmesPredicateDetails;
//import com.powsybl.cgmes.conversion.update.IidmChange;
//import com.powsybl.cgmes.model.CgmesModel;
//import com.powsybl.iidm.network.PhaseTapChanger;
//
//public class PhaseTapChangerToPhaseTapChanger extends TwoWindingsTransformerToPowerTransformer {
//
//    public PhaseTapChangerToPhaseTapChanger(IidmChange change, CgmesModel cgmes) {
//        super(change, cgmes);
//    }
//
//    @Override
//    public Multimap<String, CgmesPredicateDetails> converter() {
//        /**
//         * PhaseTapChanger
//         */
//        PhaseTapChanger newPhaseTapChanger = newTwoWindingsTransformer.getPhaseTapChanger();
//        Multimap<String, CgmesPredicateDetails> mapPTC = null;
//
//        if (newPhaseTapChanger != null) {
//            mapPTC = ArrayListMultimap.create();
//
//            mapPTC.put("rdfTypePTC", new CgmesPredicateDetails(
//                "rdf:type", "_EQ", false, "cim:PhaseTapChangerTabular", idPTC));
//
//            mapPTC.put("namePTC", new CgmesPredicateDetails(
//                "cim:IdentifiedObject.name", "_EQ", false, name.concat("_PTC"), idPTC));
//
//            mapPTC.put("TransformerWindingPTC", new CgmesPredicateDetails(
//                "cim:PhaseTapChanger.TransformerWinding", "_EQ", true, idEnd1, idPTC));
//
//            int lowTapPositionPTC = newPhaseTapChanger.getLowTapPosition();
//            mapPTC.put("phaseTapChanger.lowTapPosition", new CgmesPredicateDetails(
//                "cim:TapChanger.lowStep", "_EQ", false, String.valueOf(lowTapPositionPTC), idPTC));
//
//            int tapPositionPTC = newPhaseTapChanger.getTapPosition();
//            mapPTC.put("phaseTapChanger.tapPosition", new CgmesPredicateDetails(
//                "cim:SvTapStep.continuousPosition", "_SV", false, String.valueOf(tapPositionPTC), idPTC));
//
//            mapPTC.put("phaseTapChanger.PhaseTapChangerTable", new CgmesPredicateDetails(
//                "cim:PhaseTapChangerTabular.PhaseTapChangerTable", "_EQ", true, idPTCtable, idPTC));
//
//            /**
//             * PhaseTapChangerTable TODO elena check names and table predicates for cim14
//             */
//            mapPTC.put("phaseTapChangerTable", new CgmesPredicateDetails(
//                "rdf:type", "_EQ", false, "cim:PhaseTapChangerTable", idPTCtable));
//
//            mapPTC.put("namePTCTable", new CgmesPredicateDetails(
//                "cim:IdentifiedObject.name", "_EQ", false, name.concat("_PTC_Name"), idPTCtable));
//            /**
//             * PhaseTapChangerTablePoint
//             */
//            mapPTC.put("PhaseTapChangerTablePoint", new CgmesPredicateDetails(
//                "rdf:type", "_EQ", false, "cim:PhaseTapChangerTablePoint", idPTCtablePoint));
//        }
//
//        return mapPTC;
//    }
//
//}
