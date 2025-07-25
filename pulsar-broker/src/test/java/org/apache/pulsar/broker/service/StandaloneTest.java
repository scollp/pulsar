/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.pulsar.broker.service;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import org.apache.pulsar.PulsarStandaloneStarter;
import org.testng.annotations.Test;

@Test(groups = "broker")
public class StandaloneTest {

    static class TestPulsarStandaloneStarter extends PulsarStandaloneStarter {
        public TestPulsarStandaloneStarter(String[] args) throws Exception {
            super(args);
        }

        @Override
        protected void registerShutdownHook() {
            // ignore to prevent memory leaks
        }

        @Override
        protected void exit(int status) {
            // don't ever call System.exit in tests
            throw new RuntimeException("Exited with status " + status);
        }
    }

    @Test
    public void testWithoutMetadataStoreUrlInConfFile() throws Exception {
        String[] args = new String[]{"--config",
                "../conf/standalone.conf"};
        PulsarStandaloneStarter standalone = new TestPulsarStandaloneStarter(args);
        assertNotNull(standalone.getConfig().getProperties().getProperty("metadataStoreUrl"));
        assertNotNull(standalone.getConfig().getProperties().getProperty("configurationMetadataStoreUrl"));
    }

    @Test
    public void testInitialize() throws Exception {
        String[] args = new String[]{"--config",
                "./src/test/resources/configurations/pulsar_broker_test_standalone.conf"};
        PulsarStandaloneStarter standalone = new TestPulsarStandaloneStarter(args);
        assertNull(standalone.getConfig().getAdvertisedAddress());
        assertEquals(standalone.getConfig().getAdvertisedListeners(),
                "internal:pulsar://192.168.1.11:6660,internal:pulsar+ssl://192.168.1.11:6651");
        assertEquals(standalone.getConfig().isDispatcherPauseOnAckStatePersistentEnabled(), true);
        assertEquals(standalone.getConfig().getMaxSecondsToClearTopicNameCache(), 1);
        assertEquals(standalone.getConfig().getTopicNameCacheMaxCapacity(), 200);
        assertEquals(standalone.getConfig().isCreateTopicToRemoteClusterForReplication(), true);
    }
}
