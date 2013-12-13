
package com.ripple.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;

public class RippleServiceTest {
    private static final String EXPECTED_MASTER_SEED = "spkzBtpghrz6i8M2wSHafvxXAJbto";

    public static final String TEST_ACCOUNT_CONFIG = "/myAccount.json";

    RippleService rippleService;

    private String userName = "userName";

    private String password = "password";

    private static final String EXPECTED_ADDRESS = "rHnS75sy8Dp6qb5twi1rHHTZwujst6hn0";

    @Before
    public void setUp() {
        rippleService = mock(RippleService.class);
        when(rippleService.login(userName, password)).thenReturn(
                RippleService.convertToUser(this.getClass()
                        .getResourceAsStream(TEST_ACCOUNT_CONFIG)));
        when(rippleService.login("user", "password")).thenReturn(null);
    }

    @Test
    public void should_return_json_object_not_null_when_login_success() {
        assertNotNull(rippleService.login(userName, password));
    }

    @Test
    public void should_return_json_object_null_when_login_fail() {
        assertNull(rippleService.login("user", "password"));
    }

    @Test
    public void should_return_expected_address_when_get_wallet_address() throws JSONException {
        assertEquals(EXPECTED_ADDRESS, rippleService.login(userName, password).getWalletAddress());
    }

    @Test
    public void should_return_expected_master_seed_when_get_master_seed() throws JSONException {
        assertEquals(EXPECTED_MASTER_SEED, rippleService.login(userName, password).getMasterSeed());
    }

    @Test
    public void should_return_5_when_get_contracts_size() throws JSONException {
        assertEquals(5, rippleService.login(userName, password).getContacts().size());
    }
}
