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

package com.island.ohara.it.agent.k8s

import com.island.ohara.agent.k8s.K8SClient
import com.island.ohara.client.configurator.v0.NodeApi.Node
import com.island.ohara.common.util.CommonUtils
import com.island.ohara.configurator.Configurator
import com.island.ohara.it.agent.{BasicTests4StreamApp, ClusterNameHolder}
class TestK8SStreamApp extends BasicTests4StreamApp {

  private[this] val API_SERVER_URL: Option[String] = sys.env.get("ohara.it.k8s")
  private[this] val NODE_SERVER_NAME: Option[String] = sys.env.get("ohara.it.k8s.nodename")

  override protected def createNodes(): Seq[Node] = if (API_SERVER_URL.isEmpty || NODE_SERVER_NAME.isEmpty) Seq.empty
  else
    NODE_SERVER_NAME.get
      .split(",")
      .map(node =>
        Node(
          hostname = node,
          port = Some(22),
          user = Some("fake"),
          password = Some("fake"),
          services = Seq.empty,
          lastModified = CommonUtils.current(),
          validationReport = None,
          tags = Map.empty
      ))

  override protected def createNameHolder(nodeCache: Seq[Node]): ClusterNameHolder = if (API_SERVER_URL.isEmpty) null
  else ClusterNameHolder(nodeCache, K8SClient(API_SERVER_URL.get))

  override protected def createConfigurator(nodeCache: Seq[Node], hostname: String, port: Int): Configurator =
    Configurator.builder.hostname(hostname).port(port).k8sClient(K8SClient(API_SERVER_URL.get)).build()
}
