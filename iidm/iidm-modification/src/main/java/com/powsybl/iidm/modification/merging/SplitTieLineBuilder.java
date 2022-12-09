/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.merging;

import com.powsybl.iidm.network.Branch;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
public class SplitTieLineBuilder {

    private String tieLineId = null;
    private final Set<String> aliasTypes1 = new HashSet<>();
    private final Set<String> aliasTypes2 = new HashSet<>();
    private Branch.Side defaultSideForAliases = Branch.Side.ONE;

    public SplitTieLine build() {
        return new SplitTieLine(tieLineId, aliasTypes1, aliasTypes2, defaultSideForAliases);
    }

    public SplitTieLineBuilder withTieLineId(String tieLineId) {
        this.tieLineId = tieLineId;
        return this;
    }

    public SplitTieLineBuilder withAliasTypes1(String... aliasTypes1) {
        this.aliasTypes1.addAll(Arrays.asList(aliasTypes1));
        return this;
    }

    public SplitTieLineBuilder withAliasTypes1(Set<String> aliasTypes1) {
        this.aliasTypes1.addAll(aliasTypes1);
        return this;
    }

    public SplitTieLineBuilder withAliasTypes2(String... aliasTypes2) {
        this.aliasTypes2.addAll(Arrays.asList(aliasTypes2));
        return this;
    }

    public SplitTieLineBuilder withAliasTypes2(Set<String> aliasTypes2) {
        this.aliasTypes2.addAll(aliasTypes2);
        return this;
    }

    public SplitTieLineBuilder withDefaultSideForAliases(Branch.Side defaultSideForAliases) {
        this.defaultSideForAliases = defaultSideForAliases;
        return this;
    }
}
