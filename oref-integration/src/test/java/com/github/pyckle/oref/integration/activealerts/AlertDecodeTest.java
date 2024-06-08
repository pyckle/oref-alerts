package com.github.pyckle.oref.integration.activealerts;

import com.github.pyckle.oref.integration.dto.Alert;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AlertDecodeTest {
    @Test
    void testDecodeAlert() throws Exception{
        Alert alert;
        try (InputStream is = AlertDecodeTest.class.getResourceAsStream("exampleAlert.json")) {
            alert = new Gson().fromJson(new InputStreamReader(is, StandardCharsets.UTF_8), new TypeToken<>() {
            });
        }
        assertEquals("133601362940000000", alert.id());
    }
}
