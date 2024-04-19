/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.triplestore.api;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public class TripleStoreOptions {

    private boolean removeInitialUnderscoreForIdentifiers = true;
    private boolean unescapeIdentifiers = true;

    public TripleStoreOptions() {
    }

    public TripleStoreOptions(boolean removeInitialUnderscoreForIdentifiers, boolean unescapeIdentifiers) {
        this.removeInitialUnderscoreForIdentifiers = removeInitialUnderscoreForIdentifiers;
        this.unescapeIdentifiers = unescapeIdentifiers;
    }

    public TripleStoreOptions setRemoveInitialUnderscoreForIdentifiers(boolean removeInitialUnderscoreForIdentifiers) {
        this.removeInitialUnderscoreForIdentifiers = removeInitialUnderscoreForIdentifiers;
        return this;
    }

    public boolean isRemoveInitialUnderscoreForIdentifiers() {
        return removeInitialUnderscoreForIdentifiers;
    }

    public boolean unescapeIdentifiers() {
        return unescapeIdentifiers;
    }

    public TripleStoreOptions decodeEscapedIdentifiers(boolean unescapeIdentifiers) {
        this.unescapeIdentifiers = unescapeIdentifiers;
        return this;
    }
}
