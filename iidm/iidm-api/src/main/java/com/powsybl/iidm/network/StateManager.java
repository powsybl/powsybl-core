/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

import java.util.Collection;
import java.util.List;

/**
 * This class provides methods to manage variants of the network (create and
 * remove a variant, set the working variant, etc).
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@Deprecated
public interface StateManager {

    /**
     * Get the variant id list.
     *
     * @return the variant id list
     */
    Collection<String> getVariantIds();

    @Deprecated
    default Collection<String> getStateIds() {
        return getVariantIds();
    }

    /**
     * Get the working variant.
     *
     * @return the id of the working variant
     */
    String getWorkingVariantId();

    @Deprecated
    default String getWorkingStateId() {
        return getWorkingVariantId();
    }

    /**
     * Set the working variant.
     *
     * @param variantId the id of the working variant
     * @throws com.powsybl.commons.PowsyblException if the variant is not found
     */
    void setWorkingVariant(String variantId);

    @Deprecated
    default void setWorkingState(String variantId) {
        setWorkingVariant(variantId);
    }

    /**
     * Create a new variant by cloning an existing one.
     *
     * @param sourceVariantId the source variant id
     * @param targetVariantIds the target variant id list (the ones that will be created)
     * @throws com.powsybl.commons.PowsyblException
     *                         if the source variant is not found or if a variant with
     *                         an id of targetStateIds already exists
     */
    void cloneVariant(String sourceVariantId, List<String> targetVariantIds);

    @Deprecated
    default void cloneState(String sourceStateId, List<String> targetStateIds) {
        cloneVariant(sourceStateId, targetStateIds);
    }

    /**
     * Create a new variant by cloning an existing one.
     *
     * @param sourceVariantId the source variant id
     * @param targetVariantId the target variant id (the one that will be created)
     * @throws com.powsybl.commons.PowsyblException
     *                         if the source variant is not found or if a variant with
     *                         the id targetVariantId already exists
     */
    void cloneVariant(String sourceVariantId, String targetVariantId);

    @Deprecated
    default void cloneState(String sourceStateId, String targetStateId) {
        cloneVariant(sourceStateId, targetStateId);
    }

    /**
     * Remove a variant.
     *
     * @param variantId the id of the variant to remove
     */
    void removeVariant(String variantId);

    @Deprecated
    default void removeState(String stateId) {
        removeVariant(stateId);
    }

    /**
     * Allows variants to be accessed simulaneously by different threads. When
     * this options is activated, the working variant can have a different value
     * for each thread.
     * @param allow
     */
    void allowVariantMultiThreadAccess(boolean allow);

    @Deprecated
    default void allowStateMultiThreadAccess(boolean allow) {
        allowVariantMultiThreadAccess(allow);
    }

    boolean isVariantMultiThreadAccessAllowed();

    @Deprecated
    default boolean isStateMultiThreadAccessAllowed() {
        return isVariantMultiThreadAccessAllowed();
    }
}
