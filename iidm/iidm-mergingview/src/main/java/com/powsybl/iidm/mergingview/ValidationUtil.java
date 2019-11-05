/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import java.util.Collection;
import java.util.stream.Collectors;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public final class ValidationUtil {

    private ValidationUtil() {
    }

    public static void checkSingleyVariant(final Network other) {
        // this check must not be done on the number of variants but on the size
        // of the internal variant array because the network can have only
        // one variant but an internal array with a size greater that one and
        // some re-usable variants
        if (other.getVariantManager().getVariantIds().size() != 1) {
            throw new PowsyblException("Merging of multi-variants network is not supported");
        }
    }

    public static void checkUniqueIds(final Network other, final MergingViewIndex index) {
        // check mergeability
        final Collection<String> otherIds = other.getIdentifiables().stream().map(Identifiable::getId).collect(Collectors.toSet());
        index.getIdentifiableStream().map(Identifiable::getId).forEach(id -> {
            if (otherIds.contains(id)) {
                throw new PowsyblException("The object '" + id + "' already exists into merging view");
            }
        });
    }
}
