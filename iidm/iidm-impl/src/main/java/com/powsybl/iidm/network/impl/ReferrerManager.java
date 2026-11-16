/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class ReferrerManager<T> {

    private final T referenced;

    private final List<Referrer<T>> referrers = new CopyOnWriteArrayList<>();

    public ReferrerManager(T referenced) {
        this.referenced = Objects.requireNonNull(referenced);
    }

    public List<Referrer<T>> getReferrers() {
        return referrers;
    }

    public void register(Referrer<T> referrer) {
        referrers.add(Objects.requireNonNull(referrer));
    }

    public void unregister(Referrer<T> referrer) {
        referrers.remove(Objects.requireNonNull(referrer));
    }

    public void notifyOfRemoval() {
        for (Referrer<T> referrer : referrers) {
            referrer.onReferencedRemoval(referenced);
        }
    }
}
