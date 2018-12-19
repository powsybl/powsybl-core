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
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface VariantManager {

    /**
     * Get the variant id list.
     *
     * @return the variant id list
     */
    Collection<String> getVariantIds();

    /**
     * Get the working variant.
     *
     * @return the id of the working variant
     */
    String getWorkingVariantId();

    /**
     * Set the working variant.
     *
     * @param variantId the id of the working variant
     * @throws com.powsybl.commons.PowsyblException if the variant is not found
     */
    void setWorkingVariant(String variantId);

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

    /**
     * Remove a variant.
     *
     * @param variantId the id of the variant to remove
     */
    void removeVariant(String variantId);

    /**
     * Allows variants to be accessed simulaneously by different threads. When
     * this options is activated, the working variant can have a different value
     * for each thread.
     * @param allow
     */
    void allowVariantMultiThreadAccess(boolean allow);

    boolean isVariantMultiThreadAccessAllowed();
}
