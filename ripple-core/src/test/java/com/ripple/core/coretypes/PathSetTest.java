package com.ripple.core.coretypes;

import org.json.JSONArray;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;


public class PathSetTest {
    @Test
    public void testSerialization() throws Exception {
        String jsonPathSet = "[\n" +
                "          [\n" +
                "            {\n" +
                "              \"account\": \"r9hEDb4xBGRfBCcX3E4FirDWQBAYtpxC8K\",\n" +
                "              \"currency\": \"BTC\",\n" +
                "              \"issuer\": \"r9hEDb4xBGRfBCcX3E4FirDWQBAYtpxC8K\",\n" +
                "              \"type\": 49,\n" +
                "              \"type_hex\": \"0000000000000031\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"account\": \"rM1oqKtfh1zgjdAgbFmaRm3btfGBX25xVo\",\n" +
                "              \"currency\": \"BTC\",\n" +
                "              \"issuer\": \"rM1oqKtfh1zgjdAgbFmaRm3btfGBX25xVo\",\n" +
                "              \"type\": 49,\n" +
                "              \"type_hex\": \"0000000000000031\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"account\": \"rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59B\",\n" +
                "              \"currency\": \"BTC\",\n" +
                "              \"issuer\": \"rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59B\",\n" +
                "              \"type\": 49,\n" +
                "              \"type_hex\": \"0000000000000031\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"currency\": \"USD\",\n" +
                "              \"issuer\": \"rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59B\",\n" +
                "              \"type\": 48,\n" +
                "              \"type_hex\": \"0000000000000030\"\n" +
                "            }\n" +
                "          ],\n" +
                "          [\n" +
                "            {\n" +
                "              \"account\": \"r9hEDb4xBGRfBCcX3E4FirDWQBAYtpxC8K\",\n" +
                "              \"currency\": \"BTC\",\n" +
                "              \"issuer\": \"r9hEDb4xBGRfBCcX3E4FirDWQBAYtpxC8K\",\n" +
                "              \"type\": 49,\n" +
                "              \"type_hex\": \"0000000000000031\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"account\": \"rM1oqKtfh1zgjdAgbFmaRm3btfGBX25xVo\",\n" +
                "              \"currency\": \"BTC\",\n" +
                "              \"issuer\": \"rM1oqKtfh1zgjdAgbFmaRm3btfGBX25xVo\",\n" +
                "              \"type\": 49,\n" +
                "              \"type_hex\": \"0000000000000031\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"account\": \"rpvfJ4mR6QQAeogpXEKnuyGBx8mYCSnYZi\",\n" +
                "              \"currency\": \"BTC\",\n" +
                "              \"issuer\": \"rpvfJ4mR6QQAeogpXEKnuyGBx8mYCSnYZi\",\n" +
                "              \"type\": 49,\n" +
                "              \"type_hex\": \"0000000000000031\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"currency\": \"USD\",\n" +
                "              \"issuer\": \"rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59B\",\n" +
                "              \"type\": 48,\n" +
                "              \"type_hex\": \"000000000000000\"\n" +
                "            }\n" +
                "          ],\n" +
                "          [\n" +
                "            {\n" +
                "              \"account\": \"r9hEDb4xBGRfBCcX3E4FirDWQBAYtpxC8K\",\n" +
                "              \"currency\": \"BTC\",\n" +
                "              \"issuer\": \"r9hEDb4xBGRfBCcX3E4FirDWQBAYtpxC8K\",\n" +
                "              \"type\": 49,\n" +
                "              \"type_hex\": \"0000000000000031\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"account\": \"r3AWbdp2jQLXLywJypdoNwVSvr81xs3uhn\",\n" +
                "              \"currency\": \"BTC\",\n" +
                "              \"issuer\": \"r3AWbdp2jQLXLywJypdoNwVSvr81xs3uhn\",\n" +
                "              \"type\": 49,\n" +
                "              \"type_hex\": \"0000000000000031\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"currency\": \"0000000000000000000000005852500000000000\",\n" +
                "              \"type\": 16,\n" +
                "              \"type_hex\": \"0000000000000010\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"currency\": \"USD\",\n" +
                "              \"issuer\": \"rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59B\",\n" +
                "              \"type\": 48,\n" +
                "              \"type_hex\": \"0000000000000030\"\n" +
                "            }\n" +
                "          ]\n" +
                "        ]";


        PathSet.Translator translator = PathSet.translate;
        PathSet paths = translator.fromJSONArray(new JSONArray(jsonPathSet));
        String hex = paths.toHex();

//        System.out.println(hex);

        // This is taken from a certain transaction
        //  "hash": "0CBB429C456ED999CC691DFCC8E62E8C8C7E9522C2BEA967FED0D7E2A9B28D13",
        //  "ledger_index": 448052,

        String rippleDHex = "1200002200000000240000002E2E00004BF161D4C71AFD498D000000000000000000000000000055534400000000000A20B3C85F482532A9578DBB3950B85CA06594D168400000000000000A69D446F8038585E940000000000000000000000000425443000000000078CA21A6014541AB7B26C3929B9E0CD8C284D61C732103A4665B1F0B7AE2BCA12E2DB80A192125BBEA660F80E9CEE137BA444C1B0769EC7447304502205A964536805E35785C659D1F9670D057749AE39668175D6AA75D25B218FE682E0221009252C0E5DDD5F2712A48F211669DE17B54113918E0D2C266F818095E9339D7D3811478CA21A6014541AB7B26C3929B9E0CD8C284D61C83140A20B3C85F482532A9578DBB3950B85CA06594D1011231585E1F3BD02A15D6185F8BB9B57CC60DEDDB37C10000000000000000000000004254430000000000585E1F3BD02A15D6185F8BB9B57CC60DEDDB37C131E4FE687C90257D3D2D694C8531CDEECBE84F33670000000000000000000000004254430000000000E4FE687C90257D3D2D694C8531CDEECBE84F3367310A20B3C85F482532A9578DBB3950B85CA06594D100000000000000000000000042544300000000000A20B3C85F482532A9578DBB3950B85CA06594D13000000000000000000000000055534400000000000A20B3C85F482532A9578DBB3950B85CA06594D1FF31585E1F3BD02A15D6185F8BB9B57CC60DEDDB37C10000000000000000000000004254430000000000585E1F3BD02A15D6185F8BB9B57CC60DEDDB37C131E4FE687C90257D3D2D694C8531CDEECBE84F33670000000000000000000000004254430000000000E4FE687C90257D3D2D694C8531CDEECBE84F33673115036E2D3F5437A83E5AC3CAEE34FF2C21DEB618000000000000000000000000425443000000000015036E2D3F5437A83E5AC3CAEE34FF2C21DEB6183000000000000000000000000055534400000000000A20B3C85F482532A9578DBB3950B85CA06594D1FF31585E1F3BD02A15D6185F8BB9B57CC60DEDDB37C10000000000000000000000004254430000000000585E1F3BD02A15D6185F8BB9B57CC60DEDDB37C13157180C769B66D942EE69E6DCC940CA48D82337AD000000000000000000000000425443000000000057180C769B66D942EE69E6DCC940CA48D82337AD1000000000000000000000000058525000000000003000000000000000000000000055534400000000000A20B3C85F482532A9578DBB3950B85CA06594D100";
        assertTrue(rippleDHex.contains(hex));

        PathSet parsed = translator.fromHex(hex);
        assertEquals(hex, parsed.toHex());
    }
}
