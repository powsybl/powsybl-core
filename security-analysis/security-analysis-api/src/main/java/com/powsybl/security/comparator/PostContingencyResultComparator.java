/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.comparator;

import java.util.Comparator;
import java.util.Objects;

import com.powsybl.security.PostContingencyResult;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class PostContingencyResultComparator  implements Comparator<PostContingencyResult> {

    @Override
    public int compare(PostContingencyResult result1, PostContingencyResult result2) {
        Objects.requireNonNull(result1);
        Objects.requireNonNull(result2);
        return result1.getContingency().getId().compareTo(result2.getContingency().getId());
    }

}
