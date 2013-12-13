
package com.ripple.android.activities;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.util.ActivityController;

import com.ripple.android.profile.User;
import com.ripple.service.RippleService;
import com.ripple.service.RippleServiceTest;

import android.content.Intent;

@RunWith(RobolectricTestRunner.class)
public class AccountActivityTest {
    private ActivityController<AccountActivity> controller;

    private AccountActivity accountActivity;

    @Before
    public void setUp() {
        controller = Robolectric.buildActivity(AccountActivity.class);
    }

    @After
    public void tearDown() {
        controller.destroy();
    }

    @Test
    public void should_return_current_user_not_null_when_on_create() {
        Intent intent = new Intent(Robolectric.getShadowApplication().getApplicationContext(),
                AccountActivity.class);
        intent.putExtra(
                AccountActivity.CURRENT_USER,
                RippleService.convertToUser(this.getClass().getResourceAsStream(
                        RippleServiceTest.TEST_ACCOUNT_CONFIG)));
        accountActivity = (AccountActivity) controller.withIntent(intent).create().visible().get();
        assertNotNull(accountActivity.getCurrentUser());
        assertEquals("rHnS75sy8Dp6qb5twi1rHHTZwujst6hn0", accountActivity.getCurrentUser()
                .getWalletAddress());
    }

}
