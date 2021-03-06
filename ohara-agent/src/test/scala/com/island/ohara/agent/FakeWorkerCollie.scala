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

package com.island.ohara.agent

import com.island.ohara.agent.docker.ContainerState
import com.island.ohara.client.configurator.v0.ContainerApi.ContainerInfo
import com.island.ohara.client.configurator.v0.NodeApi
import com.island.ohara.client.configurator.v0.WorkerApi.WorkerClusterInfo

import scala.concurrent.{ExecutionContext, Future}

class FakeWorkerCollie(node: NodeCollie, brokerClusters: Map[String, Seq[ContainerInfo]]) extends WorkerCollie {
  override protected def brokerContainers(clusterName: String)(
    implicit executionContext: ExecutionContext): Future[Seq[ContainerInfo]] =
    Future.successful(
      brokerClusters.get(clusterName).getOrElse(throw new NoSuchClusterException(s"$clusterName does not exist")))

  override protected def doCreator(executionContext: ExecutionContext,
                                   clusterName: String,
                                   containerName: String,
                                   containerInfo: ContainerInfo,
                                   node: NodeApi.Node,
                                   route: Map[String, String]): Future[Unit] = Future.unit

  override def logs(clusterName: String)(
    implicit executionContext: ExecutionContext): Future[Map[ContainerInfo, String]] =
    throw new UnsupportedOperationException("FakeWorkerCollie doesn't support logs function")

  override def clusterWithAllContainers()(
    implicit executionContext: ExecutionContext): Future[Map[WorkerClusterInfo, Seq[ContainerInfo]]] =
    Future.successful(
      Map(
        WorkerClusterInfo(
          "wk1",
          "worker",
          "bk1",
          8083,
          8084,
          "aaa",
          "statustopic",
          1,
          1,
          "conftopic",
          1,
          1,
          "offsettopic",
          1,
          1,
          Seq.empty,
          Seq.empty,
          Set("node1"),
          Set.empty,
          Map.empty,
          0L,
          Some(ContainerState.RUNNING.name),
          None
        ) -> Seq(
          ContainerInfo("node1",
                        "aaaa",
                        "connect-worker",
                        "2019-05-28 00:00:00",
                        "RUNNING",
                        "unknown",
                        "ohara-xxx-wk-0000",
                        "unknown",
                        Seq.empty,
                        Map.empty,
                        "ohara-xxx-wk-0000")))
    )
  override protected def resolveHostName(hostname: String): String = hostname

  override protected def nodeCollie: NodeCollie = node

  override protected def prefixKey: String = "fakeworker"

  override protected def doRemove(clusterInfo: WorkerClusterInfo, containerInfos: Seq[ContainerInfo])(
    implicit executionContext: ExecutionContext): Future[Boolean] =
    throw new UnsupportedOperationException("FakeWorkerCollie doesn't support this function")

  override protected def doAddNode(
    previousCluster: WorkerClusterInfo,
    previousContainers: Seq[ContainerInfo],
    newNodeName: String)(implicit executionContext: ExecutionContext): Future[WorkerClusterInfo] =
    throw new UnsupportedOperationException("FakeWorkerCollie doesn't support this function")

  override protected def doRemoveNode(previousCluster: WorkerClusterInfo, beRemovedContainer: ContainerInfo)(
    implicit executionContext: ExecutionContext): Future[Boolean] =
    throw new UnsupportedOperationException("FakeWorkerCollie doesn't support this function")

  // In fake mode, we don't care the cluster state
  override protected def toClusterState(containers: Seq[ContainerInfo]): Option[ClusterState] =
    Some(ClusterState.RUNNING)
}
