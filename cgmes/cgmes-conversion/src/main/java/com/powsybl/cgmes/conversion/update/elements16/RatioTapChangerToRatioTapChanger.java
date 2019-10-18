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
        Multimap<String, CgmesPredicateDetails> mapRTTC = null;

        if (newRatioTapChanger != null) {
            mapRTTC = ArrayListMultimap.create();

            mapRTTC.put("rdfTypeRTTC", new CgmesPredicateDetails(
                "rdf:type", "_EQ", false, "cim:RatioTapChanger", idRTC));

            mapRTTC.put("nameRTTC", new CgmesPredicateDetails(
                "cim:IdentifiedObject.name", "_EQ", false, name, idRTC));

            mapRTTC.put("TransformerWindingRTTC", new CgmesPredicateDetails(
                "cim:RatioTapChanger.TransformerEnd", "_EQ", true, idEnd1, idRTC));

            int lowTapPositionRTTC = newRatioTapChanger.getLowTapPosition();
            mapRTTC.put("ratioTapChanger.lowTapPosition", new CgmesPredicateDetails(
                "cim:TapChanger.lowStep", "_EQ", false, String.valueOf(lowTapPositionRTTC), idRTC));

            int tapPositionRTTC = newRatioTapChanger.getTapPosition();
            mapRTTC.put("ratioTapChanger.tapPosition", new CgmesPredicateDetails(
                    "cim:TapChanger.step", "_SSH", false, String.valueOf(tapPositionRTTC), idRTC));
            mapRTTC.put("ratioTapChanger.tapPosition", new CgmesPredicateDetails(
                    "cim:SvTapStep.position", "_SV", false, String.valueOf(tapPositionRTTC), idRTC));

            /**
             * RatioTapChangerTable TODO elena check names
             */
            mapRTTC.put("ratioTapChangerTable", new CgmesPredicateDetails(
                "rdf:type", "_EQ", false, "cim:RatioTapChangerTable", idRTCTable));

            mapRTTC.put("nameRTTCTable", new CgmesPredicateDetails(
                "cim:IdentifiedObject.name", "_EQ", false, name.concat("_RTTC_Name"), idRTCTable));
            /**
             * RatioTapChangerTablePoint
             */
            mapRTTC.put("RatioTapChangerTablePoint", new CgmesPredicateDetails(
                "rdf:type", "_EQ", false, "cim:RatioTapChangerTablePoint", idRTCTablePoint));
        }

        return mapRTTC;
    }
}
