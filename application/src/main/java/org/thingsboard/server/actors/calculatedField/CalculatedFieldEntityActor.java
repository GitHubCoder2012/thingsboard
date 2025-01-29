/**
 * Copyright © 2016-2024 The Thingsboard Authors
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
package org.thingsboard.server.actors.calculatedField;

import lombok.extern.slf4j.Slf4j;
import org.thingsboard.server.actors.ActorSystemContext;
import org.thingsboard.server.actors.TbActorCtx;
import org.thingsboard.server.actors.TbActorException;
import org.thingsboard.server.actors.service.ContextAwareActor;
import org.thingsboard.server.common.data.id.EntityId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.msg.TbActorMsg;

@Slf4j
public class CalculatedFieldEntityActor extends ContextAwareActor {

    private final CalculatedFieldEntityMessageProcessor processor;

    CalculatedFieldEntityActor(ActorSystemContext systemContext, TenantId tenantId, EntityId entityId) {
        super(systemContext);
        this.processor = new CalculatedFieldEntityMessageProcessor(systemContext, tenantId, entityId);
    }

    @Override
    public void init(TbActorCtx ctx) throws TbActorException {
        super.init(ctx);
        log.debug("[{}][{}] Starting CF entity actor.", processor.tenantId, processor.entityId);
        try {
            processor.init(ctx);
            log.debug("[{}][{}] CF entity actor started.", processor.tenantId, processor.entityId);
        } catch (Exception e) {
            log.warn("[{}][{}] Unknown failure", processor.tenantId, processor.entityId, e);
            throw new TbActorException("Failed to initialize CF entity actor", e);
        }
    }

    @Override
    protected boolean doProcess(TbActorMsg msg) {
        switch (msg.getMsgType()) {
            case CF_STATE_RESTORE_MSG:
                processor.process((CalculatedFieldStateRestoreMsg) msg);
                break;
            case CF_ENTITY_TELEMETRY_MSG:
                processor.process((EntityCalculatedFieldTelemetryMsg) msg);
                break;
            case CF_LINKED_TELEMETRY_MSG:
                processor.process((EntityCalculatedFieldLinkedTelemetryMsg) msg);
                break;
            default:
                return false;
        }
        return true;
    }

}
