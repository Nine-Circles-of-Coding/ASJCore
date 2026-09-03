package com.KAIIIAK.KASMLib.asm.commons;

import org.objectweb.asm.commons.Remapper;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A {@link Remapper} using a {@link Map} to define its mapping.
 *
 * @author Eugene Kuleshov
 */
public class KASMSimpleRemapper extends Remapper {

    public AtomicInteger changes = null;
    private final Map<String, String> mapping;

    public KASMSimpleRemapper(Map<String, String> mapping) {
        this.mapping = mapping;
    }

    public KASMSimpleRemapper(String oldName, String newName) {
        this.mapping = Collections.singletonMap(oldName, newName);
    }

    @Override
    public String mapMethodName(String owner, String name, String desc) {
        String s = map(owner + '.' + name + desc);
        return s == null ? name : s;
    }

    @Override
    public String mapFieldName(String owner, String name, String desc) {
        String s = map(owner + '.' + name);
        return s == null ? name : s;
    }

    @Override
    public String map(String key) {
        String value = mapping.get(key);
        if (value == null) return null;
        if (!value.equals(key)) {
            if (changes != null) changes.incrementAndGet();
            return value;
        }
        return key;
    }
}
