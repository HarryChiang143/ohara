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

package com.island.ohara.connector.ftp

import com.island.ohara.common.util.CommonUtils
import com.island.ohara.kafka.connector.TaskSetting

case class FtpSourceTaskProps(hash: Int,
                              total: Int,
                              inputFolder: String,
                              completedFolder: Option[String],
                              errorFolder: String,
                              encode: String,
                              hostname: String,
                              port: Int,
                              user: String,
                              password: String) {
  def toMap: Map[String, String] = Map(
    FTP_HASH -> hash.toString,
    FTP_TOTAL -> total.toString,
    FTP_INPUT -> inputFolder,
    FTP_COMPLETED_FOLDER -> completedFolder.getOrElse(""),
    FTP_ERROR -> errorFolder,
    FTP_ENCODE -> encode,
    FTP_HOSTNAME -> hostname,
    FTP_PORT -> port.toString,
    FTP_USER_NAME -> user,
    FTP_PASSWORD -> password
  ).filter(_._2.nonEmpty)
}

object FtpSourceTaskProps {
  def apply(settings: TaskSetting): FtpSourceTaskProps = FtpSourceTaskProps(
    hash = settings.intValue(FTP_HASH),
    total = settings.intValue(FTP_TOTAL),
    inputFolder = settings.stringValue(FTP_INPUT),
    completedFolder = Option(settings.stringOption(FTP_COMPLETED_FOLDER).orElse(null)).filterNot(CommonUtils.isEmpty),
    errorFolder = settings.stringValue(FTP_ERROR),
    encode = settings.stringOption(FTP_ENCODE).orElse(FTP_ENCODE_DEFAULT),
    hostname = settings.stringValue(FTP_HOSTNAME),
    port = settings.intValue(FTP_PORT),
    user = settings.stringValue(FTP_USER_NAME),
    password = settings.stringValue(FTP_PASSWORD)
  )
}
