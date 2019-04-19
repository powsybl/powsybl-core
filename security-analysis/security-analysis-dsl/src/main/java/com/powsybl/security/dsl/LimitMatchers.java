/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.dsl;

import com.google.common.collect.ImmutableSet;

import java.util.Collection;
import java.util.Set;

/**
 * Provides implementations of {@link LimitMatcher}.
 *
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
final class LimitMatchers {

    private static final LimitMatcher TEMPORARY = (b, s, l, c) -> l != null;

    private static final LimitMatcher PERMANENT = (b, s, l, c) -> l == null;

    private static final LimitMatcher ANY_CONTINGENCY = (b, s, l, c) -> c != null;

    private static final LimitMatcher N_SITUATION = (b, s, l, c) -> c == null;

    private LimitMatchers() {
    }

    /**
     * A {@link LimitMatcher} which matches the branch with the specified ID.
     */
    static LimitMatcher branch(String id) {
        return (b, s, l, c) -> b.getId().equals(id);
    }

    /**
     * A {@link LimitMatcher} which matches the branches with the specified IDs.
     */
    static LimitMatcher branches(Collection<String> ids) {
        Set<String> set = ImmutableSet.copyOf(ids);
        return (b, s, l, c) -> set.contains(b.getId());
    }

    /**
     * A {@link LimitMatcher} which matches the contingency with the specified ID.
     */
    static LimitMatcher contingency(String id) {
        return (b, s, l, c) -> c != null && c.getId().equals(id);
    }

    /**
     * A {@link LimitMatcher} which matches the contingencies with the specified ID.
     */
    static LimitMatcher contingencies(Collection<String> ids) {
        Set<String> set = ImmutableSet.copyOf(ids);
        return (b, s, l, c) -> c != null && set.contains(c.getId());
    }

    /**
     * A {@link LimitMatcher} which matches any temporary limit.
     */
    static LimitMatcher temporary() {
        return TEMPORARY;
    }

    /**
     * A {@link LimitMatcher} which matches any permanent limit.
     */
    static LimitMatcher permanent() {
        return PERMANENT;
    }

    /**
     * A {@link LimitMatcher} which matches any limit on N situation.
     */
    static LimitMatcher nSituation() {
        return N_SITUATION;
    }

    /**
     * A {@link LimitMatcher} which matches any limit on post-contingency situations.
     */
    static LimitMatcher anyContingency() {
        return ANY_CONTINGENCY;
    }

}
