/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.google.common.collect.Sets;
import com.powsybl.commons.io.TreeDataFormat;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * @author Chamseddine BENHAMED {@literal <chamseddine.benhamed at rte-france.com>}
 */
public abstract class AbstractOptions<T> {

    /**
     * Extensions to be loaded must be in the extensions set but must not belong to the filteredExtension Set
     */
    protected Set<String> extensions;

    protected Set<String> filteredExtension;

    protected TreeDataFormat format = TreeDataFormat.XML;

    public T setExtensions(Set<String> extensions) {
        this.extensions = extensions;
        return (T) this;
    }

    public T setFilteredExtensions(Set<String> filteredExtensions) {
        this.filteredExtension = filteredExtensions;
        return (T) this;
    }

    public T addExtension(String extension) {
        if (extensions != null) {
            extensions.add(extension);
        } else {
            this.extensions = Sets.newHashSet(extension);
        }
        return (T) this;
    }

    public T addFilteredExtension(String extensionToBeFiltered) {
        if (filteredExtension != null) {
            filteredExtension.add(extensionToBeFiltered);
        } else {
            this.filteredExtension = Sets.newHashSet(extensionToBeFiltered);
        }
        return (T) this;
    }

    public Optional<Set<String>> getExtensions() {
        return Optional.ofNullable(extensions);
    }

    public Optional<Set<String>> getFilteredExtensions() {
        return Optional.ofNullable(filteredExtension);
    }

    public boolean withNoExtension() {
        return extensions != null && extensions.isEmpty();
    }

    public boolean withAllExtensions() {
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

    public boolean withExtension(String extensionName) {
        return (filteredExtension == null || !filteredExtension.contains(extensionName))
                && (withAllExtensions() || extensions.contains(extensionName));
    }

    public abstract boolean isThrowExceptionIfExtensionNotFound();

    public TreeDataFormat getFormat() {
        return format;
    }

    public T setFormat(TreeDataFormat format) {
        this.format = Objects.requireNonNull(format);
        return (T) this;
    }

}
