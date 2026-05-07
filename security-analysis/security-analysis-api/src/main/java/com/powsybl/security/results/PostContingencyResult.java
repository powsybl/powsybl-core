/**
 * Copyright (c) 2016-2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.results;

import com.powsybl.commons.extensions.Extendable;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.contingency.Contingency;
import com.powsybl.security.LimitViolationsResult;
import com.powsybl.security.PostContingencyComputationStatus;
import java.util.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian@ at rte-france.com>}
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
public class PostContingencyResult extends AbstractContingencyResult implements Extendable<PostContingencyResult> {

    private final Contingency contingency;

    private final PostContingencyComputationStatus status;

    private final ConnectivityResult connectivityResult; // Assuming this is defined elsewhere

    private final Map<Class<?>, Extension<PostContingencyResult>> extensions = new HashMap<>();

    private final Map<String, Extension<PostContingencyResult>> extensionsByName = new HashMap<>();

    public PostContingencyResult(Contingency contingency,
                                 PostContingencyComputationStatus status,
                                 LimitViolationsResult limitViolationsResult,
                                 NetworkResult networkResult,
                                 ConnectivityResult connectivityResult,
                                 double distributedActivePower) {
        super(limitViolationsResult, networkResult, distributedActivePower);
        this.contingency = Objects.requireNonNull(contingency);
        this.status = Objects.requireNonNull(status);
        this.connectivityResult = Objects.requireNonNull(connectivityResult);
    }

    public Contingency getContingency() {
        return contingency;
    }

    public PostContingencyComputationStatus getStatus() {
        return status;
    }

    public ConnectivityResult getConnectivityResult() {
        return connectivityResult;
    }

    @Override
    public <E extends Extension<PostContingencyResult>> void addExtension(Class<? super E> type, E extension) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(extension);
        removeExtension((Class<E>) extension.getClass());
        extension.setExtendable(this);
        extensions.put(type, extension);
        extensionsByName.put(extension.getName(), extension);
    }

    @Override
    public <E extends Extension<PostContingencyResult>> E getExtension(Class<? super E> type) {
        Objects.requireNonNull(type);
        return (E) extensions.get(type);
    }

    @Override
    public <E extends Extension<PostContingencyResult>> E getExtensionByName(String name) {
        Objects.requireNonNull(name);
        return (E) extensionsByName.get(name);
    }

    // Internal helper method to remove an extension by its type and instance
    private <E extends Extension<PostContingencyResult>> void removeExtensionInternal(Class<E> type, E extension) {
        extensions.remove(type);
        extensionsByName.remove(extension.getName());
        extension.setExtendable(null);
    }

    @Override
    public <E extends Extension<PostContingencyResult>> boolean removeExtension(Class<E> type) {
        boolean removed = false;
        Extension<PostContingencyResult> extension = extensions.get(type);
        if (extension != null) {
            removeExtensionInternal((Class<E>) type, (E) extension);
            removed = true;
        }

        return removed;
    }

    @Override
    public Collection<Extension<PostContingencyResult>> getExtensions() {
        return extensionsByName.values();
    }
}
