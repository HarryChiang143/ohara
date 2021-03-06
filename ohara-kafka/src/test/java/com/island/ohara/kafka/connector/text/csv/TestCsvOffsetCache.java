/*
 * Copyright 2019 is-land
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

package com.island.ohara.kafka.connector.text.csv;

import com.island.ohara.common.rule.SmallTest;
import org.junit.Assert;
import org.junit.Test;

public class TestCsvOffsetCache extends SmallTest {
  private final String path = getClass().getName();

  @Test
  public void testUpdate() {
    CsvOffsetCache cache = new CsvOffsetCache();
    cache.update(path, 1);
    Assert.assertTrue(cache.predicate(path, 2));
    Assert.assertFalse(cache.predicate(path, 1));
  }
}
