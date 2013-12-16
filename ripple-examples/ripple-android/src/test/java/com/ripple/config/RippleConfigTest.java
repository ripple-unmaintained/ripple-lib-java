
package com.ripple.config;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import android.app.Activity;
import android.content.res.AssetManager;

@RunWith(RobolectricTestRunner.class)
public class RippleConfigTest {
    AssetManager assetManager;
    ObjectMapper mapper;
    InputStream inputStream;

    @Before
    public void setUp() throws Exception {
        mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);

        assetManager = new Activity().getAssets();

        String configPath = "ripple-config.json";
        inputStream = assetManager.open(configPath);
    }

    @Test
    public void should_return_not_null_when_get_asset_manager() {
        assertNotNull(assetManager);

        assetManager = Robolectric.application.getAssets();
        assertNotNull(assetManager);

        assetManager = Robolectric.application.getResources().getAssets();
        assertNotNull(assetManager);
    }

    @Test
    public void should_return_not_null_when_get_ripple_config_file() throws IOException {
        assertNotNull(inputStream);
    }

    @Test
    public void should_return_right_value_when_convert_to_ripple_config() throws IOException {
        RippleConfig rippleConfig = mapper.readValue(inputStream, RippleConfig.class);
        assertNotNull(rippleConfig);
        assertEquals(true, rippleConfig.getServerConfig().isTrace());
        assertEquals(2, rippleConfig.getMarkets().size());
    }

}
