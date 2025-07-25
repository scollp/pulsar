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
package org.apache.pulsar.common.policies.data;

import static org.apache.pulsar.common.policies.data.OffloadPoliciesImpl.EXTRA_CONFIG_PREFIX;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Offload policies configuration test.
 */
public class OffloadPoliciesTest {

    private static final int M = 1024 * 1024;
    private static final long MIN = 1000 * 60;

    private final String offloadersDirectory = "./test-offloader-directory";
    private final Integer managedLedgerOffloadMaxThreads = 10;
    private final Integer managedLedgerOffloadPrefetchRounds = 5;
    private final Long offloadThresholdInBytes = 0L;
    private final Long offloadThresholdInSeconds = 100L;
    private final Long offloadDeletionLagInMillis = 5 * MIN;

    @Test
    public void testS3Configuration() {
        final String driver = "aws-s3";
        final String region = "test-region";
        final String bucket = "test-bucket";
        final String role = "test-role";
        final String roleSessionName = "test-role-session-name";
        final String credentialId = "test-credential-id";
        final String credentialSecret = "test-credential-secret";
        final String endPoint = "test-endpoint";
        final Integer maxBlockSizeInBytes = 5 * M;
        final Integer readBufferSizeInBytes = 2 * M;
        final Long offloadThresholdInBytes = 10L * M;
        final Long offloadThresholdInSeconds = 1000L;
        final Long offloadDeletionLagInMillis = 5L * MIN;

        OffloadPoliciesImpl offloadPolicies = OffloadPoliciesImpl.create(
                driver,
                region,
                bucket,
                endPoint,
                role,
                roleSessionName,
                credentialId,
                credentialSecret,
                maxBlockSizeInBytes,
                readBufferSizeInBytes,
                offloadThresholdInBytes,
                offloadThresholdInSeconds,
                offloadDeletionLagInMillis,
                OffloadedReadPriority.TIERED_STORAGE_FIRST
        );

        Assert.assertEquals(offloadPolicies.getManagedLedgerOffloadDriver(), driver);
        Assert.assertEquals(offloadPolicies.getS3ManagedLedgerOffloadRegion(), region);
        Assert.assertEquals(offloadPolicies.getS3ManagedLedgerOffloadBucket(), bucket);
        Assert.assertEquals(offloadPolicies.getS3ManagedLedgerOffloadServiceEndpoint(), endPoint);
        Assert.assertEquals(offloadPolicies.getS3ManagedLedgerOffloadMaxBlockSizeInBytes(), maxBlockSizeInBytes);
        Assert.assertEquals(offloadPolicies.getS3ManagedLedgerOffloadReadBufferSizeInBytes(), readBufferSizeInBytes);
        Assert.assertEquals(offloadPolicies.getManagedLedgerOffloadThresholdInBytes(),
                offloadThresholdInBytes);
        Assert.assertEquals(offloadPolicies.getManagedLedgerOffloadDeletionLagInMillis(),
                Long.valueOf(offloadDeletionLagInMillis));
        Assert.assertEquals(offloadPolicies.getManagedLedgerOffloadThresholdInSeconds(), offloadThresholdInSeconds);
    }

    @Test
    public void testGcsConfiguration() {
        final String driver = "google-cloud-storage";
        final String region = "test-region";
        final String bucket = "test-bucket";
        final String endPoint = "test-endpoint";
        final String role = "test-role";
        final String roleSessionName = "test-role-session-name";
        final String credentialId = "test-credential-id";
        final String credentialSecret = "test-credential-secret";
        final Integer maxBlockSizeInBytes = 5 * M;
        final Integer readBufferSizeInBytes = 2 * M;
        final Long offloadThresholdInBytes = 0L;
        final Long offloadThresholdInSeconds = 1000L;
        final Long offloadDeletionLagInMillis = 5 * MIN;
        final OffloadedReadPriority readPriority = OffloadedReadPriority.TIERED_STORAGE_FIRST;

        OffloadPoliciesImpl offloadPolicies = OffloadPoliciesImpl.create(
                driver,
                region,
                bucket,
                endPoint,
                role,
                roleSessionName,
                credentialId,
                credentialSecret,
                maxBlockSizeInBytes,
                readBufferSizeInBytes,
                offloadThresholdInBytes,
                offloadThresholdInSeconds,
                offloadDeletionLagInMillis,
                readPriority
        );

        Assert.assertEquals(offloadPolicies.getManagedLedgerOffloadDriver(), driver);
        Assert.assertEquals(offloadPolicies.getGcsManagedLedgerOffloadRegion(), region);
        Assert.assertEquals(offloadPolicies.getGcsManagedLedgerOffloadBucket(), bucket);
        Assert.assertEquals(offloadPolicies.getGcsManagedLedgerOffloadMaxBlockSizeInBytes(), maxBlockSizeInBytes);
        Assert.assertEquals(offloadPolicies.getGcsManagedLedgerOffloadReadBufferSizeInBytes(), readBufferSizeInBytes);
        Assert.assertEquals(offloadPolicies.getManagedLedgerOffloadThresholdInBytes(), offloadThresholdInBytes);
        Assert.assertEquals(offloadPolicies.getManagedLedgerOffloadDeletionLagInMillis(), offloadDeletionLagInMillis);
        Assert.assertEquals(offloadPolicies.getManagedLedgerOffloadedReadPriority(), readPriority);
        Assert.assertEquals(offloadPolicies.getManagedLedgerOffloadThresholdInSeconds(), offloadThresholdInSeconds);
    }

    @Test
    public void testCreateByProperties() {
        final String s3ManagedLedgerOffloadRegion = "test-s3-region";
        final String s3ManagedLedgerOffloadBucket = "test-s3-bucket";
        final String s3ManagedLedgerOffloadServiceEndpoint = "test-s3-endpoint";
        final Integer s3ManagedLedgerOffloadMaxBlockSizeInBytes = 5 * M;
        final Integer s3ManagedLedgerOffloadReadBufferSizeInBytes = 2 * M;
        final String s3ManagedLedgerOffloadRole = "test-s3-role";
        final String s3ManagedLedgerOffloadRoleSessionName = "test-s3-role-session-name";
        final String s3ManagedLedgerOffloadCredentialId = "test-s3-credential-id";
        final String s3ManagedLedgerOffloadCredentialSecret = "test-s3-credential-secret";

        final String gcsManagedLedgerOffloadRegion = "test-gcs-region";
        final String gcsManagedLedgerOffloadBucket = "test-s3-bucket";
        final Integer gcsManagedLedgerOffloadMaxBlockSizeInBytes = 10 * M;
        final Integer gcsManagedLedgerOffloadReadBufferSizeInBytes = 4 * M;
        final String gcsManagedLedgerOffloadServiceAccountKeyFile = "./gcs_key.json";

        final String fileSystemProfilePath = "test-file-system-path";
        final String fileSystemURI = "tset-system-uri";

        final String driver = "test-driver";
        Properties properties = new Properties();

        properties.setProperty("offloadersDirectory", offloadersDirectory);
        properties.setProperty("managedLedgerOffloadDriver", driver);
        properties.setProperty("managedLedgerOffloadMaxThreads", "" + managedLedgerOffloadMaxThreads);
        properties.setProperty("managedLedgerOffloadPrefetchRounds", "" + managedLedgerOffloadPrefetchRounds);
        properties.setProperty("managedLedgerOffloadAutoTriggerSizeThresholdBytes", "" + offloadThresholdInBytes);
        properties.setProperty("managedLedgerOffloadThresholdInSeconds", "" + offloadThresholdInSeconds);
        properties.setProperty("managedLedgerOffloadDeletionLagMs", "" + offloadDeletionLagInMillis);

        properties.setProperty("s3ManagedLedgerOffloadRegion", s3ManagedLedgerOffloadRegion);
        properties.setProperty("s3ManagedLedgerOffloadBucket", s3ManagedLedgerOffloadBucket);
        properties.setProperty("s3ManagedLedgerOffloadServiceEndpoint", s3ManagedLedgerOffloadServiceEndpoint);
        properties.setProperty("s3ManagedLedgerOffloadMaxBlockSizeInBytes",
                "" + s3ManagedLedgerOffloadMaxBlockSizeInBytes);
        properties.setProperty("s3ManagedLedgerOffloadReadBufferSizeInBytes",
                "" + s3ManagedLedgerOffloadReadBufferSizeInBytes);
        properties.setProperty("s3ManagedLedgerOffloadRole", s3ManagedLedgerOffloadRole);
        properties.setProperty("s3ManagedLedgerOffloadRoleSessionName", s3ManagedLedgerOffloadRoleSessionName);

        properties.setProperty("s3ManagedLedgerOffloadCredentialId", s3ManagedLedgerOffloadCredentialId);
        properties.setProperty("s3ManagedLedgerOffloadCredentialSecret", s3ManagedLedgerOffloadCredentialSecret);

        properties.setProperty("gcsManagedLedgerOffloadRegion", gcsManagedLedgerOffloadRegion);
        properties.setProperty("gcsManagedLedgerOffloadBucket", gcsManagedLedgerOffloadBucket);
        properties.setProperty("gcsManagedLedgerOffloadMaxBlockSizeInBytes",
                "" + gcsManagedLedgerOffloadMaxBlockSizeInBytes);
        properties.setProperty("gcsManagedLedgerOffloadReadBufferSizeInBytes",
                "" + gcsManagedLedgerOffloadReadBufferSizeInBytes);
        properties.setProperty("gcsManagedLedgerOffloadServiceAccountKeyFile",
                gcsManagedLedgerOffloadServiceAccountKeyFile);

        properties.setProperty("fileSystemProfilePath", fileSystemProfilePath);
        properties.setProperty("fileSystemURI", fileSystemURI);

        OffloadPoliciesImpl offloadPolicies = OffloadPoliciesImpl.create(properties);

        Assert.assertEquals(offloadPolicies.getOffloadersDirectory(), offloadersDirectory);
        Assert.assertEquals(offloadPolicies.getManagedLedgerOffloadDriver(), driver);
        Assert.assertEquals(offloadPolicies.getManagedLedgerOffloadMaxThreads(), managedLedgerOffloadMaxThreads);
        Assert.assertEquals(offloadPolicies.getManagedLedgerOffloadPrefetchRounds(),
                managedLedgerOffloadPrefetchRounds);
        Assert.assertEquals(offloadPolicies.getManagedLedgerOffloadThresholdInBytes(), offloadThresholdInBytes);
        Assert.assertEquals(offloadPolicies.getManagedLedgerOffloadThresholdInSeconds(), offloadThresholdInSeconds);
        Assert.assertEquals(offloadPolicies.getManagedLedgerOffloadDeletionLagInMillis(), offloadDeletionLagInMillis);

        Assert.assertEquals(offloadPolicies.getS3ManagedLedgerOffloadRegion(), s3ManagedLedgerOffloadRegion);
        Assert.assertEquals(offloadPolicies.getS3ManagedLedgerOffloadBucket(), s3ManagedLedgerOffloadBucket);
        Assert.assertEquals(offloadPolicies.getS3ManagedLedgerOffloadServiceEndpoint(),
                s3ManagedLedgerOffloadServiceEndpoint);
        Assert.assertEquals(offloadPolicies.getS3ManagedLedgerOffloadMaxBlockSizeInBytes(),
                s3ManagedLedgerOffloadMaxBlockSizeInBytes);
        Assert.assertEquals(offloadPolicies.getS3ManagedLedgerOffloadReadBufferSizeInBytes(),
                s3ManagedLedgerOffloadReadBufferSizeInBytes);
        Assert.assertEquals(offloadPolicies.getS3ManagedLedgerOffloadRole(), s3ManagedLedgerOffloadRole);
        Assert.assertEquals(offloadPolicies.getS3ManagedLedgerOffloadRoleSessionName(),
                s3ManagedLedgerOffloadRoleSessionName);
        Assert.assertEquals(offloadPolicies.getS3ManagedLedgerOffloadCredentialId(),
                s3ManagedLedgerOffloadCredentialId);
        Assert.assertEquals(offloadPolicies.getS3ManagedLedgerOffloadCredentialSecret(),
                s3ManagedLedgerOffloadCredentialSecret);

        Assert.assertEquals(offloadPolicies.getGcsManagedLedgerOffloadRegion(), gcsManagedLedgerOffloadRegion);
        Assert.assertEquals(offloadPolicies.getGcsManagedLedgerOffloadBucket(), gcsManagedLedgerOffloadBucket);
        Assert.assertEquals(offloadPolicies.getGcsManagedLedgerOffloadMaxBlockSizeInBytes(),
                gcsManagedLedgerOffloadMaxBlockSizeInBytes);
        Assert.assertEquals(offloadPolicies.getGcsManagedLedgerOffloadReadBufferSizeInBytes(),
                gcsManagedLedgerOffloadReadBufferSizeInBytes);
        Assert.assertEquals(offloadPolicies.getGcsManagedLedgerOffloadServiceAccountKeyFile(),
                gcsManagedLedgerOffloadServiceAccountKeyFile);

        Assert.assertEquals(offloadPolicies.getFileSystemProfilePath(), fileSystemProfilePath);
        Assert.assertEquals(offloadPolicies.getFileSystemURI(), fileSystemURI);
    }

    @Test
    public void compatibleWithConfigFileTest() {
        Properties properties = new Properties();
        properties.setProperty("managedLedgerOffloadAutoTriggerSizeThresholdBytes", "" + offloadThresholdInBytes);
        properties.setProperty("managedLedgerOffloadDeletionLagMs", "" + offloadDeletionLagInMillis);

        OffloadPoliciesImpl offloadPolicies = OffloadPoliciesImpl.create(properties);
        Assert.assertEquals(offloadThresholdInBytes, offloadPolicies.getManagedLedgerOffloadThresholdInBytes());
        Assert.assertEquals(offloadDeletionLagInMillis, offloadPolicies.getManagedLedgerOffloadDeletionLagInMillis());

        properties = new Properties();
        properties.setProperty("managedLedgerOffloadThresholdInBytes", "" + (offloadThresholdInBytes + 10));
        properties.setProperty("managedLedgerOffloadDeletionLagInMillis", "" + (offloadDeletionLagInMillis + 10));

        offloadPolicies = OffloadPoliciesImpl.create(properties);
        Assert.assertEquals(Long.valueOf(offloadThresholdInBytes + 10),
                offloadPolicies.getManagedLedgerOffloadThresholdInBytes());
        Assert.assertEquals(offloadDeletionLagInMillis + 10,
                offloadPolicies.getManagedLedgerOffloadDeletionLagInMillis().longValue());

        properties = new Properties();
        properties.setProperty("managedLedgerOffloadThresholdInBytes", "" + (offloadThresholdInBytes + 20));
        properties.setProperty("managedLedgerOffloadDeletionLagInMillis", "" + (offloadDeletionLagInMillis + 20));
        properties.setProperty("managedLedgerOffloadAutoTriggerSizeThresholdBytes", "" + offloadThresholdInBytes + 30);
        properties.setProperty("managedLedgerOffloadDeletionLagMs", "" + offloadDeletionLagInMillis + 30);

        offloadPolicies = OffloadPoliciesImpl.create(properties);
        Assert.assertEquals(Long.valueOf(offloadThresholdInBytes + 20),
                offloadPolicies.getManagedLedgerOffloadThresholdInBytes());
        Assert.assertEquals(offloadDeletionLagInMillis + 20,
                offloadPolicies.getManagedLedgerOffloadDeletionLagInMillis().longValue());
    }

    @Test
    public void oldPoliciesCompatibleTest() {
        Policies policies = new Policies();
        Assert.assertEquals(policies.offload_threshold, -1);
        Assert.assertEquals(policies.offload_threshold_in_seconds, -1);

        OffloadPoliciesImpl offloadPolicies = OffloadPoliciesImpl.oldPoliciesCompatible(null, policies);
        Assert.assertNull(offloadPolicies);

        policies.offload_deletion_lag_ms = 1000L;
        policies.offload_threshold = 0;
        policies.offload_threshold_in_seconds = 0;
        offloadPolicies = OffloadPoliciesImpl.oldPoliciesCompatible(offloadPolicies, policies);
        Assert.assertNotNull(offloadPolicies);
        Assert.assertEquals(offloadPolicies.getManagedLedgerOffloadDeletionLagInMillis(), Long.valueOf(1000));
        Assert.assertEquals(offloadPolicies.getManagedLedgerOffloadThresholdInBytes(), Long.valueOf(0));
        Assert.assertEquals(offloadPolicies.getManagedLedgerOffloadThresholdInSeconds(), Long.valueOf(0));

        policies.offload_deletion_lag_ms = 2000L;
        policies.offload_threshold = 100;
        policies.offload_threshold_in_seconds = 100;
        offloadPolicies = OffloadPoliciesImpl.oldPoliciesCompatible(offloadPolicies, policies);
        Assert.assertEquals(offloadPolicies.getManagedLedgerOffloadDeletionLagInMillis(), Long.valueOf(1000));
        Assert.assertEquals(offloadPolicies.getManagedLedgerOffloadThresholdInBytes(), Long.valueOf(0));
        Assert.assertEquals(offloadPolicies.getManagedLedgerOffloadThresholdInSeconds(), Long.valueOf(0));
    }

    @Test
    public void mergeTest() {
        final String topicBucket = "topic-bucket";
        OffloadPoliciesImpl topicLevelPolicies = new OffloadPoliciesImpl();
        topicLevelPolicies.setS3ManagedLedgerOffloadBucket(topicBucket);

        final String nsBucket = "ns-bucket";
        final Long nsDeletionLag = 2000L;
        OffloadPoliciesImpl nsLevelPolicies = new OffloadPoliciesImpl();
        nsLevelPolicies.setManagedLedgerOffloadDeletionLagInMillis(nsDeletionLag);
        nsLevelPolicies.setS3ManagedLedgerOffloadBucket(nsBucket);

        final String brokerDriver = "aws-s3";
        final Long brokerOffloadThreshold = 0L;
        final long brokerDeletionLag = 1000L;
        final Integer brokerOffloadMaxThreads = 2;
        Properties brokerProperties = new Properties();
        brokerProperties.setProperty("managedLedgerOffloadDriver", brokerDriver);
        brokerProperties.setProperty("managedLedgerOffloadAutoTriggerSizeThresholdBytes", "" + brokerOffloadThreshold);
        brokerProperties.setProperty("managedLedgerOffloadDeletionLagMs", "" + brokerDeletionLag);
        brokerProperties.setProperty("managedLedgerOffloadMaxThreads", "" + brokerOffloadMaxThreads);

        OffloadPoliciesImpl offloadPolicies =
                OffloadPoliciesImpl.mergeConfiguration(topicLevelPolicies, nsLevelPolicies, brokerProperties);
        Assert.assertNotNull(offloadPolicies);
        Assert.assertEquals(offloadPolicies.getManagedLedgerOffloadDriver(), brokerDriver);
        Assert.assertEquals(offloadPolicies.getS3ManagedLedgerOffloadBucket(), topicBucket);
        Assert.assertEquals(offloadPolicies.getManagedLedgerOffloadDeletionLagInMillis(), nsDeletionLag);
        Assert.assertEquals(offloadPolicies.getManagedLedgerOffloadThresholdInBytes(), brokerOffloadThreshold);
        Assert.assertEquals(offloadPolicies.getManagedLedgerOffloadMaxThreads(), brokerOffloadMaxThreads);
        Assert.assertEquals(offloadPolicies.getManagedLedgerOffloadPrefetchRounds(),
                Integer.valueOf(OffloadPoliciesImpl.DEFAULT_OFFLOAD_MAX_PREFETCH_ROUNDS));
        Assert.assertNull(offloadPolicies.getS3ManagedLedgerOffloadRegion());
    }


    @Test
    public void brokerPropertyCompatibleTest() {
        final Long brokerOffloadThreshold = 0L;
        final Long brokerDeletionLag = 2000L;
        final String brokerReadPriority = "bookkeeper-first";

        // 1. mergeConfiguration test
        Properties brokerProperties = new Properties();
        brokerProperties.setProperty("managedLedgerOffloadAutoTriggerSizeThresholdBytes", "" + brokerOffloadThreshold);
        brokerProperties.setProperty("managedLedgerOffloadDeletionLagMs", "" + brokerDeletionLag);
        brokerProperties.setProperty("managedLedgerDataReadPriority", "" + brokerReadPriority);
        OffloadPoliciesImpl offloadPolicies =
          OffloadPoliciesImpl.mergeConfiguration(null, null, brokerProperties);
        Assert.assertNotNull(offloadPolicies);
        Assert.assertEquals(offloadPolicies.getManagedLedgerOffloadThresholdInBytes(), brokerOffloadThreshold);
        Assert.assertEquals(offloadPolicies.getManagedLedgerOffloadDeletionLagInMillis(), brokerDeletionLag);
        Assert.assertEquals(offloadPolicies.getManagedLedgerOffloadedReadPriority().toString(), brokerReadPriority);

        // 2. compatibleWithBrokerConfigFile test
        brokerProperties = new Properties();
        brokerProperties.setProperty("managedLedgerOffloadAutoTriggerSizeThresholdBytes", "" + brokerOffloadThreshold);
        brokerProperties.setProperty("managedLedgerOffloadDeletionLagMs", "" + brokerDeletionLag);
        brokerProperties.setProperty("managedLedgerDataReadPriority", "" + brokerReadPriority);
        offloadPolicies = OffloadPoliciesImpl.create(brokerProperties);
        Assert.assertNotNull(offloadPolicies);
        Assert.assertEquals(offloadPolicies.getManagedLedgerOffloadThresholdInBytes(), brokerOffloadThreshold);
        Assert.assertEquals(offloadPolicies.getManagedLedgerOffloadDeletionLagInMillis(), brokerDeletionLag);
        Assert.assertEquals(offloadPolicies.getManagedLedgerOffloadedReadPriority().toString(), brokerReadPriority);


        brokerProperties = new Properties();
        // 2.1 offload properties name in OffloadPoliciesImpl
        brokerProperties.setProperty("managedLedgerOffloadThresholdInBytes", "" + (brokerOffloadThreshold));
        brokerProperties.setProperty("managedLedgerOffloadDeletionLagInMillis", "" + (brokerDeletionLag));
        brokerProperties.setProperty("managedLedgerOffloadedReadPriority", "" + (brokerReadPriority));
        // 2.2 offload properties name in conf file
        brokerProperties.setProperty("managedLedgerOffloadAutoTriggerSizeThresholdBytes", ""
                + brokerOffloadThreshold + 30);
        brokerProperties.setProperty("managedLedgerOffloadDeletionLagMs", "" + brokerDeletionLag + 30);
        brokerProperties.setProperty("managedLedgerDataReadPriority", "" + "tiered-storage-first");
        offloadPolicies = OffloadPoliciesImpl.create(brokerProperties);
        Assert.assertNotNull(offloadPolicies);
        Assert.assertEquals(offloadPolicies.getManagedLedgerOffloadThresholdInBytes(), brokerOffloadThreshold);
        Assert.assertEquals(offloadPolicies.getManagedLedgerOffloadDeletionLagInMillis(), brokerDeletionLag);
        Assert.assertEquals(offloadPolicies.getManagedLedgerOffloadedReadPriority().toString(), brokerReadPriority);
    }

    @Test
    public void testSupportExtraOffloadDrivers() throws Exception {
        System.setProperty("pulsar.extra.offload.drivers", "driverA, driverB");
        // using the custom classloader to reload the offload policies class to read the
        // system property correctly.
        TestClassLoader loader = new TestClassLoader();
        Class<?> clazz = loader.loadClass("org.apache.pulsar.common.policies.data.OffloadPoliciesImpl");
        Object o = clazz.getDeclaredConstructor().newInstance();
        clazz.getDeclaredMethod("setManagedLedgerOffloadDriver", String.class).invoke(o, "driverA");
        Method method = clazz.getDeclaredMethod("driverSupported");
        Assert.assertEquals(method.invoke(o), true);
        clazz.getDeclaredMethod("setManagedLedgerOffloadDriver", String.class).invoke(o, "driverB");
        Assert.assertEquals(method.invoke(o), true);
        clazz.getDeclaredMethod("setManagedLedgerOffloadDriver", String.class).invoke(o, "driverC");
        Assert.assertEquals(method.invoke(o), false);
        clazz.getDeclaredMethod("setManagedLedgerOffloadDriver", String.class).invoke(o, "aws-s3");
        Assert.assertEquals(method.invoke(o), true);
    }

    // this is used for the testSupportExtraOffloadDrivers. Because we need to change the system property,
    // we need to reload the class to read the system property.
    static class TestClassLoader extends ClassLoader {
        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException {
            if (name.contains("OffloadPoliciesImpl")) {
                return getClass(name);
            }
            return super.loadClass(name);
        }

        private Class<?> getClass(String name) {
            String file = name.replace('.', File.separatorChar) + ".class";
            Path targetPath = Paths.get(getClass().getClassLoader().getResource(".").getPath()).getParent();
            file = Paths.get(targetPath.toString(), "classes", file).toString();
            byte[] byteArr = null;
            try {
                byteArr = loadClassData(file);
                Class<?> c = defineClass(name, byteArr, 0, byteArr.length);
                resolveClass(c);
                return c;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        private byte[] loadClassData(String name) throws IOException {
            InputStream stream = Files.newInputStream(Paths.get(name));
            int size = stream.available();
            byte buff[] = new byte[size];
            DataInputStream in = new DataInputStream(stream);
            // Reading the binary data
            in.readFully(buff);
            in.close();
            return buff;
        }
    }

    @Test
    public void testCreateOffloadPoliciesWithExtraConfiguration() {
        Properties properties = new Properties();
        properties.put(EXTRA_CONFIG_PREFIX + "Key1", "value1");
        properties.put(EXTRA_CONFIG_PREFIX + "Key2", "value2");
        OffloadPoliciesImpl policies = OffloadPoliciesImpl.create(properties);

        Map<String, String> extraConfigurations = policies.getManagedLedgerExtraConfigurations();
        Assert.assertEquals(extraConfigurations.size(), 2);
        Assert.assertEquals(extraConfigurations.get("Key1"), "value1");
        Assert.assertEquals(extraConfigurations.get("Key2"), "value2");
    }

    /**
     * Test toProperties as well as create from properties.
     * @throws Exception
     */
    @Test
    public void testToProperties() throws Exception {
        // Base information convert.
        OffloadPoliciesImpl offloadPolicies = OffloadPoliciesImpl.create("aws-s3", "test-region", "test-bucket",
                "http://test.endpoint", null, null, null, null, 32 * 1024 * 1024, 5 * 1024 * 1024,
                10 * 1024 * 1024L, 100L, 10000L, OffloadedReadPriority.TIERED_STORAGE_FIRST);
        assertEquals(offloadPolicies, OffloadPoliciesImpl.create(offloadPolicies.toProperties()));

        // Set useless config to offload policies. Make sure convert conversion result is the same.
        offloadPolicies.setFileSystemProfilePath("/test/file");
        assertEquals(offloadPolicies, OffloadPoliciesImpl.create(offloadPolicies.toProperties()));

        // Set extra config to offload policies. Make sure convert conversion result is the same.
        Map<String, String> extraConfiguration = new HashMap<>();
        extraConfiguration.put("key1", "value1");
        extraConfiguration.put("key2", "value2");
        offloadPolicies.setManagedLedgerExtraConfigurations(extraConfiguration);
        assertEquals(offloadPolicies, OffloadPoliciesImpl.create(offloadPolicies.toProperties()));
    }

    @Test
    public void testDefaultOffloadPolicies() {
        OffloadPoliciesImpl offloadPolicies = new OffloadPoliciesImpl();
        assertNull(offloadPolicies.getManagedLedgerOffloadedReadPriority());
        assertNull(offloadPolicies.getManagedLedgerOffloadDeletionLagInMillis());
        assertNull(offloadPolicies.getManagedLedgerOffloadThresholdInSeconds());
        assertNull(offloadPolicies.getManagedLedgerOffloadThresholdInBytes());
    }
}
