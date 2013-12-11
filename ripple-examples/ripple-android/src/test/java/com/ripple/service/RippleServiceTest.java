
package com.ripple.service;

import static org.junit.Assert.*;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

public class RippleServiceTest {
    RippleService rippleService;

    private JSONObject jsonObject;

    private String userName;

    private String password;

    private String address;

    @Before
    public void setUp() {
        rippleService = new RippleService();
        // need set your account. then you can comment out should_return_json_object_not_null_when_login_success
        //and should_return_json_object_when_to_string
        jsonObject = rippleService.login(userName, password);
    }

    // @Test
    public void should_return_json_object_not_null_when_login_success() {
        assertNotNull(jsonObject);
    }

    @Test
    public void should_return_json_object_null_when_login_fail() {
        assertNull(rippleService.login("user", "password"));
    }

//    @Test
    public void should_return_json_object_when_to_string() throws JSONException {
        assertEquals(address, jsonObject.getString("account_id"));
    }

}
