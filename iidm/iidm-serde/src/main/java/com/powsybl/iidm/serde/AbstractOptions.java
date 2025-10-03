/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.io.TreeDataFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * @author Chamseddine BENHAMED {@literal <chamseddine.benhamed at rte-france.com>}
 */
public abstract class AbstractOptions<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractOptions.class);

    protected Set<String> includedExtensions;

    protected Set<String> excludedExtensions;

    protected TreeDataFormat format = TreeDataFormat.XML;

    /**
     * Set the set of extensions to be included from import / export
     * @param extensions the set of extensions o be included from import / export
     * @return this casted to T
     */
    public T setIncludedExtensions(Set<String> extensions) {
        if (excludedExtensions != null) {
            LOGGER.warn("Previously excluded extensions list will now be ignored: {}", excludedExtensions);
        }
        if (extensions != null && extensions.isEmpty()) {
            LOGGER.warn("All extensions will be excluded");
        }
        this.excludedExtensions = null;
        this.includedExtensions = extensions;
        return castThis();
    }

    /**
     * Set the set of extensions to be excluded from import / export
     * @param extensions the set of extensions o be excluded from import / export
     * @return this casted to T
     */
    public T setExcludedExtensions(Set<String> extensions) {
        if (includedExtensions != null) {
            LOGGER.warn("Previously included extensions list will now be ignored: {}", includedExtensions);
        }
        this.includedExtensions = null;
        this.excludedExtensions = extensions;
        return castThis();
    }

    /**
     * Add an extension to the list of included extension for import / export
     * @param extension the extension to be included for import / export
     * @return this casted to T
     */
    public T addIncludedExtension(String extension) {
        if (excludedExtensions != null) {
            if (excludedExtensions.remove(extension)) {
                LOGGER.warn("Previously excluded extensions {} will now be included", extension);
            }
        } else {
            if (includedExtensions == null) {
                includedExtensions = new HashSet<>();
            }
            includedExtensions.add(extension);
        }
        return castThis();
    }

    /**
     * Add an extension to the list of excluded extension for import / export
     * @param extension the extension to be excluded for import / export
     * @return this casted to T
     */
    public T addExcludedExtension(String extension) throws PowsyblException {
        if (includedExtensions != null) {
            if (includedExtensions.remove(extension)) {
                LOGGER.warn("Previously included extensions {} will now be excluded", extension);
            }
        } else {
            if (excludedExtensions == null) {
                excludedExtensions = new HashSet<>();
            }
            excludedExtensions.add(extension);
        }
        return castThis();
    }

    public Optional<Set<String>> getIncludedExtensions() {
        return Optional.ofNullable(includedExtensions);
    }

    public Optional<Set<String>> getExcludedExtensions() {
        return Optional.ofNullable(excludedExtensions);
    }

    public boolean withNoExtension() {
        return includedExtensions != null && includedExtensions.isEmpty();
    }

    public boolean withAllExtensions() {
        return includedExtensions == null && (excludedExtensions == null || excludedExtensions.isEmpty());
    }

    public boolean hasAtLeastOneExtension(Set<String> extensions) {
        if (withAllExtensions()) {
            return true;
        }
        for (String extension : extensions) {
            if (withExtension(extension)) {
                return true;
            }
        }
        return false;
    }

    public boolean withExtension(String extensionName) {
        return (excludedExtensions == null || !excludedExtensions.contains(extensionName))
                && (includedExtensions == null || includedExtensions.contains(extensionName));
    }

    public abstract boolean isThrowExceptionIfExtensionNotFound();

    public TreeDataFormat getFormat() {
        return format;
    }

    public T setFormat(TreeDataFormat format) {
        this.format = Objects.requireNonNull(format);
        return castThis();
    }

    /**
     * Cast to T is safe in practice (hence the added SuppressWarning annotation).
     * @return this casted to T
     */
    @SuppressWarnings("unchecked")
    private T castThis() {
        return (T) this;
    }

}