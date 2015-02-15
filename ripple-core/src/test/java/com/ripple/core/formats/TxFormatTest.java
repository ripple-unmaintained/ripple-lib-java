package com.ripple.core.formats;

import com.ripple.core.fields.Field;
import com.ripple.core.serialized.enums.TransactionType;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

public class TxFormatTest {
//        @Test
    public void dumpTxGetters() throws Exception {
        Set<Map.Entry<TransactionType, TxFormat>> entries = TxFormat.formats.entrySet();
        JSONObject formats = new JSONObject();

        for (Map.Entry<TransactionType, TxFormat> entry : entries) {
            TransactionType key = entry.getKey();
            TxFormat value = entry.getValue();
            JSONArray array = new JSONArray();
            formats.put(key.toString(), array);
            array.put(key.asInteger());

            System.out.println("***********");
            System.out.println(key.toString());
            System.out.println("***********");

            EnumMap<Field, Format.Requirement> requirements = value.requirementEnumMap;
            for (Field field : requirements.keySet()) {
                if (isCommonField(field)) {
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
                if (isCommonField(field)) {
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

    private boolean isCommonField(Field field) {
        return (field == Field.TransactionType ||
                field == Field.Account ||
                field == Field.Sequence ||
                field == Field.Fee ||
                field == Field.SigningPubKey ||
                field == Field.Flags ||
                field == Field.SourceTag ||
                field == Field.PreviousTxnID ||
                field == Field.OperationLimit ||
                field == Field.TxnSignature ||
                field == Field.AccountTxnID ||
                field == Field.LastLedgerSequence);
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
}
