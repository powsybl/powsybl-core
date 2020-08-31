/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtendable;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Validable;

import java.util.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
abstract class AbstractIdentifiable<I extends Identifiable<I>> extends AbstractExtendable<I> implements Identifiable<I>, Validable, MultiVariantObject {

    protected final String id;

    protected String name;

    protected boolean fictitious = false;

    protected final Map<String, Property> properties = new HashMap<>();

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
        Objects.requireNonNull(aliasType);
        return Optional.ofNullable(aliasesByType.get(aliasType));
    }

    @Override
    public void addAlias(String alias) {
        addAlias(alias, null);
    }

    @Override
    public void addAlias(String alias, String aliasType) {
        Objects.requireNonNull(alias);
        if (aliasType != null && aliasesByType.containsKey(aliasType)) {
            throw new PowsyblException(id + " already has an alias of type " + aliasType);
        }
        if (getNetwork().getIndex().addAlias(this, alias)) {
            if (aliasType != null) {
                aliasesByType.put(aliasType, alias);
            } else {
                aliasesWithoutType.add(alias);
            }
        }
    }

    @Override
    public void removeAlias(String alias) {
        Objects.requireNonNull(alias);
        getNetwork().getIndex().removeAlias(this, alias);
        String type = aliasesByType.entrySet().stream().filter(entry -> entry.getValue().contains(alias)).map(Map.Entry::getKey).filter(Objects::nonNull).findFirst().orElse(null);
        if (type != null) {
            aliasesByType.remove(type);
        } else {
            aliasesWithoutType.remove(alias);
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

    @Override
    public boolean hasProperty() {
        return !properties.isEmpty();
    }

    @Override
    public boolean hasProperty(String key) {
        return properties.containsKey(key);
    }

    @Override
    public Set<String> getPropertyNames() {
        return properties.keySet();
    }

    @Override
    public PropertyType getPropertyType(String key) {
        if (properties.containsKey(key)) {
            return properties.get(key).propertyType();
        }
        return null;
    }

    @Override
    public String getStringProperty(String key) {
        return getStringProperty(key, null);
    }

    @Override
    public String getStringProperty(String key, String defaultValue) {
        if (properties.containsKey(key)) {
            return properties.get(key).stringValue();
        }
        return defaultValue;
    }

    @Override
    public Optional<String> getOptionalStringProperty(String key) {
        return Optional.ofNullable(getStringProperty(key));
    }

    @Override
    public String setStringProperty(String key, String value) {
        notifyElementModification(key, value);
        Property property = properties.put(key, new Property(value));
        if (property != null && property.propertyType() == PropertyType.STRING) {
            return property.stringValue();
        }
        return null;
    }

    @Override
    public int getIntegerProperty(String key) {
        return getIntegerProperty(key, 0);
    }

    @Override
    public int getIntegerProperty(String key, int defaultValue) {
        if (properties.containsKey(key)) {
            return properties.get(key).intValue();
        }
        return defaultValue;
    }

    @Override
    public OptionalInt getOptionalIntegerProperty(String key) {
        return OptionalInt.of(getIntegerProperty(key));
    }

    @Override
    public int setIntegerProperty(String key, int value) {
        notifyElementModification(key, value);
        Property property = properties.put(key, new Property(value));
        if (property != null && property.propertyType() == PropertyType.INTEGER) {
            return property.intValue();
        }
        return 0;
    }

    @Override
    public double getDoubleProperty(String key) {
        return getDoubleProperty(key, Double.NaN);
    }

    @Override
    public double getDoubleProperty(String key, double defaultValue) {
        if (properties.containsKey(key)) {
            return properties.get(key).doubleValue();
        }
        return defaultValue;
    }

    @Override
    public OptionalDouble getOptionalDoubleProperty(String key) {
        return OptionalDouble.of(getDoubleProperty(key));
    }

    @Override
    public double setDoubleProperty(String key, double value) {
        notifyElementModification(key, value);
        Property property = properties.put(key, new Property(value));
        if (property != null && property.propertyType() == PropertyType.DOUBLE) {
            return property.doubleValue();
        }
        return Double.NaN;
    }

    @Override
    public boolean getBooleanProperty(String key) {
        return getBooleanProperty(key, false);
    }

    @Override
    public boolean getBooleanProperty(String key, boolean defaultValue) {
        if (properties.containsKey(key)) {
            return properties.get(key).booleanValue();
        }
        return defaultValue;
    }

    @Override
    public Optional<Boolean> getOptionalBooleanProperty(String key) {
        return Optional.of(getBooleanProperty(key));
    }

    @Override
    public boolean setBooleanProperty(String key, boolean value) {
        notifyElementModification(key, value);
        Property property = properties.put(key, new Property(value));
        if (property != null && property.propertyType() == PropertyType.BOOLEAN) {
            return property.booleanValue();
        }
        return false;
    }

    private void notifyElementModification(String key, Object value) {
        if (properties.containsKey(key)) {
            getNetwork().getListeners().notifyElementReplaced(this, () -> "properties[" + key + "]", properties.get(key).getValue(), value);
        } else {
            getNetwork().getListeners().notifyElementAdded(this, () -> "properties[" + key + "]", value);
        }
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
