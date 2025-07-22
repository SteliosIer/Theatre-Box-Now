package com.example.myapplication4;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.example.myapplication4.data.NLPClient;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class ChatbotFragmentTest {
    private NLPClient nlpClient;
    private final String TEST_RESPONSE = "{\"intent\":\"%s\",\"entities\":%s}";

    @Before
    public void setUp() {
        nlpClient = new NLPClient();
        nlpClient.setTestingMode(true); // Enable test mode
    }

    // Test 1: Successful Intent Detection
    @Test
    public void testBookIntentDetection() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        nlpClient.setMockResponse(String.format(TEST_RESPONSE,
                "BOOK_TICKET",
                "{\"performance\":\"hamlet\"}"
        ));

        nlpClient.processText("book hamlet tickets", new NLPClient.NLPCallback() {
            @Override
            public void onResult(String intent, JSONObject entities) {
                assertEquals("BOOK_TICKET", intent);
                latch.countDown();
            }

            @Override
            public void onError(String error) {
                fail("Should not return error");
                latch.countDown();
            }
        });

        assertTrue(latch.await(1, TimeUnit.SECONDS));
    }

    // Test 2: Entity Extraction
    @Test
    public void testPerformanceAndTimeExtraction() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        nlpClient.setMockResponse(String.format(TEST_RESPONSE,
                "BOOK_TICKET",
                "{\"performance\":\"romeo and juliet\",\"time\":\"evening\"}"
        ));

        nlpClient.processText("book romeo for evening", new NLPClient.NLPCallback() {
            @Override
            public void onResult(String intent, JSONObject entities) {
                try {
                    assertEquals("romeo and juliet", entities.getString("performance"));
                    assertEquals("evening", entities.getString("time"));
                } catch (Exception e) {
                    fail(e.getMessage());
                }
                latch.countDown();
            }

            @Override
            public void onError(String error) {
                fail(error);
                latch.countDown();
            }
        });

        assertTrue(latch.await(1, TimeUnit.SECONDS));
    }

    // Test 3: Error Handling
    @Test
    public void testErrorResponse() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        nlpClient.setMockResponse("invalid json");

        nlpClient.processText("test input", new NLPClient.NLPCallback() {
            @Override
            public void onResult(String intent, JSONObject entities) {
                fail("Should not succeed");
                latch.countDown();
            }

            @Override
            public void onError(String error) {
                assertTrue(error.contains("JSON"));
                latch.countDown();
            }
        });

        assertTrue(latch.await(1, TimeUnit.SECONDS));
    }

    // Test 4: Unknown Intent
    @Test
    public void testUnknownIntent() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        nlpClient.setMockResponse(String.format(TEST_RESPONSE,
                "UNKNOWN",
                "{}"
        ));

        nlpClient.processText("random text", new NLPClient.NLPCallback() {
            @Override
            public void onResult(String intent, JSONObject entities) {
                assertEquals("UNKNOWN", intent);
                latch.countDown();
            }

            @Override
            public void onError(String error) {
                fail("Should not return error");
                latch.countDown();
            }
        });

        assertTrue(latch.await(1, TimeUnit.SECONDS));
    }
}