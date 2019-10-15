package com.powsybl.cgmes.conversion.update.elements14;

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

    public Multimap<String, CgmesPredicateDetails> mapIidmToCgmesPredicates() {
        /**
         * PhaseTapChanger
         */
        PhaseTapChanger newPhaseTapChanger = newTwoWindingsTransformer.getPhaseTapChanger();
        Multimap<String, CgmesPredicateDetails> mapPHTC = null;

        if (newPhaseTapChanger != null) {
            mapPHTC = ArrayListMultimap.create();

            mapPHTC.put("rdfTypePHTC", new CgmesPredicateDetails(
                "rdf:type", "_EQ", false, "cim:PhaseTapChangerTabular", idPTC));

            mapPHTC.put("namePHTC", new CgmesPredicateDetails(
                "cim:IdentifiedObject.name", "_EQ", false, name.concat("_PHTC"), idPTC));

            mapPHTC.put("TransformerWindingPHTC", new CgmesPredicateDetails(
                "cim:PhaseTapChanger.TransformerWinding", "_EQ", true, idEnd1, idPTC));

            int lowTapPositionPHTC = newPhaseTapChanger.getLowTapPosition();
            mapPHTC.put("phaseTapChanger.lowTapPosition", new CgmesPredicateDetails(
                "cim:TapChanger.lowStep", "_EQ", false, String.valueOf(lowTapPositionPHTC), idPTC));

            int tapPositionPHTC = newPhaseTapChanger.getTapPosition();
            mapPHTC.put("phaseTapChanger.tapPosition", new CgmesPredicateDetails(
                "cim:SvTapStep.continuousPosition", "_SV", false, String.valueOf(tapPositionPHTC), idPTC));

            mapPHTC.put("phaseTapChanger.PhaseTapChangerTable", new CgmesPredicateDetails(
                "cim:PhaseTapChangerTabular.PhaseTapChangerTable", "_EQ", true, idPTCtable, idPTC));

            /**
             * PhaseTapChangerTable TODO elena check names and table predicates for cim14
             */
            mapPHTC.put("phaseTapChangerTable", new CgmesPredicateDetails(
                "rdf:type", "_EQ", false, "cim:PhaseTapChangerTable", idPTCtable));

            mapPHTC.put("namePHTCTable", new CgmesPredicateDetails(
                "cim:IdentifiedObject.name", "_EQ", false, name.concat("_PHTC_Name"), idPTCtable));
            /**
             * PhaseTapChangerTablePoint
             */
            mapPHTC.put("PhaseTapChangerTablePoint", new CgmesPredicateDetails(
                "rdf:type", "_EQ", false, "cim:PhaseTapChangerTablePoint", idPTCtablePoint));
        }

        return mapPHTC;
    }

}
