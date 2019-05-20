/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.immutable;

import com.powsybl.iidm.network.VariantManager;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * <p>Methods {@link #cloneVariant(String, String)}, {@link #cloneVariant(String, List)}, {@link #removeVariant(String)} are not allowed.</p>
 * <p>But you can still invoke mutative methods {@link #setWorkingVariant(String)} and {@link #allowVariantMultiThreadAccess(boolean)}.</p>
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
final class ImmutableVariantManager implements VariantManager {

    private final VariantManager variantManager;

    ImmutableVariantManager(VariantManager variantManager) {
        this.variantManager = variantManager;
    }

    @Override
    public Collection<String> getVariantIds() {
        return Collections.unmodifiableCollection(variantManager.getVariantIds());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getWorkingVariantId() {
        return variantManager.getWorkingVariantId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setWorkingVariant(String variantId) {
        variantManager.setWorkingVariant(variantId);
    }

    /**
     * Mutative operation is not allowed. It will throw an exception in runtime.
     * @return
     */
    @Override
    public void cloneVariant(String sourceVariantId, List<String> targetVariantIds) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    /**
     * Mutative operation is not allowed. It will throw an exception in runtime.
     * @return
     */
    @Override
    public void cloneVariant(String sourceVariantId, List<String> targetVariantIds, boolean mayOverwrite) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    /**
     * Mutative operation is not allowed. It will throw an exception in runtime.
     * @return
     */
    @Override
    public void cloneVariant(String sourceVariantId, String targetVariantId) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    /**
     * Mutative operation is not allowed. It will throw an exception in runtime.
     * @return
     */
    @Override
    public void cloneVariant(String sourceVariantId, String targetVariantId, boolean mayOverwrite) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    /**
     * Mutative operation is not allowed. It will throw an exception in runtime.
     * @return
     */
    @Override
    public void removeVariant(String variantId) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void allowVariantMultiThreadAccess(boolean allow) {
        variantManager.allowVariantMultiThreadAccess(allow);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isVariantMultiThreadAccessAllowed() {
        return variantManager.isVariantMultiThreadAccessAllowed();
    }
}
