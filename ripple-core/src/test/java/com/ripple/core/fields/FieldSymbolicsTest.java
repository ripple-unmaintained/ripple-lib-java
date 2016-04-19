package com.ripple.core.fields;

import com.ripple.encodings.common.B16;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

public class FieldSymbolicsTest {

    // @Test
    public void dumpFieldsJSON() {
        Field[] values = Field.values();
        JSONArray obj = new JSONArray();
        for (Field f : values) {
            JSONArray tuple = new JSONArray();
            tuple.put(f.name());
            JSONObject definition = new JSONObject();
            definition.put("nth", f.id);
            definition.put("type", f.getType().toString());
            definition.put("bytes", B16.encode(f.getBytes()));
            tuple.put(definition);
            obj.put(tuple);
        }

        System.out.println(obj.toString(2));
    }


//    @Test
//    public void testIsSymbolicField() throws Exception {
//        assertTrue(FieldSymbolics.isSymbolicField(Field.LedgerEntryType));
//    }

//    @Test
    public void testAsInteger() throws Exception {
        /*assertNotNull(FieldSymbolics.asInteger(Field.LedgerEntryType, "AccountRoot"));
        assertEquals((int) 'a', (int) FieldSymbolics.asInteger(Field.LedgerEntryType, "AccountRoot"));*/
    }
}
