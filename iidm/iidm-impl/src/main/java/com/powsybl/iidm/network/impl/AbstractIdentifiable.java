/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.google.common.base.Strings;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtendable;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Validable;
import com.powsybl.iidm.network.util.Identifiables;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
abstract class AbstractIdentifiable<I extends Identifiable<I>> extends AbstractExtendable<I> implements Identifiable<I>, Validable, MultiVariantObject {

    protected String id;

    protected String name;

    protected boolean fictitious = false;

    protected final Properties properties = new Properties();

    private final Set<String> aliasesWithoutType = new HashSet<>();
    private final Map<String, String> aliasesByType = new HashMap<>();

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

    void replaceId(String newId) {
        throw new PowsyblException("Cannot change ID");
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
    public I setName(String name) {
        String oldName = this.name;
        this.name = name;
        getNetwork().getListeners().notifyUpdate(this, "name", oldName, name);
        return (I) this;
    }

    @Override
    public Set<String> getAliases() {
        Set<String> aliases = new HashSet<>();
        aliases.addAll(aliasesWithoutType);
        aliases.addAll(aliasesByType.values());
        return Collections.unmodifiableSet(aliases);
    }

    @Override
    public Optional<String> getAliasType(String alias) {
        Objects.requireNonNull(alias);
        if (aliasesWithoutType.contains(alias)) {
            return Optional.empty();
        }
        return aliasesByType.entrySet().stream().filter(entry -> entry.getValue().equals(alias)).map(Map.Entry::getKey).findFirst();
    }

    @Override
    public Optional<String> getAliasFromType(String aliasType) {
        if (Strings.isNullOrEmpty(aliasType)) {
            throw new PowsyblException("Alias type must not be null or empty");
        }
        return Optional.ofNullable(aliasesByType.get(aliasType));
    }

    @Override
    public void addAlias(String alias) {
        addAlias(alias, false);
    }

    @Override
    public void addAlias(String alias, boolean ensureAliasUnicity) {
        addAlias(alias, null, ensureAliasUnicity);
    }

    @Override
    public void addAlias(String alias, String aliasType) {
        addAlias(alias, aliasType, false);
    }

    @Override
    public void addAlias(String alias, String aliasType, boolean ensureAliasUnicity) {
        Objects.requireNonNull(alias);
        String uniqueAlias = alias;
        if (ensureAliasUnicity) {
            uniqueAlias = Identifiables.getUniqueId(alias, getNetwork().getIndex()::contains);
        }
        if (!Strings.isNullOrEmpty(aliasType) && aliasesByType.containsKey(aliasType)) {
            throw new PowsyblException(id + " already has an alias of type " + aliasType);
        }
        if (getNetwork().getIndex().addAlias(this, uniqueAlias)) {
            if (Strings.isNullOrEmpty(aliasType)) {
                aliasesWithoutType.add(uniqueAlias);
            } else {
                aliasesByType.put(aliasType, uniqueAlias);
            }
        }
    }

    @Override
    public void removeAlias(String alias) {
        Objects.requireNonNull(alias);
        getNetwork().getIndex().removeAlias(this, alias);
        String type = aliasesByType.entrySet().stream().filter(entry -> entry.getValue().equals(alias)).map(Map.Entry::getKey).filter(Objects::nonNull).findFirst().orElse(null);
        if (Strings.isNullOrEmpty(type)) {
            aliasesWithoutType.remove(alias);
        } else {
            aliasesByType.remove(type);
        }
    }

    @Override
    public boolean hasAliases() {
        return !aliasesWithoutType.isEmpty() || !aliasesByType.isEmpty();
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
            getNetwork().getListeners().notifyElementAdded(this, () -> getPropertyStringForNotification(key), value);
        } else {
            getNetwork().getListeners().notifyElementReplaced(this, () -> getPropertyStringForNotification(key), oldValue, value);
        }
        return oldValue;
    }

    @Override
    public boolean removeProperty(String key) {
        Object oldValue = properties.remove(key);
        if (oldValue != null) {
            getNetwork().getListeners().notifyElementRemoved(this, () -> getPropertyStringForNotification(key), oldValue);
            return true;
        }
        return false;
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

    @Override
    public <E extends Extension<I>> boolean removeExtension(Class<E> type) {
        E extension = getExtension(type);
        NetworkListenerList listeners = getNetwork().getListeners();
        if (extension != null) {
            listeners.notifyExtensionBeforeRemoval(extension);
            removeExtension(type, extension);
            listeners.notifyExtensionAfterRemoval(this, extension.getName());
            return true;
        }
        return false;
    }

    private String getPropertyStringForNotification(String key) {
        return "properties[" + key + "]";
    }
}
