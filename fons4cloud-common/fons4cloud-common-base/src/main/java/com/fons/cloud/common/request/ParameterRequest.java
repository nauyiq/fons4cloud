package com.fons.cloud.common.request;

import cn.hutool.core.map.MapUtil;
import lombok.Getter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author hongqy
 */
@Getter
public class ParameterRequest implements Serializable {

    private final Map<String, Object> parameters = new HashMap<>();

    public void addParameter(String key, Object value) {
        this.parameters.put(key, value);
    }

    public void addParameters(Map<String, Object> parameters) {
        this.parameters.putAll(parameters);
    }

    public Object getString(String key) {
        return getString(key, null);
    }

    public String getString(String key, String defaultValue) {
        return (String) this.parameters.getOrDefault(key, defaultValue);
    }

    public Integer getInteger(String key, Integer defaultValue) {
        return MapUtil.getInt(this.parameters, key, defaultValue);
    }

    public Boolean getBoolean(String key, Boolean defaultValue) {
        return MapUtil.getBool(this.parameters, key, defaultValue);
    }


    @SuppressWarnings("unchecked")
    public <T> T getObj(String key, T defaultValue) {
        return (T) this.parameters.getOrDefault(key, defaultValue);
    }

}
