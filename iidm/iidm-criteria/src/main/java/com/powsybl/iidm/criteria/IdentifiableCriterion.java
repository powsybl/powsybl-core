/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.criteria;

import java.util.Objects;

/**
 * <p>{@link NetworkElementCriterion} on identifiables of a network.</p>
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public class IdentifiableCriterion extends AbstractNetworkElementEquipmentCriterion {
    public static final NetworkElementCriterionType TYPE = NetworkElementCriterionType.IDENTIFIABLE;

    private final AtLeastOneCountryCriterion atLeastOneCountryCriterion;
    private final AtLeastOneNominalVoltageCriterion atLeastOneNominalVoltageCriterion;

    public IdentifiableCriterion(AtLeastOneCountryCriterion atLeastOneCountryCriterion) {
        this(null, atLeastOneCountryCriterion);
    }

    public IdentifiableCriterion(String name, AtLeastOneCountryCriterion atLeastOneCountryCriterion) {
        super(name);
        this.atLeastOneCountryCriterion = Objects.requireNonNull(atLeastOneCountryCriterion);
        this.atLeastOneNominalVoltageCriterion = null;
    }

    public IdentifiableCriterion(AtLeastOneNominalVoltageCriterion atLeastOneNominalVoltageCriterion) {
        this((String) null, atLeastOneNominalVoltageCriterion);
    }

    public IdentifiableCriterion(String name, AtLeastOneNominalVoltageCriterion atLeastOneNominalVoltageCriterion) {
        super(name);
        this.atLeastOneCountryCriterion = null;
        this.atLeastOneNominalVoltageCriterion = Objects.requireNonNull(atLeastOneNominalVoltageCriterion);
    }

    public IdentifiableCriterion(AtLeastOneCountryCriterion atLeastOneCountryCriterion, AtLeastOneNominalVoltageCriterion atLeastOneNominalVoltageCriterion) {
        this(null, atLeastOneCountryCriterion, atLeastOneNominalVoltageCriterion);
    }

    public IdentifiableCriterion(String name, AtLeastOneCountryCriterion atLeastOneCountryCriterion, AtLeastOneNominalVoltageCriterion atLeastOneNominalVoltageCriterion) {
        super(name);
        this.atLeastOneCountryCriterion = Objects.requireNonNull(atLeastOneCountryCriterion);
        this.atLeastOneNominalVoltageCriterion = Objects.requireNonNull(atLeastOneNominalVoltageCriterion);
    }

    @Override
    public NetworkElementCriterionType getNetworkElementCriterionType() {
        return TYPE;
    }

    @Override
    public AtLeastOneCountryCriterion getCountryCriterion() {
        return atLeastOneCountryCriterion;
    }

    @Override
    public AtLeastOneNominalVoltageCriterion getNominalVoltageCriterion() {
        return atLeastOneNominalVoltageCriterion;
    }
}
