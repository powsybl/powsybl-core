/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.iidm.mergingview;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Identifiable;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
abstract class AbstractAdapter<I extends Identifiable<I>> implements Identifiable<I> {

    private final I delegate;

    private final MergingViewIndex index;

    protected AbstractAdapter(final I delegate, final MergingViewIndex index) {
        Objects.requireNonNull(delegate, "delegate is null");
        this.delegate = delegate;
        this.index = index;
    }

    public MergingView getNetwork() {
        return index.getView();
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    public I getDelegate() {
        return delegate;
    }

    public MergingViewIndex getIndex() {
        return index;
    }

    @Override
    public String getId() {
        return delegate.getId();
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public boolean hasProperty() {
        return delegate.hasProperty();
    }

    @Override
    public boolean hasProperty(final String key) {
        return delegate.hasProperty(key);
    }

    @Override
    public String getProperty(final String key) {
        return delegate.getProperty(key);
    }

    @Override
    public String getProperty(final String key, final String defaultValue) {
        return delegate.getProperty(key, defaultValue);
    }

    @Override
    public String setProperty(final String key, final String value) {
        return delegate.setProperty(key, value);
    }

    @Override
    public Set<String> getPropertyNames() {
        return delegate.getPropertyNames();
    }

    @Override
    public <E extends Extension<I>> void addExtension(final Class<? super E> type, final E extension) {
        delegate.addExtension(type, extension);
    }

    @Override
    public <E extends Extension<I>> E getExtension(final Class<? super E> type) {
        return delegate.getExtension(type);
    }

    @Override
    public <E extends Extension<I>> E getExtensionByName(final String name) {
        return delegate.getExtensionByName(name);
    }

    @Override
    public <E extends Extension<I>> boolean removeExtension(final Class<E> type) {
        return delegate.removeExtension(type);
    }

    @Override
    public <E extends Extension<I>> Collection<E> getExtensions() {
        return delegate.getExtensions();
    }
}
