#!/bin/bash
#
#   Licensed to the Apache Software Foundation (ASF) under one or more
#   contributor license agreements.  See the NOTICE file distributed with
#   this work for additional information regarding copyright ownership.
#   The ASF licenses this file to You under the Apache License, Version 2.0
#   (the "License"); you may not use this file except in compliance with
#   the License.  You may obtain a copy of the License at
#
#        http://www.apache.org/licenses/LICENSE-2.0
#
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.
#

wait_for_service() {
  local service_name="${1}"
  echo "Waiting for service '${service_name}' (max 120s)"
  local count=120
  until podman ps --format "{{.Names}} {{.Status}}" --filter name="${service_name}" | grep -q "healthy" && [[ count -gt 0 ]]
  do
    sleep 1
    ((count--))
  done
}

zookeeper() {
  podman pod create \
    --name zookeeper \
    --hostname zookeeper \
    --infra-name zookeeper_infra \
    --network test_net \
    --replace

  podman run -d \
    --name zookeeper_service \
    --pod zookeeper \
    --env ZOOKEEPER_CLIENT_PORT=2181 \
    --env ZOOKEEPER_TICK_TIME=2000 \
    --health-cmd '[ "nc", "-z", "localhost", "2181" ]' \
    --health-interval 30s \
    --health-timeout 10s \
    --health-retries 5 \
    --health-start-period 120s \
    docker.io/confluentinc/cp-zookeeper:7.3.3

  wait_for_service "zookeeper_service"
}

kafka() {
  podman pod create \
    --name broker \
    --hostname broker \
    --infra-name broker_infra \
    --publish 9092:9092 \
    --network test_net \
    --replace

  podman run -d \
    --name kafka_broker \
    --pod broker \
    --env KAFKA_BROKER_ID=1 \
    --env KAFKA_ZOOKEEPER_CONNECT='zookeeper:2181' \
    --env KAFKA_LISTENER_SECURITY_PROTOCOL_MAP=PLAINTEXT:PLAINTEXT,PLAINTEXT_INTERNAL:PLAINTEXT \
    --env KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://broker:9092,PLAINTEXT_INTERNAL://broker:29092 \
    --env KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 \
    --env KAFKA_TRANSACTION_STATE_LOG_MIN_ISR=1 \
    --env KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR=1 \
    --health-cmd '[ "kafka-topics", "--bootstrap-server", "broker:9092", "--list" ]' \
    --health-interval 30s \
    --health-timeout 10s \
    --health-retries 5 \
    --health-start-period 120s \
    docker.io/confluentinc/cp-kafka:7.3.3

    wait_for_service "kafka_broker"
}

pulsar() {
  podman pod create \
    --name broker \
    --hostname broker \
    --infra-name broker_infra \
    --publish 8080:8080 \
    --publish 6650:6650 \
    --network test_net \
    --replace

  podman run -d \
    --name pulsar_broker \
    --pod broker \
    --volume /tmp/pulsar/broker/data:/pulsar/data:Z \
    --health-cmd '[ "bin/pulsar-admin", "brokers", "healthcheck" ]' \
    --health-interval 30s \
    --health-timeout 10s \
    --health-retries 5 \
    --health-start-period 120s \
    docker.io/apachepulsar/pulsar:2.11.0 \
    bin/pulsar standalone

    wait_for_service "pulsar_broker"
}

main-router() {
  podman pod create \
    --name main_router \
    --hostname main_router \
    --infra-name main_router_infra \
    --publish 8082:8082 \
    --network test_net \
    --replace

  podman run -d \
    --name main_router_service \
    --pod main_router \
    --env THC_PATH=/main-router/actuator/health \
    --env THC_PORT=8082 \
    --health-cmd '[ "/cnb/process/health-check" ]' \
    --health-interval 30s \
    --health-timeout 10s \
    --health-retries 5 \
    --health-start-period 120s \
    docker.io/library/main-router:latest

    wait_for_service "main_router_service"
}

number-statistics-service() {
  podman pod create \
    --name number_statistics \
    --hostname number_statistics \
    --infra-name number_statistics_infra \
    --publish 8913:8913 \
    --network test_net \
    --replace

  podman run -d \
    --name number_statistics_service \
    --pod number_statistics \
    --env THC_PATH=/number-statistics/actuator/health \
    --env THC_PORT=8913 \
    --health-cmd '[ "/cnb/process/health-check" ]' \
    --health-interval 30s \
    --health-timeout 10s \
    --health-retries 5 \
    --health-start-period 120s \
    docker.io/library/number-statistics-service:latest
}

number-generator-service() {
  podman pod create \
    --name number_generator \
    --hostname number_generator \
    --infra-name number_generator_infra \
    --publish 8912:8912 \
    --network test_net \
    --replace

  podman run -d \
    --name number_generator_service \
    --pod number_generator \
    --env THC_PATH=/number-generator/actuator/health \
    --env THC_PORT=8912 \
    --health-cmd '[ "/cnb/process/health-check" ]' \
    --health-interval 30s \
    --health-timeout 10s \
    --health-retries 5 \
    --health-start-period 120s \
    docker.io/library/number-generator-service:latest
}

all-numbers-service() {
  podman pod create \
    --name all_numbers \
    --hostname all_numbers \
    --infra-name all_numbers_infra \
    --publish 8911:8911 \
    --network test_net \
    --replace

  podman run -d \
    --name all_numbers_service \
    --pod all_numbers \
    --env THC_PATH=/all-numbers/actuator/health \
    --env THC_PORT=8911 \
    --health-cmd '[ "/cnb/process/health-check" ]' \
    --health-interval 30s \
    --health-timeout 10s \
    --health-retries 5 \
    --health-start-period 120s \
    docker.io/library/all-numbers-service:latest
}

prime-numbers-service() {
  podman pod create \
    --name prime_numbers \
    --hostname prime_numbers \
    --infra-name prime_numbers_infra \
    --publish 8923:8923 \
    --network test_net \
    --replace

  podman run -d \
    --name prime_numbers_service \
    --pod prime_numbers \
    --env THC_PATH=/prime-numbers/actuator/health \
    --env THC_PORT=8923 \
    --health-cmd '[ "/cnb/process/health-check" ]' \
    --health-interval 30s \
    --health-timeout 10s \
    --health-retries 5 \
    --health-start-period 120s \
    docker.io/library/prime-numbers-service:latest
}

tens-numbers-service() {
  podman pod create \
    --name tens_numbers \
    --hostname tens_numbers \
    --infra-name tens_numbers_infra \
    --publish 8910:8910 \
    --network test_net \
    --replace

  podman run -d \
    --name tens_numbers_service \
    --pod tens_numbers \
    --env THC_PATH=/tens-numbers/actuator/health \
    --env THC_PORT=8910 \
    --health-cmd '[ "/cnb/process/health-check" ]' \
    --health-interval 30s \
    --health-timeout 10s \
    --health-retries 5 \
    --health-start-period 120s \
    docker.io/library/tens-numbers-service:latest
}

nines-numbers-service() {
  podman pod create \
    --name nines_numbers \
    --hostname nines_numbers \
    --infra-name nines_numbers_infra \
    --publish 8909:8909 \
    --network test_net \
    --replace

  podman run -d \
    --name nines_numbers_service \
    --pod nines_numbers \
    --env THC_PATH=/nines-numbers/actuator/health \
    --env THC_PORT=8909 \
    --health-cmd '[ "/cnb/process/health-check" ]' \
    --health-interval 30s \
    --health-timeout 10s \
    --health-retries 5 \
    --health-start-period 120s \
    docker.io/library/nines-numbers-service:latest
}

eights-numbers-service() {
  podman pod create \
    --name eights_numbers \
    --hostname eights_numbers \
    --infra-name eights_numbers_infra \
    --publish 8908:8908 \
    --network test_net \
    --replace

  podman run -d \
    --name eights_numbers_service \
    --pod eights_numbers \
    --env THC_PATH=/eights-numbers/actuator/health \
    --env THC_PORT=8908 \
    --health-cmd '[ "/cnb/process/health-check" ]' \
    --health-interval 30s \
    --health-timeout 10s \
    --health-retries 5 \
    --health-start-period 120s \
    docker.io/library/eights-numbers-service:latest
}

sevens-numbers-service() {
  podman pod create \
    --name sevens_numbers \
    --hostname sevens_numbers \
    --infra-name sevens_numbers_infra \
    --publish 8907:8907 \
    --network test_net \
    --replace

  podman run -d \
    --name sevens_numbers_service \
    --pod sevens_numbers \
    --env THC_PATH=/sevens-numbers/actuator/health \
    --env THC_PORT=8907 \
    --health-cmd '[ "/cnb/process/health-check" ]' \
    --health-interval 30s \
    --health-timeout 10s \
    --health-retries 5 \
    --health-start-period 120s \
    docker.io/library/sevens-numbers-service:latest
}

sixes-numbers-service() {
  podman pod create \
    --name sixes_numbers \
    --hostname sixes_numbers \
    --infra-name sixes_numbers_infra \
    --publish 8906:8906 \
    --network test_net \
    --replace

  podman run -d \
    --name sixes_numbers_service \
    --pod sixes_numbers \
    --env THC_PATH=/sixes-numbers/actuator/health \
    --env THC_PORT=8906 \
    --health-cmd '[ "/cnb/process/health-check" ]' \
    --health-interval 30s \
    --health-timeout 10s \
    --health-retries 5 \
    --health-start-period 120s \
    docker.io/library/sixes-numbers-service:latest
}

fives-numbers-service() {
  podman pod create \
    --name fives_numbers \
    --hostname fives_numbers \
    --infra-name fives_numbers_infra \
    --publish 8905:8905 \
    --network test_net \
    --replace

  podman run -d \
    --name fives_numbers_service \
    --pod fives_numbers \
    --env THC_PATH=/fives-numbers/actuator/health \
    --env THC_PORT=8905 \
    --health-cmd '[ "/cnb/process/health-check" ]' \
    --health-interval 30s \
    --health-timeout 10s \
    --health-retries 5 \
    --health-start-period 120s \
    docker.io/library/fives-numbers-service:latest
}

fours-numbers-service() {
  podman pod create \
    --name fours_numbers \
    --hostname fours_numbers \
    --infra-name fours_numbers_infra \
    --publish 8904:8904 \
    --network test_net \
    --replace

  podman run -d \
    --name fours_numbers_service \
    --pod fours_numbers \
    --env THC_PATH=/fours-numbers/actuator/health \
    --env THC_PORT=8904 \
    --health-cmd '[ "/cnb/process/health-check" ]' \
    --health-interval 30s \
    --health-timeout 10s \
    --health-retries 5 \
    --health-start-period 120s \
    docker.io/library/fours-numbers-service:latest
}

threes-numbers-service() {
  podman pod create \
    --name threes_numbers \
    --hostname threes_numbers \
    --infra-name threes_numbers_infra \
    --publish 8903:8903 \
    --network test_net \
    --replace

  podman run -d \
    --name threes_numbers_service \
    --pod threes_numbers \
    --env THC_PATH=/threes-numbers/actuator/health \
    --env THC_PORT=8903 \
    --health-cmd '[ "/cnb/process/health-check" ]' \
    --health-interval 30s \
    --health-timeout 10s \
    --health-retries 5 \
    --health-start-period 120s \
    docker.io/library/threes-numbers-service:latest
}

even-numbers-service() {
  podman pod create \
    --name even_numbers \
    --hostname even_numbers \
    --infra-name even_numbers_infra \
    --publish 8902:8902 \
    --network test_net \
    --replace

  podman run -d \
    --name even_numbers_service \
    --pod even_numbers \
    --env THC_PATH=/even-numbers/actuator/health \
    --env THC_PORT=8902 \
    --health-cmd '[ "/cnb/process/health-check" ]' \
    --health-interval 30s \
    --health-timeout 10s \
    --health-retries 5 \
    --health-start-period 120s \
    docker.io/library/even-numbers-service:latest
}

odd-numbers-service() {
  podman pod create \
    --name odd_numbers \
    --hostname odd_numbers \
    --infra-name odd_numbers_infra \
    --publish 8901:8901 \
    --network test_net \
    --replace

  podman run -d \
    --name odd_numbers_service \
    --pod odd_numbers \
    --env THC_PATH=/odd-numbers/actuator/health \
    --env THC_PORT=8901 \
    --health-cmd '[ "/cnb/process/health-check" ]' \
    --health-interval 30s \
    --health-timeout 10s \
    --health-retries 5 \
    --health-start-period 120s \
    docker.io/library/odd-numbers-service:latest
}

start_services() {
  zookeeper
  kafka
  main-router
  number-statistics-service
  number-generator-service
  all-numbers-service
  prime-numbers-service
  tens-numbers-service
  nines-numbers-service
  eights-numbers-service
  sevens-numbers-service
  sixes-numbers-service
  fives-numbers-service
  fours-numbers-service
  threes-numbers-service
  even-numbers-service
  odd-numbers-service
}

prepare_resources() {
  podman unshare rm -rf /tmp/pulsar
  mkdir -p /tmp/pulsar/broker/data
  mkdir -p /tmp/pulsar/zookeeper
  mkdir -p /tmp/pulsar/bookkeeper
  chmod -R 777 /tmp/pulsar
  podman network create test_net --ignore
}

prepare_resources
start_services
