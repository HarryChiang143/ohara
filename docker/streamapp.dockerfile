#
# Copyright 2019 is-land
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

FROM openjdk:8u171-jdk-alpine as deps
MAINTAINER sam cho <sam@is-land.com.tw>

ARG GRADLE_VERSION=5.4.1


RUN apk --no-cache add git && rm -rf /tmp/* /var/cache/apk/* && \
 mkdir -p /opt/lib && \
 rm -rf /var/lib/apt/lists/*

# download gradle
WORKDIR /opt/gradle
RUN wget https://downloads.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip && \
 unzip gradle-$GRADLE_VERSION-bin.zip && \
 rm -f gradle-$GRADLE_VERSION-bin.zip && \
 ln -s /opt/gradle/gradle-$GRADLE_VERSION /opt/gradle/default

# add gradle to path
ENV GRADLE_HOME=/opt/gradle/default
ENV PATH=$PATH:$GRADLE_HOME/bin

# build ohara-streams
ARG BRANCH="master"
ARG COMMIT=$BRANCH
ARG REPO="https://github.com/oharastream/ohara.git"
ARG BEFORE_BUILD=""
WORKDIR /testpatch/ohara
RUN git clone $REPO /testpatch/ohara
RUN git checkout $COMMIT
RUN if [[ "$BEFORE_BUILD" != "" ]]; then /bin/sh -c "$BEFORE_BUILD" ; fi

# copy required jars except test jar
RUN gradle jar -x test && \
 cp `ls /testpatch/ohara/*/build/libs/*.jar | grep -v tests.jar | grep -E 'common|kafka|metrics|streams'` /opt/lib

# download all dependencies
RUN gradle ohara-stream:copyDependencies

FROM centos:7.6.1810

RUN yum -y update && \
 yum -y install java-1.8.0-openjdk-headless wget && \
 yum clean all && \
 rm -rf /var/cache/yum

# add user from root to kafka
ARG USER=ohara
RUN groupadd $USER
RUN useradd -ms /bin/bash -g $USER $USER

# copy required library
COPY --from=deps /opt/lib/* /opt/ohara/

# clone ohara binary
COPY --from=deps /testpatch/ohara/bin/* /home/$USER/default/
COPY --from=deps /testpatch/ohara/docker/streamapp.sh /home/$USER/default/
RUN mkdir /home/$USER/lib && \
 ln -s /opt/ohara/*.jar /home/$USER/lib && \
 chown -R $USER:$USER /home/$USER/default && \
 chmod +x /home/$USER/default/*.sh
ENV OHARA_HOME=/home/$USER/default
ENV PATH=$PATH:$OHARA_HOME

# add Tini
ARG TINI_VERSION=v0.18.0
RUN wget https://github.com/krallin/tini/releases/download/${TINI_VERSION}/tini -O /tini
RUN chmod +x /tini

# change user
USER $USER

ENTRYPOINT ["/tini", "--", "streamapp.sh"]