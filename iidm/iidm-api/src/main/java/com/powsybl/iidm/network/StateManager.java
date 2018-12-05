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
 * <p>
 * WARNING: Variant management is not thread safe and should never be done will an other thread read from or write to
 * an already existing variant. The classical pattern for multi-variant processing is to pre-allocate variants and
 * call {@link #allowVariantMultiThreadAccess} to allow multi-thread access on main thread, work on variants from other
 * threads (be carefull to only write concurrently attributes flagged as dependent to variant in the Javadoc) and then
 * remove variants from main thread once work is over.
 *
 * @deprecated use {@link VariantManager} instead.
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
    default Collection<String> getVariantIds() {
        throw new UnsupportedOperationException();
    }

    /**
     * @deprecated use {@link #getVariantIds()} instead.
     */
    @Deprecated
    default Collection<String> getStateIds() {
        return getVariantIds();
    }

    /**
     * Get the working variant.
     *
     * @return the id of the working variant
     */
    default String getWorkingVariantId() {
        throw new UnsupportedOperationException();
    }

    /**
     * @deprecated use {@link #getWorkingVariantId()} instead.
     */
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
    default void setWorkingVariant(String variantId) {
        throw new UnsupportedOperationException();
    }

    /**
     * @deprecated use {@link #setWorkingVariant(String)} instead.
     */
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
    default void cloneVariant(String sourceVariantId, List<String> targetVariantIds) {
        throw new UnsupportedOperationException();
    }

    /**
     * @deprecated use {@link #cloneVariant(String, List)} instead.
     */
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
    default void cloneVariant(String sourceVariantId, String targetVariantId) {
        throw new UnsupportedOperationException();
    }

    /**
     * @deprecated use {@link #cloneVariant(String, String)} instead.
     */
    @Deprecated
    default void cloneState(String sourceStateId, String targetStateId) {
        cloneVariant(sourceStateId, targetStateId);
    }

    /**
     * Remove a variant.
     *
     * @param variantId the id of the variant to remove
     */
    default void removeVariant(String variantId) {
        throw new UnsupportedOperationException();
    }

    /**
     * @deprecated use {@link #removeVariant(String)} instead.
     */
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
    default void allowVariantMultiThreadAccess(boolean allow) {
        throw new UnsupportedOperationException();
    }

    /**
     * @deprecated use {@link #allowVariantMultiThreadAccess(boolean)} instead.
     */
    @Deprecated
    default void allowStateMultiThreadAccess(boolean allow) {
        allowVariantMultiThreadAccess(allow);
    }

    default boolean isVariantMultiThreadAccessAllowed() {
        throw new UnsupportedOperationException();
    }

    /**
     * @deprecated use {@link #isVariantMultiThreadAccessAllowed()} instead.
     */
    @Deprecated
    default boolean isStateMultiThreadAccessAllowed() {
        return isVariantMultiThreadAccessAllowed();
    }
}
