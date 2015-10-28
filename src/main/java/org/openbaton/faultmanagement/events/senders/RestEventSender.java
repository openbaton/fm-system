/*
 * Copyright (c) 2015 Fraunhofer FOKUS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openbaton.faultmanagement.events.senders;
import org.openbaton.faultmanagement.events.AbstractVNFAlarm;
import org.openbaton.faultmanagement.events.senders.interfaces.EventSender;
import org.openbaton.faultmanagement.model.AlarmEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.Future;

/**
 * Created by mob on 28/10/15.
 */
@Service
@Scope
public class RestEventSender implements EventSender {


    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    @Async
    public Future<Void> send(AlarmEndpoint endpoint, AbstractVNFAlarm event) throws IOException {
        try {
            /*CloseableHttpClient httpClient = HttpClientBuilder.create().build();
            Gson mapper = new GsonBuilder().create();
            String json = "{\"action:\"" + event.getAction() + "\", \"payload\":" + mapper.toJson(event.getPayload()) + "}";

            log.trace("body is: " + json);

            log.trace("Invoking POST on URL: " + endpoint.getEndpoint());
            HttpPost request = new HttpPost(endpoint.getEndpoint());
            request.addHeader("content-type", "application/json");
            request.addHeader("accept", "application/json");
            StringEntity params = new StringEntity(json);
            request.setEntity(params);
            HttpResponse response = httpClient.execute(request);
            if (response.getEntity().getContentLength() != 0) {
            } else {

            }*/
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
