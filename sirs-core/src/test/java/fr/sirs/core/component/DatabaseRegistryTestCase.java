/**
 * This file is part of SIRS-Digues 2.
 *
 * Copyright (C) 2016, FRANCE-DIGUES,
 *
 * SIRS-Digues 2 is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * SIRS-Digues 2 is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * SIRS-Digues 2. If not, see <http://www.gnu.org/licenses/>
 */
package fr.sirs.core.component;

import org.junit.Test;

import fr.sirs.core.CouchDBTestCase;
import java.io.IOException;
import java.util.UUID;
import org.apache.sis.test.DependsOnMethod;
import org.junit.Assert;
import org.junit.BeforeClass;

public class DatabaseRegistryTestCase extends CouchDBTestCase {

    private static String REPLICATION_DEST;

    @BeforeClass
    public static void init() {
        REPLICATION_DEST = "sirs-test-dup".concat(UUID.randomUUID().toString());
        deleteAfterClass(REPLICATION_DEST);
    }

    @Test
    public void databaseList() throws IOException {
        Assert.assertTrue(
                "Database list does not contain target databasse !",
                REGISTRY.listSirsDatabases().contains(DB_NAME));
    }

    @DependsOnMethod("databaseList")
    @Test
    public void initDatabaseFromRemote() throws IOException {
        REGISTRY.synchronizeSirsDatabases(DB_NAME, REPLICATION_DEST, true);
    }

    @DependsOnMethod("initDatabaseFromRemote")
    @Test
    public void getReplicationTasks() throws IOException {
        Assert.assertTrue("No replication task found !", REGISTRY.getReplicationTasks().findAny().isPresent());
    }

    @DependsOnMethod("getReplicationTasks")
    @Test
    public void getSynchronizationTask() throws IOException {
        Assert.assertTrue(REGISTRY.getSynchronizationTasks(REPLICATION_DEST).count() > 0);
    }

    @DependsOnMethod("getSynchronizationTask")
    @Test
    public void dropDatabase() throws IOException {
        REGISTRY.dropDatabase(REPLICATION_DEST);
        Assert.assertFalse(
                "Database list contains a deleted database !",
                REGISTRY.listSirsDatabases().contains(REPLICATION_DEST));
    }
}
