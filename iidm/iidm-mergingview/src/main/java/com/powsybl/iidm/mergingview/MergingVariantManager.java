/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VariantManager;
import com.powsybl.iidm.network.VariantManagerConstants;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
class MergingVariantManager implements VariantManager {

    private final MergingViewIndex index;

    private boolean allowVariantMultiThreadAccess = false;

    private String workingVariantId = VariantManagerConstants.INITIAL_VARIANT_ID;

    MergingVariantManager(final MergingViewIndex index) {
        this.index = Objects.requireNonNull(index, "merging view index is null");
    }

    private Stream<VariantManager> getVariantManagerStream() {
        return index.getNetworkStream().map(Network::getVariantManager);
    }

    @Override
    public Collection<String> getVariantIds() {
        final VariantManager vm = getVariantManagerStream().findFirst().orElse(null);
        if (Objects.isNull(vm)) {
            throw new PowsyblException("No VariantManager found");
        }
        return vm.getVariantIds();
    }

    @Override
    public String getWorkingVariantId() {
        return workingVariantId;
    }

    @Override
    public void setWorkingVariant(String variantId) {
        this.workingVariantId = variantId;
        getVariantManagerStream().forEach(v -> v.setWorkingVariant(variantId));
    }

    @Override
    public void allowVariantMultiThreadAccess(boolean allow) {
        this.allowVariantMultiThreadAccess = allow;
        getVariantManagerStream().forEach(v -> v.allowVariantMultiThreadAccess(allow));
    }

    @Override
    public boolean isVariantMultiThreadAccessAllowed() {
        return allowVariantMultiThreadAccess;
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public void cloneVariant(String sourceVariantId, List<String> targetVariantIds) {
        cloneVariant(sourceVariantId, targetVariantIds, false);
    }

    @Override
    public void cloneVariant(String sourceVariantId, List<String> targetVariantIds, boolean mayOverwrite) {
        getVariantManagerStream().forEach(v -> v.cloneVariant(sourceVariantId, targetVariantIds, mayOverwrite));
    }

    @Override
    public void cloneVariant(String sourceVariantId, String targetVariantId) {
        cloneVariant(sourceVariantId, targetVariantId, false);
    }

    @Override
    public void cloneVariant(String sourceVariantId, String targetVariantId, boolean mayOverwrite) {
        getVariantManagerStream().forEach(v -> v.cloneVariant(sourceVariantId, targetVariantId, mayOverwrite));
    }

    @Override
    public void removeVariant(String variantId) {
        getVariantManagerStream().forEach(v -> v.removeVariant(variantId));
    }
}
