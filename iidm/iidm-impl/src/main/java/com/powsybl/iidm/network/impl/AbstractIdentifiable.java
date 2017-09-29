/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network.impl;

import eu.itesla_project.iidm.network.Identifiable;

import java.util.*;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
abstract class AbstractIdentifiable<I extends Identifiable<I>> implements Identifiable<I>, Validable {

    protected String id;

    protected String name;

    protected Properties properties;

    protected final Map<Class<?>, Extension<I>> extensions = new HashMap<>();

    protected final Map<String, Extension<I>> extensionsByName = new HashMap<>();

    AbstractIdentifiable(String id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name != null ? name : id;
    }

    protected abstract String getTypeDescription();

    @Override
    public String getMessageHeader() {
        return getTypeDescription() + " '" + id + "': ";
    }

    @Override
    public boolean hasProperty() {
        return properties != null && properties.size() > 0;
    }

    @Override
    public Properties getProperties() {
        if (properties == null) {
            properties = new Properties();
        }
        return properties;
    }

    @Override
    public <E extends Extension<I>> void addExtension(Class<? super E> type, E extension) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(extension);
        extensions.put(type, extension);
        extensionsByName.put(extension.getName(), extension);
    }

    @Override
    public <E extends Extension<I>> E getExtension(Class<E> type) {
        Objects.requireNonNull(type);
        return (E) extensions.get(type);
    }

    @Override
    public <E extends Extension<I>> E getExtensionByName(String name) {
        Objects.requireNonNull(name);
        return (E) extensionsByName.get(name);
    }

    @Override
    public <E extends Extension<I>> boolean removeExtension(Class<E> type) {
        Objects.requireNonNull(type);
        return extensions.remove(type) != null;
    }

    @Override
    public Collection<Extension<I>> getExtensions() {
        return extensions.values();
    }

    @Override
    public String toString() {
        return id;
    }

}
