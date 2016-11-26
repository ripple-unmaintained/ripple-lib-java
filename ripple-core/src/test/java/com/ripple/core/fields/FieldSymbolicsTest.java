package com.ripple.core.fields;

import com.ripple.core.formats.Format;
import com.ripple.core.formats.LEFormat;
import com.ripple.core.formats.TxFormat;
import com.ripple.core.serialized.enums.EngineResult;
import com.ripple.core.serialized.enums.LedgerEntryType;
import com.ripple.core.serialized.enums.TransactionType;
import com.ripple.utils.TestHelpers;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;

import static org.junit.Assert.*;

public class FieldSymbolicsTest {
    @Test
    public void CheckProtocolDefinitions() throws FileNotFoundException {
        FileReader reader = TestHelpers.getResourceReader("protocol.json");
        JSONObject o = new JSONObject(new JSONTokener(reader));

        checkFields(o.getJSONArray("fields"));
        checkTransactionTypes(o.getJSONArray("transactions"));
        checkLedgerEntries(o.getJSONArray("ledgerEntries"));
        checkEngineResults(o.getJSONObject("engineResults"));
    }

    private void checkEngineResults(JSONObject engineResults) {
        Iterator keys = engineResults.keys();
        Map<String, EngineResult> results = new HashMap<String, EngineResult>();
        for (EngineResult r : EngineResult.values()) {
            results.put(r.name(), r);
            assertTrue("No old codes", engineResults.has(r.name()));
        }
//        TreeMap<Integer, String> sorted = new TreeMap<Integer, String>();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            JSONObject resultObj = engineResults.getJSONObject(key);
            int ordinal = resultObj.getInt("ordinal");
            String description = resultObj.getString("description");
            String decl = makeDeclarationLine(key, ordinal, description);
            assertTrue("missing " + decl, results.containsKey(key));
            EngineResult ter = results.get(key);
            assertEquals(decl, ter.asInteger(), ordinal);
            assertEquals(decl, ter.human, description);
//            sorted.put(ordinal, decl);
        }
//        for (String s : sorted.values()) {
//            System.out.println(s);
//        }
    }

    private String makeDeclarationLine(String key, int ordinal, String description) {
        if (ordinal >= 0 || Math.abs(ordinal) % 100 == 99) {
            return String.format(Locale.US, "%s(%d, \"%s\"),",
                    key, ordinal,
                    description);
        } else {
            return String.format(Locale.US, "%s(\"%s\"),", key, description);
        }
    }

    private void checkTransactionTypes(JSONArray txns) {
        assertEquals(txns.length(),
                TransactionType.values().length);

        for (int i = 0; i < txns.length(); i++) {
            JSONObject tx = txns.getJSONObject(i);
            String txName = tx.getString("name");
            if (!txName.isEmpty()) {
                try {
                    TransactionType.valueOf(txName);
                } catch (IllegalArgumentException e) {
                    fail("missing TransactionType " +
                              txName);
                }
                Format txFormat = TxFormat.fromString(txName);
                assertNotNull(txFormat);
                checkFormat(tx, txFormat);
            }
        }
    }

    private void checkFormat(JSONObject obj, Format format) {
        String txName = obj.getString("name");

        if (format == null) {
            throw new IllegalArgumentException();
        }
        EnumMap<Field, Format.Requirement> requirements = format.requirements();
        JSONArray fields = obj.getJSONArray("fields");

        for (int j = 0; j < fields.length(); j++) {
            JSONArray field = fields.getJSONArray(j);
            String fieldName = field.getString(0);
            String requirement = field.getString(1);

            Field key = Field.fromString(fieldName);
            if (!requirements.containsKey(key)) {
                fail(String.format("%s format missing %s %s %n",
                        txName, requirement, fieldName));
            } else {
                Format.Requirement req = requirements.get(key);
                if (!req.toString().equals(requirement)) {
                    fail(String.format("%s format missing %s %s %n",
                                txName, requirement, fieldName));

                }
            }
        }
        // check length is same, and if none are missing, must be equal ;)
        assertEquals(obj.toString(2),
                fields.length(), requirements.size());
    }

    private void checkLedgerEntries(JSONArray entries) {
        assertEquals(entries.length(), LedgerEntryType.values().length);
        for (int i = 0; i < entries.length(); i++) {
            JSONObject entryJson = entries.getJSONObject(i);
            String name = entryJson.getString("name");
            if (!name.isEmpty()) {
                try {
                    LedgerEntryType.valueOf(name);
                } catch (IllegalArgumentException e) {
                    fail("missing LedgerEntryType for " +
                            entryJson);
                }
                LEFormat format = LEFormat.fromString(name);
                assertNotNull(format);
                checkFormat(entryJson, format);
            }
        }
    }

    private void checkFields(JSONArray fields) {
        TreeSet<String> names = new TreeSet<String>();
        for (int i = 0; i < fields.length(); i++) {
            JSONObject fieldJson = fields.getJSONObject(i);
            String nam = fieldJson.getString("name");
            names.add(nam);
            if (!nam.isEmpty()) {
                try {
                    Field f = Field.valueOf(fieldJson.getString("name"));
                    Type t = Type.valueOf(fieldJson.getString("type"));
                    assertEquals(fieldJson.toString(2), f.type.id, t.id);
                    assertEquals(fieldJson.toString(2), f.id, fieldJson
                            .getInt("ordinal"));
                } catch (IllegalArgumentException e) {
                    fail("Can't find Field or Type for "
                            + fieldJson);
                }
            }
        }
        for (Field field : Field.values()) {
            if (field.isSerialized() && !names.contains(field.name())) {
                if (!((field == Field.ArrayEndMarker) ||
                        (field == Field.ObjectEndMarker)))
                    fail(field.toString());
            }
        }
    }
}
