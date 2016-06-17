package net.morimekta.config.format;

import net.morimekta.config.Config;
import net.morimekta.config.ConfigException;
import net.morimekta.config.Sequence;
import net.morimekta.config.Value;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

/**
 * Format config into properties objects or files.
 */
public class PropertiesConfigFormat implements ConfigFormat {
    public PropertiesConfigFormat() {
    }

    @Override
    public void format(OutputStream out, Config config) throws IOException, ConfigException {
        Properties properties = format(config);
        properties.store(out, " generated by " + getClass().getName());
    }

    public Properties format(Config config) throws IOException, ConfigException {
        Properties properties = new Properties();
        formatTo("", config, properties);
        return properties;
    }

    @Override
    public Config parse(InputStream in) throws ConfigException, IOException {
        throw new IllegalStateException("not implemented");
    }

    // --- INTERNAL ---
    private void formatTo(String prefix, Config config, Properties properties) {
        for (Config.Entry entry : config.entrySet()) {
            String key = makeKey(prefix, entry.getKey());
            switch (entry.getValue().getType()) {
                case BOOLEAN:
                case NUMBER:
                case STRING:
                    properties.setProperty(key, entry.getValue().getValue().toString());
                    break;
                case SEQUENCE:
                    formatTo(key, entry.asSequence(), properties);
                    break;
                case CONFIG:
                    formatTo(key, entry.asConfig(), properties);
                    break;
            }
        }
    }

    private void formatTo(String prefix, Sequence sequence, Properties properties) {
        int i = 0;
        for (Value value : sequence.asValueArray()) {
            String key = makeKey(prefix, Integer.toString(i));
            switch (value.getType()) {
                case BOOLEAN:
                case NUMBER:
                case STRING:
                    properties.setProperty(key, value.getValue().toString());
                    break;
                case SEQUENCE:
                    formatTo(key, value.asSequence(), properties);
                    break;
                case CONFIG:
                    formatTo(key, value.asConfig(), properties);
                    break;
            }
            ++i;
        }
    }

    private String makeKey(String prefix, String key) {
        if (prefix.length() > 0) {
            return String.format("%s.%s", prefix, key);
        }
        return key;
    }
}
