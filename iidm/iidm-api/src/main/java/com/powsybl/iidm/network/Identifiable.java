/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

import java.util.*;

import com.powsybl.commons.extensions.Extendable;

/**
 * An object that is part of the network model and that is identified uniquely
 * by a <code>String</code> id.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface Identifiable<I extends Identifiable<I>> extends Extendable<I> {

    enum PropertyType {
        STRING("string"),
        INTEGER("integer"),
        DOUBLE("double"),
        BOOLEAN("boolean");

        private String name;
        PropertyType(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * Get the network associated to the object.
     */
    Network getNetwork();

    /**
     * Get the unique identifier of the object.
     */
    String getId();

    /**
     * Get the name of the object if it exists. If not, get the unique identifier of the object.
     *
     * @deprecated Use {@link #getNameOrId()} or {@link #getOptionalName()} instead.
     */
    @Deprecated
    default String getName() {
        return getNameOrId();
    }

    /**
     * Get the aliases of the object.
     */
    default Set<String> getAliases() {
        return Collections.emptySet();
    }

    /**
     * Get the alias of the object with a given alias type if it exists. Else return an empty optional.
     */
    default Optional<String> getAliasFromType(String aliasType) {
        return Optional.empty();
    }

    /**
     * Get the alias type of an alias if it exists, an empty optional otherwise.
     * If the alias does not exist, return an empty optional.
     */
    default Optional<String> getAliasType(String alias) {
        return Optional.empty();
    }

    /**
     * Add an alias to the object. Aliases must be unique in associated Network, and different
     * from any identifiable ID. No alias type is associated to this alias.
     *
     * If the alias already exists (i.e. is not unique) or equals an identifiable ID, throw a {@link com.powsybl.commons.PowsyblException}
     */
    default void addAlias(String alias) {
    }

    /**
     * Add an alias to the object. Aliases must be unique in associated Network, and different
     * from any identifiable ID. This alias is associated to a given alias type.
     * If the given alias type is null, no alias type is considered associated to the alias.
     * Only one alias can be associated to a non null given alias type for one object.
     *
     * If the alias already exists (i.e. is not unique) or equals an identifiable ID, throw a {@link com.powsybl.commons.PowsyblException}
     * If a given non null type already has an associated alias for this object, throw a {@link com.powsybl.commons.PowsyblException}
     */
    default void addAlias(String alias, String aliasType) {
    }

    /**
     * Remove an alias of the object.
     *
     * If the alias does not exist for this object, throw a {@link com.powsybl.commons.PowsyblException}
     */
    default void removeAlias(String alias) {
    }

    /**
     * Return true if identifiable has aliases.
     */
    default boolean hasAliases() {
        return false;
    }

    /**
     * Return an optional containing the name  of the object if it exists. If not, return an empty optional.
     */
    default Optional<String> getOptionalName() {
        throw new UnsupportedOperationException();
    }

    /**
     * Get the name of the object if it exists. If not, get the unique identifier of the object.
     */
    default String getNameOrId() {
        return getOptionalName().orElseGet(this::getId);
    }

    /**
     * Check that this object has some properties.
     */
    boolean hasProperty();

    /**
     * Get the type of the property.
     */
    default PropertyType getPropertyType(String key) {
        return PropertyType.STRING;
    }


    /**
     * Check that this object has property with specified name.
     */
    boolean hasProperty(String key);

    /**
     * Get property associated to specified key.
     *
     * @deprecated Use {@link #getStringProperty(String)} instead.
     */
    @Deprecated
    default String getProperty(String key) {
        return getStringProperty(key);
    }

    /**
     * Get property associated to specified key, with default value.
     *
     * @deprecated Use {@link #getStringProperty(String, String)} instead.
     */
    @Deprecated
    default String getProperty(String key, String defaultValue) {
        return getStringProperty(key, defaultValue);
    }

    /**
     * Set property value associated to specified key.
     *
     * @deprecated Use {@link #setStringProperty(String, String)} instead.
     */
    @Deprecated
    default String setProperty(String key, String value) {
        return setStringProperty(key, value);
    }

    /**
     * Get string property associated to specified key.
     */
    String getStringProperty(String key);

    /**
     * Get string property associated to specified key, with default value.
     */
    String getStringProperty(String key, String defaultValue);

    /**
     * Get string property associated to specified key.
     */
    Optional<String> getOptionalStringProperty(String key);

    /**
     * Set string property value associated to specified key.
     */
    String setStringProperty(String key, String value);

    /**
     * Get integer property associated to specified key.
     */
    default OptionalInt getOptionalIntegerProperty(String key) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    default int getIntegerProperty(String key) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Get integer property associated to specified key, with default value.
     */
    default int getIntegerProperty(String key, int defaultValue) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Get integer property associated to specified key.
     */
    default int setIntegerProperty(String key, int value) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Get double property associated to specified key.
     */
    default double getDoubleProperty(String key) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Get double property associated to specified key, with default value.
     */
    default double getDoubleProperty(String key, double defaultValue) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Get double property associated to specified key.
     */
    default OptionalDouble getOptionalDoubleProperty(String key) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Set double property value associated to specified key.
     */
    default double setDoubleProperty(String key, double value) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Get boolean property associated to specified key.
     */
    default boolean getBooleanProperty(String key) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Get boolean property associated to specified key, with default value.
     */
    default boolean getBooleanProperty(String key, boolean defaultValue) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Get boolean property associated to specified key.
     */
    default Optional<Boolean> getOptionalBooleanProperty(String key) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Set boolean property value associated to specified key.
     */
    default boolean setBooleanProperty(String key, boolean value) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Get properties key values.
     */
    Set<String> getPropertyNames();

    /**
     * Get the fictitious status
     */
    default boolean isFictitious() {
        return false;
    }

    /**
     * Set the fictitious status
     */
    default void setFictitious(boolean fictitious) {
        throw new UnsupportedOperationException();
    }
}
