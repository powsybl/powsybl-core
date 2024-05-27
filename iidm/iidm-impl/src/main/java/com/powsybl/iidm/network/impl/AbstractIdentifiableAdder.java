/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Validable;
import com.powsybl.commons.ref.Ref;
import com.powsybl.iidm.network.util.Identifiables;

import java.util.Arrays;
import java.util.function.Predicate;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
abstract class AbstractIdentifiableAdder<T extends AbstractIdentifiableAdder<T>> implements Validable {

    private String id;

    private boolean ensureIdUnicity = false;

    private String name;

    private boolean fictitious = false;

    AbstractIdentifiableAdder() {
    }

    protected abstract NetworkImpl getNetwork();

    protected abstract String getTypeDescription();

    public T setId(String id) {
        this.id = id;
        return (T) this;
    }

    public T setEnsureIdUnicity(boolean ensureIdUnicity) {
        this.ensureIdUnicity = ensureIdUnicity;
        return (T) this;
    }

    public T setName(String name) {
        this.name = name;
        return (T) this;
    }

    public T setFictitious(boolean fictitious) {
        this.fictitious = fictitious;
        return (T) this;
    }

    protected String checkAndGetUniqueId() {
        return checkAndGetUniqueId(getNetwork().getIndex()::contains);
    }

    protected String checkAndGetUniqueId(Predicate<String> containsId) {
        if (id == null) {
            throw new PowsyblException(getTypeDescription() + " id is not set");
        }
        String uniqueId;
        if (ensureIdUnicity) {
            uniqueId = Identifiables.getUniqueId(id, containsId);
        } else {
            if (containsId.test(id)) {
                Identifiable<?> obj = getNetwork().getIndex().get(id);
                throw new PowsyblException("The network " + getNetwork().getId()
                        + " already contains an object '" + obj.getClass().getSimpleName()
                        + "' with the id '" + id + "'");
            }
            uniqueId = id;
        }
        return uniqueId;
    }

    protected String getName() {
        return name;
    }

    protected boolean isFictitious() {
        return fictitious;
    }

    @Override
    public String getMessageHeader() {
        return getTypeDescription() + " '" + id + "': ";
    }

    /**
     * Compute the {@link Ref}<{@link NetworkImpl}> to use for a new element from its voltage levels.
     *
     * @param network the root network in which the element will be added
     * @param voltageLevels the list of the voltage levels of the element
     * @return the networkRef to use
     */
    protected static Ref<NetworkImpl> computeNetworkRef(NetworkImpl network, VoltageLevelExt... voltageLevels) {
        if (voltageLevels.length == 0) {
            return network.getRef();
        }
        // We support only one level of subnetworks.
        // Thus, if the subnetworkIds of all the voltageLevels are the same (and not null), the ref is the one of
        // the subnetwork. Else, it is the root network's one.
        String subnetworkId = voltageLevels[0].getSubnetworkId();
        if (subnetworkId == null) {
            return network.getRef();
        }
        boolean existDifferentSubnetworkId = Arrays.stream(voltageLevels, 1, voltageLevels.length)
                .map(VoltageLevelExt::getSubnetworkId)
                .anyMatch(Predicate.not(subnetworkId::equals));
        if (existDifferentSubnetworkId) {
            return network.getRef();
        }
        return voltageLevels[0].getNetworkRef();
    }
}
