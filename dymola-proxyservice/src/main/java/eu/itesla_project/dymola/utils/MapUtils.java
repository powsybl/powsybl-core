/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.dymola.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public final class MapUtils {
 
  public static class Entry<K, V> {
    private K key;
    private V value;
 
    public Entry(final K key, final V value) {
      this.key = key;
      this.value = value;
    }
 
    public K getKey() {
      return key;
    }
 
    public V getValue() {
      return value;
    }
  }
 
  public static <K, V> Entry<K, V> entry(final K key, final V value) {
    return new Entry(key, value);
  }
 
  public static <K, V> Map<K, V> asMap(final Entry<K, V>... entries) {
    return populate(new HashMap<K, V>(), entries);
  }
 
  public static <K, V> Map<K, V> asOrderedMap(final Entry<K, V>... entries) {
    return populate(new LinkedHashMap<K, V>(), entries);
  }
 
  public static <K, V> Map<K, V> asUnmodifiableMap(final Entry<K, V>... entries) {
    return Collections.unmodifiableMap(populate(new HashMap<K, V>(), entries));
  }
 
  public static <K, V> Map<K, V> asUnmodifiableOrderedMap(final Entry<K, V>... entries) {
    return Collections.unmodifiableMap(populate(new LinkedHashMap<K, V>(), entries));
  }
 
  private static <K, V> Map<K, V> populate(final Map map, final Entry<K, V>... entries) {
    for (final Entry entry : entries) {
      map.put(entry.getKey(), entry.getValue());
    }
    return map;
  }
}
