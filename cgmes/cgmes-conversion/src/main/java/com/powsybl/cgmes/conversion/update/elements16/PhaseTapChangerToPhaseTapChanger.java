package com.powsybl.cgmes.conversion.update.elements16;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.powsybl.cgmes.conversion.update.CgmesPredicateDetails;
import com.powsybl.cgmes.conversion.update.IidmChange;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.iidm.network.PhaseTapChanger;

public class PhaseTapChangerToPhaseTapChanger extends TwoWindingsTransformerToPowerTransformer {

    public PhaseTapChangerToPhaseTapChanger(IidmChange change, CgmesModel cgmes) {
        super(change, cgmes);
    }

    @Override
    public Multimap<String, CgmesPredicateDetails> mapIidmToCgmesPredicates() {
        /**
         * PhaseTapChanger
         */
        PhaseTapChanger newPhaseTapChanger = newTwoWindingsTransformer.getPhaseTapChanger();
        Multimap<String, CgmesPredicateDetails> mapPTC = null;

        if (newPhaseTapChanger != null) {
            mapPTC = ArrayListMultimap.create();

            mapPTC.put("rdfTypePTC", new CgmesPredicateDetails(
                "rdf:type", "_EQ", false, "cim:PhaseTapChangerTabular", idPTC));

            mapPTC.put("namePTC", new CgmesPredicateDetails(
                "cim:IdentifiedObject.name", "_EQ", false, name, idPTC));

            mapPTC.put("TransformerWindingPTC", new CgmesPredicateDetails(
                "cim:PhaseTapChanger.TransformerEnd", "_EQ", true, idEnd1, idPTC));

            int lowTapPositionPTC = newPhaseTapChanger.getLowTapPosition();
            mapPTC.put("phaseTapChanger.lowTapPosition", new CgmesPredicateDetails(
                "cim:TapChanger.lowStep", "_EQ", false, String.valueOf(lowTapPositionPTC), idPTC));

            int tapPositionPTC = newPhaseTapChanger.getTapPosition();
            mapPTC.put("phaseTapChanger.tapPosition", new CgmesPredicateDetails(
                "cim:TapChanger.step", "_SSH", false, String.valueOf(tapPositionPTC), idPTC));
            mapPTC.put("phaseTapChanger.tapPosition", new CgmesPredicateDetails(
                    "cim:SvTapStep.position", "_SV", false, String.valueOf(tapPositionPTC), idPTC));

            mapPTC.put("phaseTapChanger.PhaseTapChangerTable", new CgmesPredicateDetails(
                "cim:PhaseTapChangerTabular.PhaseTapChangerTable", "_EQ", true, idPTCTable, idPTC));

            /**
             * PhaseTapChangerTable TODO elena check names
             */
            mapPTC.put("phaseTapChangerTable", new CgmesPredicateDetails(
                "rdf:type", "_EQ", false, "cim:PhaseTapChangerTable", idPTCTable));

            mapPTC.put("namePTCTable", new CgmesPredicateDetails(
                "cim:IdentifiedObject.name", "_EQ", false, name.concat("_PTC_Name"), idPTCTable));
            /**
             * PhaseTapChangerTablePoint
             */
            mapPTC.put("PhaseTapChangerTablePoint", new CgmesPredicateDetails(
                "rdf:type", "_EQ", false, "cim:PhaseTapChangerTablePoint", idPTCTablePoint));
        }

        return mapPTC;
    }

}
