/*
 * Copyright 2020 The Terasology Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.destinationsol.systems.DestructionSystemTests;

import org.destinationsol.entitysystem.EntitySystemManager;
import org.destinationsol.game.context.internal.ContextImpl;
import org.destinationsol.health.components.Health;
import org.destinationsol.modules.ModuleManager;
import org.destinationsol.removal.events.RemovalForOptimizationEvent;
import org.junit.Before;
import org.junit.Test;
import org.terasology.gestalt.entitysystem.component.management.ComponentManager;
import org.terasology.gestalt.entitysystem.entity.EntityRef;

import static org.junit.Assert.assertFalse;

/**
 * Test to ensure that a {@link RemovalForOptimizationEvent} on an entity will remove that entity.
 */
public class RemovalForOptimizationTest {

    private ModuleManager moduleManager;
    private EntitySystemManager entitySystemManager;

    @Before
    public void setUp() throws Exception {
        moduleManager = new ModuleManager();
        moduleManager.init();
        entitySystemManager = new EntitySystemManager(moduleManager.getEnvironment(), new ComponentManager(), new ContextImpl());
    }

    @Test
    public void testOnRemovalForOptimization(){
        EntityRef entity = entitySystemManager.getEntityManager().createEntity(new Health());

        entitySystemManager.sendEvent(new RemovalForOptimizationEvent(), entity);

        assertFalse(entity.exists());
    }
}
