package org.excelsi.nausicaa.ca;


import java.util.HashMap;
import java.util.Map;


public final class PipelineContext {
    private final Map<String,Object> _ctx = new HashMap<>();


    public PipelineContext() {
    }

    public PipelineContext(String name, Object o) {
        set(name, o);
    }

    public PipelineContext with(String name, Object o) {
        set(name, o);
        return this;
    }

    public <T> T get(final String name) {
        return (T) _ctx.get(name);
    }

    public <T> T set(final String name, final T t) {
        _ctx.put(name, t);
        return t;
    }
}
