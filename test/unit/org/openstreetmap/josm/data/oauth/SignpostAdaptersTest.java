// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.data.oauth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openstreetmap.josm.JOSMFixture;
import org.openstreetmap.josm.data.oauth.SignpostAdapters.HttpRequest;
import org.openstreetmap.josm.data.oauth.SignpostAdapters.HttpResponse;
import org.openstreetmap.josm.data.oauth.SignpostAdapters.OAuthConsumer;
import org.openstreetmap.josm.tools.HttpClient;

/**
 * Unit tests for class {@link SignpostAdapters}.
 */
public class SignpostAdaptersTest {

    /**
     * Setup test.
     */
    @BeforeClass
    public static void setUpBeforeClass() {
        JOSMFixture.createUnitTestFixture().init();
    }

    private static HttpClient newClient() throws MalformedURLException {
        return HttpClient.create(new URL("https://www.openstreetmap.org"));
    }

    /**
     * Unit test of method {@link SignpostAdapters.OAuthConsumer#wrap}.
     * @throws MalformedURLException never
     */
    @Test
    public void testOAuthConsumerWrap() throws MalformedURLException {
        assertNotNull(new OAuthConsumer("", "").wrap(newClient()));
    }

    /**
     * Unit test of method {@link SignpostAdapters.HttpRequest#getMessagePayload}.
     * @throws IOException never
     */
    @Test
    public void testHttpRequestGetMessagePayload() throws IOException {
        assertNull(new HttpRequest(newClient()).getMessagePayload());
    }

    /**
     * Unit test of method {@link SignpostAdapters.HttpRequest#setRequestUrl}.
     * @throws IOException never
     */
    @Test(expected = IllegalStateException.class)
    public void testHttpRequestSetRequestUrl() throws IOException {
        new HttpRequest(newClient()).setRequestUrl(null);
    }

    /**
     * Unit test of method {@link SignpostAdapters.HttpRequest#getAllHeaders}.
     * @throws IOException never
     */
    @Test(expected = IllegalStateException.class)
    public void testHttpRequestGetAllHeaders() throws IOException {
        new HttpRequest(newClient()).getAllHeaders();
    }

    /**
     * Unit test of method {@link SignpostAdapters.HttpRequest#unwrap}.
     * @throws IOException never
     */
    @Test(expected = IllegalStateException.class)
    public void testHttpRequestUnwrap() throws IOException {
        new HttpRequest(newClient()).unwrap();
    }

    /**
     * Unit test of method {@link SignpostAdapters.HttpResponse#getReasonPhrase()}.
     * @throws Exception never
     */
    @Test
    public void testHttpResponseGetReasonPhrase() throws Exception {
        assertEquals("OK", new HttpResponse(new HttpRequest(newClient()).request.connect()).getReasonPhrase());
    }

    /**
     * Unit test of method {@link SignpostAdapters.HttpResponse#unwrap}.
     * @throws IOException never
     */
    @Test(expected = IllegalStateException.class)
    public void testHttpResponseUnwrap() throws IOException {
        new HttpResponse(new HttpRequest(newClient()).request.connect()).unwrap();
    }
}
