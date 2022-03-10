/*
 * MIT License
 *
 * Copyright (c) 2021 pumbas600
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.pumbas.halpbot;

import org.dockbox.hartshorn.core.annotations.stereotype.Service;
import org.dockbox.hartshorn.core.boot.ExceptionHandler;
import org.dockbox.hartshorn.core.domain.Exceptional;
import org.dockbox.hartshorn.data.mapping.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

@Service
public class RequestUtil
{
    @Inject
    private ObjectMapper mapper;
    private final HttpClient client;


    public RequestUtil() {
        this.client = HttpClient.newHttpClient();
    }

    public HttpRequest getRequest(String url) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
    }

    public HttpRequest postRequest(String url, String body) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(BodyPublishers.ofString(body))
                .build();
    }

    public Exceptional<String> get(String url) {

        try {
            HttpResponse<String> response = this.client.send(
                    this.getRequest(url), HttpResponse.BodyHandlers.ofString());

            if (isOk(response)) {
                return Exceptional.of(response.body());
            }
            return Exceptional.of(
                    new IOException("There was an error sending the GET request to %s. Got the response: %s"
                            .formatted(url, response)));
        } catch (IOException | InterruptedException e) {
            return Exceptional.of(e);
        }
    }

    public <T> Exceptional<T> get(String url, Class<T> type) {
        return this.get(url).flatMap(json -> this.mapper.read(json, type));
    }

    public CompletableFuture<String> getAsync(String url) {
        return this.client.sendAsync(this.getRequest(url), HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (!isOk(response)) {
                        ExceptionHandler.unchecked(
                                new IOException("There was an error sending the GET request to %s. Got the response: %s"
                                        .formatted(url, response)));
                    }
                    return response.body();
                });
    }

    public <T> CompletableFuture<T> getAsync(String url, Class<T> type) {
        return this.getAsync(url)
                .thenApply(json -> this.mapper.read(json, type).rethrowUnchecked().get());
    }

    public CompletableFuture<String> postAsync(String url, String body) {
        return this.client.sendAsync(this.postRequest(url, body), HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (!isOk(response)) {
                        ExceptionHandler.unchecked(
                                new IOException("There was an error sending the POST request to %s. Got the response: %s"
                                        .formatted(url, response)));
                    }
                    return response.body();
                });
    }

    public <T> CompletableFuture<T> postAsync(String url, String body, Class<T> type) {
        return this.postAsync(url, body)
                .thenApply(json -> this.mapper.read(json, type).rethrowUnchecked().get());
    }

    public static boolean isOk(HttpResponse<?> response) {
        int statusCode = response.statusCode();
        return statusCode >= 200 && statusCode < 300;
    }
}
