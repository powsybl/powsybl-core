/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.loadflow.validation;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.function.Supplier;

/**
 * Provides access to the list of known candidate computations.
 *
 * @author Sylvain Leclerc {@literal <sylvain.leclerc at rte-france.com>}
 */
public final class CandidateComputations {

    //Lazy initialization of the list of computations
    private static final Supplier<List<CandidateComputation>> COMPUTATIONS =
            Suppliers.memoize(() -> ImmutableList.copyOf(ServiceLoader.load(CandidateComputation.class, CandidateComputations.class.getClassLoader())));

    private CandidateComputations() {
    }

    /**
     * Get the list of all known candidate computations implementations.
     * The returned list is immutable.
     */
    public static List<CandidateComputation> getComputations() {
        return COMPUTATIONS.get();
    }

    /**
     * Get the list of all known candidate computations names.
     * The returned list is immutable.
     */
    public static List<String> getComputationsNames() {
        return getComputations().stream().map(CandidateComputation::getName).toList();
    }

    /**
     * Get a candidate computation by its name.
     */
    public static Optional<CandidateComputation> getComputation(String name) {
        return getComputations().stream().filter(c -> c.getName().equals(name)).findFirst();
    }

}
