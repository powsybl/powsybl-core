/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * @author Chamseddine BENHAMED <chamseddine.benhamed at rte-france.com>
 */
public abstract class AbstractOptions<T> {
    protected Optional<Set<String>> extensions = Optional.empty();

    protected IidmImportExportMode mode = IidmImportExportMode.UNIQUE_FILE;

    public AbstractOptions() {
    }

    public abstract T setExtensions(Set<String> extensions);

    public abstract T setMode(IidmImportExportMode mode);

    public abstract T addExtension(String extension);

    public Optional<Set<String>> getExtensions() {
        return this.extensions;
    }

    public boolean withNoExtension() {
        return extensions.map(Set::isEmpty).orElse(false);
    }

    public  boolean withAllExtensions() {
        return !extensions.isPresent();
    }

    public  boolean withExtension(String extensionName) {
        return withAllExtensions() || extensions.orElse(new HashSet<>()).contains(extensionName);
    }

    public IidmImportExportMode getMode() {
        return this.mode;
    }

}
