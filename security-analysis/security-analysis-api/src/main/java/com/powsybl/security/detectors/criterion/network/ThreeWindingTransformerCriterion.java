package com.powsybl.security.detectors.criterion.network;

import com.powsybl.iidm.network.util.criterion.SingleCountryCriterion;
import com.powsybl.iidm.network.util.criterion.ThreeNominalVoltageCriterion;

import java.util.Set;

public class ThreeWindingTransformerCriterion extends AbstractNetworkElementCriterion {

    SingleCountryCriterion singleCountryCriterion;
    ThreeNominalVoltageCriterion threeNominalVoltageCriterion = new ThreeNominalVoltageCriterion(null, null, null);

    public ThreeWindingTransformerCriterion(Set<String> networkElementIds) {
        super(networkElementIds);
    }

    @Override
    public NetworkElementCriterionType getNetworkElementCriterionType() {
        return NetworkElementCriterionType.THREE_WINDING_TRANSFORMER;
    }

    public SingleCountryCriterion getSingleCountryCriterion() {
        return singleCountryCriterion;
    }

    public ThreeWindingTransformerCriterion setSingleCountryCriterion(SingleCountryCriterion singleCountryCriterion) {
        this.singleCountryCriterion = singleCountryCriterion;
        return this;
    }

    public ThreeNominalVoltageCriterion getThreeNominalVoltageCriterion() {
        return threeNominalVoltageCriterion;
    }

    public ThreeWindingTransformerCriterion setThreeNominalVoltageCriterion(ThreeNominalVoltageCriterion threeNominalVoltageCriterion) {
        this.threeNominalVoltageCriterion = threeNominalVoltageCriterion;
        return this;
    }

}
