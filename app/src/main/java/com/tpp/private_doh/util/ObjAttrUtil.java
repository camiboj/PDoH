package com.tpp.private_doh.util;

import java.util.HashMap;
import java.util.Map;

public class ObjAttrUtil {
    private Map<Object, Map<String, Object>> objAttrs = new HashMap<>();

    public Object getAttr(Object obj, String k) {
        Map<String, Object> map = objAttrs.get(obj);
        if (map == null) {
            return null;
        }
        return map.get(k);
    }

    public void removeAttr(Object obj) {
        objAttrs.remove(obj);
    }

    public void setAttr(Object obj, String k, Object value) {
        Map<String, Object> map = objAttrs.get(obj);
        if (map == null) {
            objAttrs.put(obj, new HashMap<>());
            map = objAttrs.get(obj);
        }
        map.put(k, value);
    }
}

