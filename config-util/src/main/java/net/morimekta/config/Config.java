package net.morimekta.config;

import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Base configuration object. Essentially a type-safe map from a string key that
 * can look up more than one level into the map (if referencing config objects
 * within the config object). This way, if the config contains a config object
 * on key 'b', then getString('b.c') will look for key 'c' in the config 'b'. E.g.:
 *
 * <code>
 * Value v = getValue("up.args");
 * </code>
 *
 * Will first navigate one step "up", then try to find the value "args".
 * Otherwise looking up a key is equivalent to a series of
 * {@link #getConfig(String)} and ${@link #getValue(String)} at the end.
 * E.g. these two calls are equivalent.
 *
 * <code>
 * Value v = getValue("arg1.arg2.arg3");
 * Value v = getConfig("arg1").getConfig("arg2").getValue("arg3");
 * </code>
 *
 * It is not implementing the Map base class since it would require also
 * implementing generic entry adders (put, putAll), and type unsafe getters.
 */
public abstract class Config {
    public static final String UP = "up";

    interface Entry extends Comparable<Entry> {
        /**
         * Get the entry key.
         *
         * @return The key string.
         */
        String getKey();

        /**
         * Get the type of value in the entry.
         *
         * @return The value type.
         */
        default Value.Type getType() {
            return getValue().type;
        }

        /**
         * Get the entry value..
         * @return The value.
         */
        Value getValue();

        /**
         * Get the entry value as string.
         * @return The value string.
         * @throws IncompatibleValueException If the value could not be
         *         converted.
         */
        default String asString() {
            return getValue().asString();
        }

        /**
         * Get the entry value as boolean.
         * @return The boolean value.
         * @throws IncompatibleValueException If the value could not be
         *         converted.
         */
        default boolean asBoolean() {
            return getValue().asBoolean();
        }

        /**
         * Get the entry value as integer.
         * @return The integer value.
         * @throws IncompatibleValueException If the value could not be
         *         converted.
         */
        default int asInteger() {
            return getValue().asInteger();
        }

        /**
         * Get the entry value as long.
         * @return The long value.
         * @throws IncompatibleValueException If the value could not be
         *         converted.
         */
        default long asLong() {
            return getValue().asLong();
        }

        /**
         * Get the entry value as double.
         * @return The double value.
         * @throws IncompatibleValueException If the value could not be
         *         converted.
         */
        default double asDouble() {
            return getValue().asDouble();
        }

        /**
         * Get the entry value as config.
         * @return The config value.
         * @throws IncompatibleValueException If the value could not be
         *         converted.
         */
        default Config asConfig() {
            return getValue().asConfig();
        }

        /**
         * Get the entry value as sequence.
         * @return The sequence value.
         * @throws IncompatibleValueException If the value could not be
         *         converted.
         */
        default Sequence asSequence() {
            return getValue().asSequence();
        }

        @Override
        default int compareTo(Entry other) {
            return getKey().compareTo(other.getKey());
        }
    }

    protected static class ImmutableEntry implements Entry {
        private final String key;
        private final Value value;

        protected ImmutableEntry(String key, Value value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public Value getValue() {
            return value;
        }
    }

    /**
     * @return True if the config set is empty.
     */
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * @return The number of entries in the config.
     */
    public abstract int size();

    public abstract Config getParent();

    public abstract Set<String> keySet();

    public abstract Set<Entry> entrySet();

    public Stream<String> keyStream() {
        return StreamSupport.stream(keySpliterator(), false);
    }

    protected int spliteratorCapabilities() {
        return Spliterator.DISTINCT |
               Spliterator.NONNULL |
               Spliterator.SIZED |
               Spliterator.SUBSIZED;
    }

    public Spliterator<String> keySpliterator() {
        return Spliterators.spliterator(keySet(), spliteratorCapabilities());
    }

    public Stream<Entry> entryStream() {
        return StreamSupport.stream(entrySpliterator(), false);
    }

    public Spliterator<Entry> entrySpliterator() {
        return Spliterators.spliterator(entrySet(), spliteratorCapabilities());
    }

    /**
     * Checks if the key prefix exists deeply in the config. Also supports 'up'
     * and 'super' navigation, unless the config instance also contains the key
     * "up" or "super".
     *
     * @param key The prefix to look for.
     * @return The
     */
    public abstract boolean containsKey(String key);

    /**
     * Get the value type of the value for the key.
     *
     * @param key The key to look up.
     * @return The value type or null if not found.
     */
    public Value.Type typeOf(String key) {
        return getValue(key).type;
    }

    /**
     * @param key The simple key to look for.
     * @return The string value.
     * @throws KeyNotFoundException When the key does not exist.
     * @throws IncompatibleValueException When a value cannot be converted to
     *         requested type.
     */
    public String getString(String key) {
        return getValue(key).asString();
    }

    /**
     * @param key The simple key to look for.
     * @param def The default value if not found.
     * @return The string value.
     * @throws IncompatibleValueException When a value cannot be converted to
     *         requested type.
     */
    public String getString(String key, String def) {
        if (containsKey(key)) {
            return getValue(key).asString();
        }
        return def;
    }

    /**
     * @param key The simple key to look for.
     * @return The boolean value.
     * @throws KeyNotFoundException When the key does not exist.
     * @throws IncompatibleValueException When a value cannot be converted to
     *         requested type.
     */
    public boolean getBoolean(String key) {
        return getValue(key).asBoolean();
    }

    /**
     * @param key The simple key to look for.
     * @param def The default value if not found.
     * @return The boolean value.
     * @throws IncompatibleValueException When a value cannot be converted to
     *         requested type.
     */
    public boolean getBoolean(String key, boolean def) {
        if (containsKey(key)) {
            return getValue(key).asBoolean();
        }
        return def;
    }

    /**
     * @param key The simple key to look for.
     * @return The integer value.
     * @throws KeyNotFoundException When the key does not exist.
     * @throws IncompatibleValueException When a value cannot be converted to
     *         requested type.
     */
    public int getInteger(String key) {
        return getValue(key).asInteger();
    }

    /**
     * @param key The simple key to look for.
     * @param def The default value if not found.
     * @return The integer value.
     * @throws IncompatibleValueException When a value cannot be converted to
     *         requested type.
     */
    public int getInteger(String key, int def) {
        if (containsKey(key)) {
            return getValue(key).asInteger();
        }
        return def;
    }

    /**
     * @param key The simple key to look for.
     * @return The long value.
     * @throws KeyNotFoundException When the key does not exist.
     * @throws IncompatibleValueException When a value cannot be converted to
     *         requested type.
     */
    public long getLong(String key) {
        return getValue(key).asLong();
    }

    /**
     * @param key The simple key to look for.
     * @param def The default value if not found.
     * @return The long value.
     * @throws IncompatibleValueException When a value cannot be converted to
     *         requested type.
     */
    public long getLong(String key, long def) {
        if (containsKey(key)) {
            return getValue(key).asLong();
        }
        return def;
    }

    /**
     * @param key The simple key to look for.
     * @return The double value.
     * @throws KeyNotFoundException When the key does not exist.
     * @throws IncompatibleValueException When a value cannot be converted to
     *         requested type.
     */
    public double getDouble(String key) {
        return getValue(key).asDouble();
    }

    /**
     * @param key The simple key to look for.
     * @param def The default value if not found.
     * @return The double value.
     * @throws IncompatibleValueException When a value cannot be converted to
     *         requested type.
     */
    public double getDouble(String key, double def) {
        if (containsKey(key)) {
            return getValue(key).asDouble();
        }
        return def;
    }

    /**
     * @param key The simple key to look for.
     * @return The sequence value.
     * @throws KeyNotFoundException When the key does not exist.
     * @throws IncompatibleValueException When a value cannot be converted to
     *         requested type.
     */
    public Sequence getSequence(String key) {
        return getValue(key).asSequence();
    }

    /**
     * @param key The simple key to look for.
     * @return The config value.
     * @throws KeyNotFoundException When the key does not exist.
     * @throws IncompatibleValueException When a value cannot be converted to
     *         requested type.
     */
    public Config getConfig(String key) {
        return getValue(key).asConfig();
    }

    /**
     * Get a value from the config looking up deeply into the config. It can also look
     * "up" from the object. The "up" context is always the same for the same config
     * instance. E.g.
     *
     * @param key The key to look up.
     * @return The value.
     * @throws KeyNotFoundException If not found.
     */
    public abstract Value getValue(String key);
}
