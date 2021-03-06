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

package com.island.ohara.streams.ostream;

import com.island.ohara.common.data.Serializer;
import java.util.Map;

// Kafka use it's own serializer to initial Serdes object, we need to implement that
public class BytesSerializer implements org.apache.kafka.common.serialization.Serializer<byte[]> {

  @Override
  public void configure(Map<String, ?> configs, boolean isKey) {}

  @Override
  public byte[] serialize(String topic, byte[] data) {
    if (data == null) return null;
    else return Serializer.BYTES.to(data);
  }

  @Override
  public void close() {}
}
