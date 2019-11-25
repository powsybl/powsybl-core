/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Identifiable;

import java.util.Collection;
import java.util.Set;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
abstract class AbstractIdentifiableAdapter<I extends Identifiable<I>> extends AbstractAdapter<I> implements Identifiable<I> {

    protected AbstractIdentifiableAdapter(final I delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    @Override
    public MergingView getNetwork() {
        return getIndex().getView();
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public String getId() {
        return getDelegate().getId();
    }

    @Override
    public String getName() {
        return getDelegate().getName();
    }

    @Override
    public boolean hasProperty() {
        return getDelegate().hasProperty();
    }

    @Override
    public boolean hasProperty(final String key) {
        return getDelegate().hasProperty(key);
    }

    @Override
    public String getProperty(final String key) {
        return getDelegate().getProperty(key);
    }

    @Override
    public String getProperty(final String key, final String defaultValue) {
        return getDelegate().getProperty(key, defaultValue);
    }

    @Override
    public String setProperty(final String key, final String value) {
        return getDelegate().setProperty(key, value);
    }

    @Override
    public Set<String> getPropertyNames() {
        return getDelegate().getPropertyNames();
    }

    @Override
    public <E extends Extension<I>> void addExtension(final Class<? super E> type, final E extension) {
        getDelegate().addExtension(type, extension);
    }

    @Override
    public <E extends Extension<I>> E getExtension(final Class<? super E> type) {
        return getDelegate().getExtension(type);
    }

    @Override
    public <E extends Extension<I>> E getExtensionByName(final String name) {
        return getDelegate().getExtensionByName(name);
    }

    @Override
    public <E extends Extension<I>> boolean removeExtension(final Class<E> type) {
        return getDelegate().removeExtension(type);
    }

    @Override
    public <E extends Extension<I>> Collection<E> getExtensions() {
        return getDelegate().getExtensions();
    }
}
