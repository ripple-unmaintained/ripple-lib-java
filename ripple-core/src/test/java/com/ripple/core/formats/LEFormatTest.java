package com.ripple.core.formats;

import com.ripple.core.fields.Field;
import com.ripple.core.serialized.enums.LedgerEntryType;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import static junit.framework.TestCase.assertNotNull;

public class LEFormatTest {
    @Test
    public void testFromValue() throws Exception {

        LEFormat accountRoot = LEFormat.fromValue("AccountRoot");
        assertNotNull(accountRoot);
    }


//    @Test
    public void dumpSLEGetters() throws Exception {
        Set<Map.Entry<LedgerEntryType, LEFormat>> entries = LEFormat.formats.entrySet();
        JSONObject formats = new JSONObject();

        for (Map.Entry<LedgerEntryType, LEFormat> entry : entries) {
            LedgerEntryType key = entry.getKey();
            LEFormat value = entry.getValue();
            JSONArray array = new JSONArray();
            formats.put(key.toString(), array);
            array.put(key.asInteger());

            System.out.println("***********");
            System.out.println(key.toString());
            System.out.println("***********");

            EnumMap<Field, Format.Requirement> requirements = value.requirementEnumMap;
            for (Field field : requirements.keySet()) {
                if ((field == Field.LedgerIndex ||
                        field == Field.LedgerEntryType ||
                        field == Field.Flags ||
                        field == Field.PreviousTxnID ||
                        field == Field.PreviousTxnLgrSeq)) {
                    continue;
                }

                System.out.println(createGetter((field)));

//                JSONArray spec = new JSONArray();
//                spec.put(field.toString());
//                spec.put(requirements.get(field).toString());
//                spec.put(field.getId());
//                spec.put(field.getType().toString());
//                array.put(spec);
            }
            for (Field field : requirements.keySet()) {
                if ((field == Field.LedgerIndex ||
                        field == Field.LedgerEntryType ||
                        field == Field.Flags ||
                        field == Field.PreviousTxnID ||
                        field == Field.PreviousTxnLgrSeq)) {
                    continue;
                }

                System.out.println(createSetter((field)));

//                JSONArray spec = new JSONArray();
//                spec.put(field.toString());
//                spec.put(requirements.get(field).toString());
//                spec.put(field.getId());
//                spec.put(field.getType().toString());
//                array.put(spec);
            }
        }

        System.out.println(formats.toString(4));
//        assertNotNull(accountRoot);
    }



    private String createGetter(Field field) {
        String template = "public %s %s() {return get(%s.%s);}";
        //                    return type name
        return String.format(template, field.getType(), camelise(field.toString()), field.getType(), field);
    }
    private String createSetter(Field field) {
        String template = "public void %s(%s val) {put(Field.%s, val);}";
        //                    return type name
        return String.format(template, camelise(field.toString()), field.getType(), field);
    }

    private String camelise(String s) {
        return s.substring(0, 1).toLowerCase() + s.substring(1);
    }


    //    @Test
    public void testDumps() throws Exception {
        Set<Map.Entry<LedgerEntryType, LEFormat>> entries = LEFormat.formats.entrySet();
        JSONObject formats = new JSONObject();

        for (Map.Entry<LedgerEntryType, LEFormat> entry : entries) {
            LedgerEntryType key = entry.getKey();
            LEFormat value = entry.getValue();
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
