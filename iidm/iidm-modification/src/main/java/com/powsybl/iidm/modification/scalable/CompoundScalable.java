/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.scalable;

import java.util.Collection;
import java.util.Set;

/**
 * @author Philippe Edwards <philippe.edwards at rte-france.com>
 */
public interface CompoundScalable extends Scalable {

    /**
     * Returns all the scalables, active or not, that are contained in the compound scalable.
     *
     * @return scalables contained in the compound scalable.
     */
    Collection<Scalable> getScalables();

    /**
     * Returns the scalables that are contained in the compound scalable and have not been deactivated.
     *
     * @return active scalables contained in the compound scalable.
     */
    Collection<Scalable> getActiveScalables();

    /**
     * Deactivates a given set of scalables from the compound scalable and
     * from the compound scalables it  may contain.
     *
     * @param scalablesToDeactivate a set of scalables to be deactivated
     */
    void deactivateScalables(Set<Scalable> scalablesToDeactivate);

    /**
     * Reactivates all the scalables from the compound scalable and from
     * the compound scalables it may contain.
     */
    void activateAllScalables();

    /**
     * Reactivates a given set of scalables from the compound scalable and from
     * the compound scalables it may contain.
     *
     * @param scalablesToActivate a set of scalables to be reactivated
     */
    void activateScalables(Set<Scalable> scalablesToActivate);

    /**
     * Creates a shallow copy of the compound scalable.
     * The elementary scalables themselves are not duplicated but the container containing them is,
     * and shallow copies of compound scalables are also created. The scalables deactivated in
     * the original compound scalable start deactivated.
     *
     * @return a scalable of the same type as the original with the same scalables (and the same ones are deactivated).
     */
    CompoundScalable shallowCopy();
}
