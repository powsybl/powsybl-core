package com.powsybl.cgmes.conversion.update.elements16;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.powsybl.cgmes.conversion.update.CgmesPredicateDetails;
import com.powsybl.cgmes.conversion.update.IidmChange;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.iidm.network.RatioTapChanger;

public class RatioTapChangerToRatioTapChanger extends TwoWindingsTransformerToPowerTransformer {

    public RatioTapChangerToRatioTapChanger(IidmChange change, CgmesModel cgmes) {
        super(change, cgmes);
    }

    @Override
    public Multimap<String, CgmesPredicateDetails> mapIidmToCgmesPredicates() {
        /**
         * RatioTapChanger
         */
        RatioTapChanger newRatioTapChanger = newTwoWindingsTransformer.getRatioTapChanger();
        Multimap<String, CgmesPredicateDetails> mapRTC = null;

        if (newRatioTapChanger != null) {
            mapRTC = ArrayListMultimap.create();

            mapRTC.put("rdfTypeRTC", new CgmesPredicateDetails(
                "rdf:type", "_EQ", false, "cim:RatioTapChanger", idRTC));

            mapRTC.put("nameRTC", new CgmesPredicateDetails(
                "cim:IdentifiedObject.name", "_EQ", false, name, idRTC));

            mapRTC.put("TransformerWindingRTC", new CgmesPredicateDetails(
                "cim:RatioTapChanger.TransformerEnd", "_EQ", true, idEnd1, idRTC));

            int lowTapPositionRTC = newRatioTapChanger.getLowTapPosition();
            mapRTC.put("ratioTapChanger.lowTapPosition", new CgmesPredicateDetails(
                "cim:TapChanger.lowStep", "_EQ", false, String.valueOf(lowTapPositionRTC), idRTC));

            int tapPositionRTC = newRatioTapChanger.getTapPosition();
            mapRTC.put("ratioTapChanger.tapPosition", new CgmesPredicateDetails(
                    "cim:TapChanger.step", "_SSH", false, String.valueOf(tapPositionRTC), idRTC));
            mapRTC.put("ratioTapChanger.tapPosition", new CgmesPredicateDetails(
                    "cim:SvTapStep.position", "_SV", false, String.valueOf(tapPositionRTC), idRTC));

            /**
             * RatioTapChangerTable TODO elena check names
             */
            mapRTC.put("ratioTapChangerTable", new CgmesPredicateDetails(
                "rdf:type", "_EQ", false, "cim:RatioTapChangerTable", idRTCTable));

            mapRTC.put("nameRTCTable", new CgmesPredicateDetails(
                "cim:IdentifiedObject.name", "_EQ", false, name.concat("_RTC_Name"), idRTCTable));
            /**
             * RatioTapChangerTablePoint
             */
            mapRTC.put("RatioTapChangerTablePoint", new CgmesPredicateDetails(
                "rdf:type", "_EQ", false, "cim:RatioTapChangerTablePoint", idRTCTablePoint));
        }

        return mapRTC;
    }
}
