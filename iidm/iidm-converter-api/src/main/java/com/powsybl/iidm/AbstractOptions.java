/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * @author Chamseddine BENHAMED <chamseddine.benhamed at rte-france.com>
 */
public abstract class AbstractOptions<T> {
    protected Set<String> extensions = Sets.newHashSet("ALL");

    protected ImportExportTypes mode = ImportExportTypes.BASE_AND_EXTENSIONS_IN_ONE_SINGLE_FILE;

    public AbstractOptions(Set<String> extensions) {
        this.extensions = extensions;
    }

    public AbstractOptions() {

    }

    public abstract T setExtensions(Set<String> extensions);

    public abstract T setMode(ImportExportTypes mode);

    public Set<String> getExtensions() {
        return this.extensions;
    }

    public boolean withNoExtension() {
        return extensions.isEmpty();
    }

    public  boolean isALL() {
        return extensions.size() == 1 && extensions.iterator().next().equals("ALL");
    }

    public  boolean isInExtensionsList(String extensionName) {
        if (isALL()) {
            return Boolean.TRUE;
        }
        return extensions.contains(extensionName);
    }

}
