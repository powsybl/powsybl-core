package com.powsybl.security.detectors.criterion.network;

import com.powsybl.iidm.network.util.translation.NetworkElementInterface;

public class NetworkElementVisitor {

    NetworkElementInterface networkElement;

    public NetworkElementVisitor(NetworkElementInterface networkElement) {
        this.networkElement = networkElement;
    }

    public boolean visitLineCriterion(LineCriterion lineCriterion) {
        return lineCriterion.getNetworkElementIds().contains(networkElement.getId())
                || lineCriterion.getTwoCountriesCriterion().filter(networkElement) && lineCriterion.getSingleNominalVoltageCriterion().filter(networkElement);
    }

    public boolean visitTwoWindingsTransformerCriterion(TwoWindingsTransformerCriterion twoWindingsTransformerCriterion) {
        return twoWindingsTransformerCriterion.getNetworkElementIds().contains(networkElement.getId())
                || twoWindingsTransformerCriterion.getSingleCountryCriterion().filter(networkElement) && twoWindingsTransformerCriterion.getTwoNominalVoltageCriterion().filter(networkElement);
    }

    public boolean visitThreeWindingsTransformerCriterion(ThreeWindingsTransformerCriterion threeWindingsTransformerCriterion) {
        return threeWindingsTransformerCriterion.getNetworkElementIds().contains(networkElement.getId())
                || threeWindingsTransformerCriterion.getSingleCountryCriterion().filter(networkElement) && threeWindingsTransformerCriterion.getThreeNominalVoltageCriterion().filter(networkElement);
    }

}
