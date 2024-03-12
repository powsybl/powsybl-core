/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.criteria;

import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.IdentifiableType;

import java.util.regex.Pattern;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
public class RegexCriterion implements Criterion {

    private final String regex;

    public RegexCriterion(String regex) {
        this.regex = regex;
    }

    @Override
    public CriterionType getType() {
        return CriterionType.REGEX;
    }

    @Override
    public boolean filter(Identifiable<?> identifiable, IdentifiableType type) {
        return Pattern.compile(regex).matcher(identifiable.getId()).find();
    }

    public String getRegex() {
        return regex;
    }
}
