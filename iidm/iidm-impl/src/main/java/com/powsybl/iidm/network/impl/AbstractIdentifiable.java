/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.extensions.AbstractExtendable;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Validable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
abstract class AbstractIdentifiable<I extends Identifiable<I>> extends AbstractExtendable<I> implements Identifiable<I>, Validable, MultiVariantObject {

    protected final String id;

    protected String name;

    protected boolean fictitious = false;

    protected Set<String> aliases = new TreeSet<>();

    protected final Properties properties = new Properties();

    AbstractIdentifiable(String id, String name) {
        this.id = id;
        this.name = name;
    }

    AbstractIdentifiable(String id, String name, boolean fictitious) {
        this(id, name);
        this.fictitious = fictitious;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Optional<String> getOptionalName() {
        return Optional.ofNullable(name);
    }

    @Override
    public String getNameOrId() {
        return name != null ? name : id;
    }

    @Override
    public Set<String> getAliases() {
        return Collections.unmodifiableSet(aliases);
    }

    @Override
    public void addAlias(String alias) {
        if (getNetwork().getIndex().addAlias(this, alias)) {
            aliases.add(alias);
        }
    }

    @Override
    public void removeAlias(String alias) {
        getNetwork().getIndex().removeAlias(this, alias);
        aliases.remove(alias);
    }

    @Override
    public boolean hasAliases() {
        return !aliases.isEmpty();
    }

    @Override
    public boolean isFictitious() {
        return fictitious;
    }

    @Override
    public void setFictitious(boolean fictitious) {
        boolean oldValue = this.fictitious;
        this.fictitious = fictitious;
        getNetwork().getListeners().notifyUpdate(this, "fictitious", oldValue, fictitious);
    }

    @Override
    public abstract NetworkImpl getNetwork();

    protected abstract String getTypeDescription();

    @Override
    public String getMessageHeader() {
        return getTypeDescription() + " '" + id + "': ";
    }

    public Properties getProperties() {
        return properties;
    }

    @Override
    public boolean hasProperty() {
        return !properties.isEmpty();
    }

    @Override
    public boolean hasProperty(String key) {
        return properties.containsKey(key);
    }

    @Override
    public String getProperty(String key) {
        Object val = properties.get(key);
        return val != null ? val.toString() : null;
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        Object val = properties.getOrDefault(key, defaultValue);
        return val != null ? val.toString() : null;
    }

    @Override
    public String setProperty(String key, String value) {
        String oldValue = (String) properties.put(key, value);
        if (Objects.isNull(oldValue)) {
            getNetwork().getListeners().notifyElementAdded(this, () -> "properties[" + key + "]", value);
        } else {
            getNetwork().getListeners().notifyElementReplaced(this, () -> "properties[" + key + "]", oldValue, value);
        }
        return oldValue;
    }

    @Override
    public Set<String> getPropertyNames() {
        return properties.keySet().stream().map(Object::toString).collect(Collectors.toSet());
    }

    @Override
    public String toString() {
        return id;
    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        getExtensions().stream()
                .filter(e -> e instanceof MultiVariantObject)
                .map(e -> (MultiVariantObject) e)
                .forEach(e -> e.extendVariantArraySize(initVariantArraySize, number, sourceIndex));
    }

    @Override
    public void reduceVariantArraySize(int number) {
        getExtensions().stream()
                .filter(e -> e instanceof MultiVariantObject)
                .map(e -> (MultiVariantObject) e)
                .forEach(e -> e.reduceVariantArraySize(number));
    }

    @Override
    public void deleteVariantArrayElement(int index) {
        getExtensions().stream()
                .filter(e -> e instanceof MultiVariantObject)
                .map(e -> (MultiVariantObject) e)
                .forEach(e -> e.deleteVariantArrayElement(index));
    }

    @Override
    public void allocateVariantArrayElement(int[] indexes, int sourceIndex) {
        getExtensions().stream()
                .filter(e -> e instanceof MultiVariantObject)
                .map(e -> (MultiVariantObject) e)
                .forEach(e -> e.allocateVariantArrayElement(indexes, sourceIndex));
    }
}
