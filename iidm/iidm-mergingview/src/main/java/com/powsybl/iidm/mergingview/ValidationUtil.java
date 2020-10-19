/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.google.common.collect.Sets;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;

import java.util.Collection;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public final class ValidationUtil {

    private ValidationUtil() {
    }

    static void checkSingleVariant(final Network other) {
        // this check must not be done on the number of variants but on the size
        // of the internal variant array because the network can have only
        // one variant but an internal array with a size greater that one and
        // some re-usable variants
        if (other.getVariantManager().getVariantIds().size() != 1) {
            throw new PowsyblException("Merging of multi-variants network is not supported");
        }
    }

    static void checkUniqueIds(final Network other, final MergingViewIndex index) {
        // check mergeability
        final Collection<String> otherIds = other.getIdentifiables().stream()
                                                                    .map(Identifiable::getId)
                                                                    .collect(Collectors.toSet());
        index.getIdentifiableStream().forEach(identifiable -> {
            String id = identifiable.getId();
            if (otherIds.contains(id)) {
                checkValidDanglingLines(identifiable, other.getIdentifiable(id), () -> "The object '" + identifiable.getId() + "' already exists into merging view");
            }
        });
    }

    static void checkUniqueAliases(final Network other, final MergingViewIndex index) {
        // check mergeability
        final Set<String> otherIds = other.getIdentifiables().stream()
                .flatMap(i -> i.getAliases().stream())
                .collect(Collectors.toSet());
        index.getIdentifiableStream().forEach(identifiable -> {
            Set<String> aliases = identifiable.getAliases();
            Set<String> commons = Sets.intersection(aliases, otherIds);
            if (!commons.isEmpty()) {
                for (String alias : commons) {
                    Identifiable<?> otherIdentifiable = other.getIdentifiable(alias);
                    checkValidDanglingLines(identifiable, otherIdentifiable, () -> String.format("Object (%s) with alias '%s' cannot be created because alias already refers to object (%s) with ID '%s'",
                            otherIdentifiable.getClass(),
                            alias,
                            index.getIdentifiable(identifiable).getClass(),
                            identifiable.getId()));
                }
            }
        });
    }

    private static void checkValidDanglingLines(Identifiable<?> origin, Identifiable<?> other, Supplier<String> exceptionMessage) {
        if (other instanceof DanglingLine && origin instanceof DanglingLine) {
            String xnodeCode1 = ((DanglingLine) origin).getUcteXnodeCode();
            String xnodeCode2 = ((DanglingLine) other).getUcteXnodeCode();
            if (xnodeCode1 != null && xnodeCode2 != null) {
                if (!xnodeCode1.equals(xnodeCode2)) {
                    throw new PowsyblException(String.format("Dangling line couple %s have inconsistent Xnodes (%s!=%s)", origin.getId(), xnodeCode1, xnodeCode2));
                }
                return;
            }
        }
        throw new PowsyblException(exceptionMessage.get());
    }
}
