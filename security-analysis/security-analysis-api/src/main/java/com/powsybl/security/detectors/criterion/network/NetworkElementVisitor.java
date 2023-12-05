package com.powsybl.security.detectors.criterion.network;

import com.powsybl.iidm.network.*;

public class NetworkElementVisitor {

    Identifiable identifiable;

    public NetworkElementVisitor(Identifiable identifiable) {
        this.identifiable = identifiable;
    }

    public boolean visitLineCriterion(LineCriterion lineCriterion) {
        return lineCriterion.getNetworkElementIds().contains(identifiable.getId()) || lineCriterion.getTwoCountriesCriterion().filter(identifiable, IdentifiableType.LINE) && lineCriterion.getSingleNominalVoltageCriterion().filter(identifiable, IdentifiableType.LINE);
    }

    public boolean visitTwoWindingsTransformerCriterion(TwoWindingsTransformerCriterion twoWindingsTransformerCriterionCriterion) {
        return false;
    }

    public boolean visitThreeWindingsTransformerCriterion(ThreeWindingsTransformerCriterion threeWindingsTransformerCriterion) {
        return false;
    }

}
