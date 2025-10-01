/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.google.common.collect.Sets;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.io.TreeDataFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public T setIncludedExtensions(Set<String> extensions) {
        if (excludedExtensions != null) {
            LOGGER.warn("Previously excluded extensions {} will be ignored !", excludedExtensions);
        }
        if (extensions != null && extensions.isEmpty()) {
            LOGGER.warn("All Extensions will be disabled");
        }
        this.excludedExtensions = null;
        this.includedExtensions = extensions;
        return castThis();
    }

    public T setExcludedExtensions(Set<String> extensions) {
        if (includedExtensions != null) {
            LOGGER.warn("Previously included extensions {} will be ignored !", includedExtensions);
        }
        this.includedExtensions = null;
        this.excludedExtensions = extensions;
        return castThis();
    }

    public T addIncludedExtension(String extension) {
        if (excludedExtensions != null && excludedExtensions.contains(extension)) {
            LOGGER.warn("Previously excluded extensions {} will be included", extension);
        }
        if (excludedExtensions != null) {
            excludedExtensions.remove(extension);
        }
        if (includedExtensions != null) {
            includedExtensions.add(extension);
        } else {
            this.includedExtensions = Sets.newHashSet(extension);
        }
        return castThis();
    }

    public T addExcludedExtension(String extension) throws PowsyblException {
        if (includedExtensions != null && !includedExtensions.contains(extension)) {
            LOGGER.warn("Extension {} is not already explicitly included {} ! It will still be added to the exclusion list as it may be included after", extension, includedExtensions);
        }
        if (includedExtensions != null) {
            includedExtensions.remove(extension);
        }
        if (excludedExtensions != null) {
            excludedExtensions.add(extension);
        } else {
            this.excludedExtensions = Sets.newHashSet(extension);
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
        return includedExtensions == null;
    }

    public boolean hasAtLeastOneExtension(Set<String> extensions) {
        if (withAllExtensions()) {
            return true;
        }
        for (String extension : extensions) {
            if (this.includedExtensions.contains(extension)) {
                return true;
            }
        }
        return false;
    }

    public boolean withExtension(String extensionName) {
        return (excludedExtensions == null || !excludedExtensions.contains(extensionName))
                && (withAllExtensions() || includedExtensions.contains(extensionName));
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
     * Cast to T is safe in practice (hence added SuprresWarning annotation).
     * @return this casted to T
     */
    @SuppressWarnings("unchecked")
    private T castThis() {
        return (T) this;
    }

}
