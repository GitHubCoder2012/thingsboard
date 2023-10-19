/**
 * Copyright © 2016-2023 The Thingsboard Authors
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
package org.thingsboard.server.service.queue.ruleengine;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.thingsboard.server.common.msg.queue.TopicPartitionInfo;
import org.thingsboard.server.gen.transport.TransportProtos;
import org.thingsboard.server.queue.TbQueueConsumer;
import org.thingsboard.server.queue.common.TbProtoQueueMsg;

import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Slf4j
public class TbQueueConsumerTask {

    @Getter
    private final Object key;
    @Getter
    private final TbQueueConsumer<TbProtoQueueMsg<TransportProtos.ToRuleEngineMsg>> consumer;

    private Future<?> task;
    private CountDownLatch completionLatch;

    public void setTask(Future<?> task) {
        this.completionLatch = new CountDownLatch(1);
        this.task = task;
    }

    public void subscribe(Set<TopicPartitionInfo> partitions) {
        log.trace("[{}] Subscribing to partitions: {}", key, partitions);
        consumer.subscribe(partitions);
    }

    public void initiateStop() {
        log.debug("[{}] Initiating stop", key);
        consumer.stop();
        if (isRunning()) {
            task.cancel(true);
        }
    }

    public void awaitCompletion() {
        log.trace("[{}] Awaiting finish", key);
        if (isRunning()) {
            try {
                if (!completionLatch.await(30, TimeUnit.SECONDS)) {
                    task = null;
                    throw new IllegalStateException("timeout of 30 seconds expired");
                }
                log.trace("[{}] Awaited finish", key);
            } catch (Exception e) {
                log.warn("[{}] Failed to await for consumer to stop", key, e);
            }
        }
    }

    public boolean isRunning() {
        return task != null;
    }

    public void finished() {
        completionLatch.countDown();
        task = null;
    }

}
