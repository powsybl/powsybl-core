package com.powsybl.cgmes.update.elements16;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.update.CgmesPredicateDetails;
import com.powsybl.cgmes.update.IidmChange;
import com.powsybl.iidm.network.RatioTapChanger;

public class RatioTapChangerToRatioTapChanger extends TwoWindingsTransformerToPowerTransformer {

    public RatioTapChangerToRatioTapChanger(IidmChange change, CgmesModel cgmes) {
        super(change, cgmes);
    }

    public Multimap<String, CgmesPredicateDetails> mapIidmToCgmesPredicates() {
        /**
         * RatioTapChanger
         */
        RatioTapChanger newRatioTapChanger = newTwoWindingsTransformer.getRatioTapChanger();
        Multimap<String, CgmesPredicateDetails> mapRTTC = null;

        if (newRatioTapChanger != null) {
            mapRTTC = ArrayListMultimap.create();

            mapRTTC.put("rdfTypeRTTC", new CgmesPredicateDetails(
                "rdf:type", "_EQ", false, "cim:RatioTapChanger", idRTTC));

            mapRTTC.put("nameRTTC", new CgmesPredicateDetails(
                "cim:IdentifiedObject.name", "_EQ", false, name, idRTTC));

            mapRTTC.put("TransformerWindingRTTC", new CgmesPredicateDetails(
                "cim:RatioTapChanger.TransformerEnd", "_EQ", true, idEnd1, idRTTC));

            int lowTapPositionRTTC = newRatioTapChanger.getLowTapPosition();
            mapRTTC.put("ratioTapChanger.lowTapPosition", new CgmesPredicateDetails(
                "cim:TapChanger.lowStep", "_EQ", false, String.valueOf(lowTapPositionRTTC), idRTTC));

            int tapPositionRTTC = newRatioTapChanger.getTapPosition();
            mapRTTC.put("ratioTapChanger.tapPosition", new CgmesPredicateDetails(
                "cim:TapChanger.neutralStep", "_EQ", false, String.valueOf(tapPositionRTTC + 1), idRTTC));

            /**
             * RatioTapChangerTable TODO elena check names
             */
            mapRTTC.put("ratioTapChangerTable", new CgmesPredicateDetails(
                "rdf:type", "_EQ", false, "cim:RatioTapChangerTable", idRTTC_Table));

            mapRTTC.put("nameRTTCTable", new CgmesPredicateDetails(
                "cim:IdentifiedObject.name", "_EQ", false, name.concat("_RTTC_Name"), idRTTC_Table));
            /**
             * RatioTapChangerTablePoint
             */
            mapRTTC.put("RatioTapChangerTablePoint", new CgmesPredicateDetails(
                "rdf:type", "_EQ", false, "cim:RatioTapChangerTablePoint", idRTTC_TablePoint));
        }

        return mapRTTC;
    }
}
