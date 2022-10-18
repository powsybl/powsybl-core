/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.triplestore.api;

import com.powsybl.commons.reporter.Reporter;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class TripleStoreOptions {

    private boolean removeInitialUnderscoreForIdentifiers = true;
    private Reporter reporter;

    public TripleStoreOptions() {
    }

    public TripleStoreOptions(boolean removeInitialUnderscoreForIdentifiers) {
        this.removeInitialUnderscoreForIdentifiers = removeInitialUnderscoreForIdentifiers;
    }

    public TripleStoreOptions(boolean removeInitialUnderscoreForIdentifiers, Reporter reporter) {
        this.removeInitialUnderscoreForIdentifiers = removeInitialUnderscoreForIdentifiers;
        this.reporter = reporter;
    }

    public TripleStoreOptions setRemoveInitialUnderscoreForIdentifiers(boolean removeInitialUnderscoreForIdentifiers) {
        this.removeInitialUnderscoreForIdentifiers = removeInitialUnderscoreForIdentifiers;
        return this;
    }

    public TripleStoreOptions setReporter(Reporter reporter) {
        this.reporter = reporter;
        return this;
    }

    public boolean isRemoveInitialUnderscoreForIdentifiers() {
        return removeInitialUnderscoreForIdentifiers;
    }

    public Reporter getReporter() {
        return this.reporter;
    }
}
