package io.github.ulisse1996.jaorm.logger;

import java.io.InputStream;
import java.io.Reader;
import java.util.Map;
import java.util.Properties;

public final class UnmodifiableProperties extends Properties {

    private static final String CAN_T_MODIFY_PROPERTIES = "Can't modify properties";

    public UnmodifiableProperties(Properties properties) {
        super(properties);
    }

    @Override
    public synchronized Object put(Object key, Object value) {
        throw new UnsupportedOperationException(CAN_T_MODIFY_PROPERTIES);
    }

    @Override
    public synchronized Object putIfAbsent(Object key, Object value) {
        throw new UnsupportedOperationException(CAN_T_MODIFY_PROPERTIES);
    }

    @Override
    public synchronized void putAll(Map<?, ?> t) {
        throw new UnsupportedOperationException(CAN_T_MODIFY_PROPERTIES);
    }

    @Override
    public synchronized void load(Reader reader) {
        throw new UnsupportedOperationException(CAN_T_MODIFY_PROPERTIES);
    }

    @Override
    public synchronized void load(InputStream inStream) {
        throw new UnsupportedOperationException(CAN_T_MODIFY_PROPERTIES);
    }

    @Override
    public synchronized void loadFromXML(InputStream in) {
        throw new UnsupportedOperationException(CAN_T_MODIFY_PROPERTIES);
    }
}
