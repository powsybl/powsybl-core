package com.powsybl.security.detectors.criterion.network;

import com.powsybl.iidm.network.util.criterion.SingleCountryCriterion;
import com.powsybl.iidm.network.util.criterion.ThreeNominalVoltageCriterion;

import java.util.Set;

public class ThreeWindingsTransformerCriterion extends AbstractNetworkElementCriterion {

    SingleCountryCriterion singleCountryCriterion;
    ThreeNominalVoltageCriterion threeNominalVoltageCriterion = new ThreeNominalVoltageCriterion(null, null, null);

    public ThreeWindingsTransformerCriterion(Set<String> networkElementIds) {
        super(networkElementIds);
    }

    @Override
    public NetworkElementCriterionType getNetworkElementCriterionType() {
        return NetworkElementCriterionType.THREE_WINDING_TRANSFORMER;
    }

    @Override
    public boolean accept(NetworkElementVisitor networkElementVisitor) {
        return networkElementVisitor.visitThreeWindingsTransformerCriterion(this);
    }

    public SingleCountryCriterion getSingleCountryCriterion() {
        return singleCountryCriterion;
    }

    public ThreeWindingsTransformerCriterion setSingleCountryCriterion(SingleCountryCriterion singleCountryCriterion) {
        this.singleCountryCriterion = singleCountryCriterion;
        return this;
    }

    public ThreeNominalVoltageCriterion getThreeNominalVoltageCriterion() {
        return threeNominalVoltageCriterion;
    }

    public ThreeWindingsTransformerCriterion setThreeNominalVoltageCriterion(ThreeNominalVoltageCriterion threeNominalVoltageCriterion) {
        this.threeNominalVoltageCriterion = threeNominalVoltageCriterion;
        return this;
    }

}
