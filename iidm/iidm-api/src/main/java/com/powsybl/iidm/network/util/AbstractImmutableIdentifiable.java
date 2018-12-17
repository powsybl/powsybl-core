/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Identifiable;

import java.util.Collection;
import java.util.Objects;
import java.util.Properties;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
abstract class AbstractImmutableIdentifiable<I extends Identifiable<I>> implements Identifiable<I> {

    protected final I identifiable;

    protected AbstractImmutableIdentifiable(I identifiable) {
        this.identifiable = Objects.requireNonNull(identifiable);
    }

    @Override
    public String getId() {
        return identifiable.getId();
    }

    @Override
    public String getName() {
        return identifiable.getName();
    }

    @Override
    public boolean hasProperty() {
        return identifiable.hasProperty();
    }

    @Override
    public Properties getProperties() {
        return identifiable.getProperties();
    }

    @Override
    public <E extends Extension<I>> void addExtension(Class<? super E> type, E extension) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public <E extends Extension<I>> E getExtension(Class<E> type) {
        return identifiable.getExtension(type);
    }

    @Override
    public <E extends Extension<I>> E getExtensionByName(String name) {
        return identifiable.getExtensionByName(name);
    }

    @Override
    public <E extends Extension<I>> boolean removeExtension(Class<E> type) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public <E extends Extension<I>> Collection<E> getExtensions() {
        return identifiable.getExtensions();
    }
}
