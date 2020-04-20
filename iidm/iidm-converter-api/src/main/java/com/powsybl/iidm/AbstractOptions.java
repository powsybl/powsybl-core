/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm;

import java.util.Optional;
import java.util.Set;

/**
 * @author Chamseddine BENHAMED <chamseddine.benhamed at rte-france.com>
 */
public abstract class AbstractOptions<T> {
    protected Set<String> extensions = null;

    public abstract T setExtensions(Set<String> extensions);

    /**
     * @deprecated Not used anymore
     */
    @Deprecated
    public T setMode(IidmImportExportMode mode) {
        throw new UnsupportedOperationException("Not supported anymore");
    }

    public abstract T addExtension(String extension);

    public Optional<Set<String>> getExtensions() {
        return Optional.ofNullable(extensions);
    }

    public boolean withNoExtension() {
        return extensions != null && extensions.isEmpty();
    }

    public  boolean withAllExtensions() {
        return extensions == null;
    }

    public boolean hasAtLeastOneExtension(Set<String> extensions) {
        if (withAllExtensions()) {
            return true;
        }
        for (String extension : extensions) {
            if (this.extensions.contains(extension)) {
                return true;
            }
        }
        return false;
    }

    public  boolean withExtension(String extensionName) {
        return withAllExtensions() || extensions.contains(extensionName);
    }

    /**
     * @deprecated Not used anymore
     */
    @Deprecated
    public IidmImportExportMode getMode() {
        throw new UnsupportedOperationException("Not supported anymore");
    }

}
