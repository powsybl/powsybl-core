/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.triplestore.api;

import java.util.Objects;

/**
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 */
public class PrefixNamespace {

    private final String prefix;
    private final String namespace;

    public PrefixNamespace(String prefix, String namespace) {
        this.prefix = Objects.requireNonNull(prefix);
        this.namespace = Objects.requireNonNull(namespace);
    }

    public String getPrefix() {
        return prefix;
    }

    public String getNamespace() {
        return namespace;
    }

    @Override
    public String toString() {
        return prefix + ":" + namespace;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PrefixNamespace)) {
            return false;
        }
        PrefixNamespace other = (PrefixNamespace) obj;
        return prefix.equals(other.getPrefix()) && namespace.equals(other.getNamespace());
    }

    @Override
    public int hashCode() {
        return Objects.hash(prefix, namespace);
    }
}
