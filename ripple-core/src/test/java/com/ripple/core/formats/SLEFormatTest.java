package com.ripple.core.formats;

import com.ripple.core.enums.LedgerEntryType;
import com.ripple.core.fields.Field;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.omg.DynamicAny._DynArrayStub;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import static junit.framework.TestCase.assertNotNull;

public class SLEFormatTest {
    @Test
    public void testFromValue() throws Exception {

        SLEFormat accountRoot = SLEFormat.fromValue("AccountRoot");
        assertNotNull(accountRoot);
    }

//    @Test
    public void testDumps() throws Exception {
        Set<Map.Entry<LedgerEntryType, SLEFormat>> entries = SLEFormat.formats.entrySet();
        JSONObject formats = new JSONObject();

        for (Map.Entry<LedgerEntryType, SLEFormat> entry : entries) {
            LedgerEntryType key = entry.getKey();
            SLEFormat value = entry.getValue();
            JSONArray array = new JSONArray();
            formats.put(key.toString(), array);
            array.put(key.asInteger());

            EnumMap<Field, Format.Requirement> requirements = value.requirementEnumMap;
            for (Field field : requirements.keySet()) {
                JSONArray spec = new JSONArray();
                spec.put(field.toString());
                spec.put(requirements.get(field).toString());
                spec.put(field.getId());
                spec.put(field.getType().toString());
                array.put(spec);
            }
        }

        System.out.println(formats.toString(4));
//        assertNotNull(accountRoot);
    }
}
