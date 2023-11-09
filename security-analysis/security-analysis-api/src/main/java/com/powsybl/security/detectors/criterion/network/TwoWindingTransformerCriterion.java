package com.powsybl.security.detectors.criterion.network;

import com.powsybl.iidm.network.util.criterion.SingleCountryCriterion;
import com.powsybl.iidm.network.util.criterion.TwoNominalVoltageCriterion;

import java.util.Set;

public class TwoWindingTransformerCriterion extends AbstractNetworkElementCriterion implements BranchCriterion {

    SingleCountryCriterion singleCountryCriterion;
    TwoNominalVoltageCriterion twoNominalVoltageCriterion = new TwoNominalVoltageCriterion(null, null);

    public TwoWindingTransformerCriterion(Set<String> networkElementIds) {
        super(networkElementIds);
    }

    @Override
    public NetworkElementCriterionType getNetworkElementCriterionType() {
        return NetworkElementCriterionType.BRANCH;
    }

    @Override
    public BranchCriterionType getBranchCriterionType() {
        return BranchCriterionType.TWO_WINDING_TRANSFORMER;
    }

    public SingleCountryCriterion getSingleCountryCriterion() {
        return singleCountryCriterion;
    }

    public TwoWindingTransformerCriterion setSingleCountryCriterion(SingleCountryCriterion singleCountryCriterion) {
        this.singleCountryCriterion = singleCountryCriterion;
        return this;
    }

    public TwoNominalVoltageCriterion getTwoNominalVoltageCriterion() {
        return twoNominalVoltageCriterion;
    }

    public TwoWindingTransformerCriterion setTwoNominalVoltageCriterion(TwoNominalVoltageCriterion twoNominalVoltageCriterion) {
        this.twoNominalVoltageCriterion = twoNominalVoltageCriterion;
        return this;
    }

}
