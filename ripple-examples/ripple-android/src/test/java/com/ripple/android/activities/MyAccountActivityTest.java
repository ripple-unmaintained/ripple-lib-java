
package com.ripple.android.activities;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.util.ActivityController;

import android.content.Intent;
import android.os.Bundle;

@RunWith(RobolectricTestRunner.class)
public class MyAccountActivityTest {
    private ActivityController<MyAccountActivity> controller;
    private MyAccountActivity activity;

    @Before
    public void setUp() {
        controller = Robolectric.buildActivity(MyAccountActivity.class);
    }

    @After
    public void tearDown() {
        controller.destroy();
    }

    private void createWithIntent(String myExtra) {
        Intent intent = new Intent(Robolectric.application,
                MyAccountActivity.class);
        Bundle extras = new Bundle();
        extras.putString("myExtra", myExtra);
        intent.putExtras(extras);
        activity = (MyAccountActivity) controller
                .withIntent(intent)
                .create()
                .start()
                .visible()
                .get();
    }

    @Test
    public void createsAndDestroysActivity() {
        createWithIntent("foo");
        // Assertions go here
    }

    @Test
    public void pausesAndResumesActivity() {
        createWithIntent("foo");
        controller.pause().resume();
        // Assertions go here
    }

    @Test
    public void recreatesActivity() {
        createWithIntent("foo");
        // activity.recreate();
        // Assertions go here
    }
}
