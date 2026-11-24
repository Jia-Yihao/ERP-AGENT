#!/bin/sh
# 将 Sentinel 规则与 Seata 服务端配置发布到 Nacos，供各服务启动时加载
set -eu

NACOS_ADDR="${NACOS_ADDR:-nacos:8848}"
SEED_DIR="${SEED_DIR:-/seed}"

echo "waiting for nacos ${NACOS_ADDR}"
until curl -fsS "http://${NACOS_ADDR}/nacos/v1/console/health/readiness" >/dev/null 2>&1; do
  sleep 2
done

publish() {
  data_id="$1"
  group="$2"
  file="$3"
  code=$(curl -s -o /tmp/publish.out -w '%{http_code}' -X POST \
    "http://${NACOS_ADDR}/nacos/v1/cs/configs" \
    --data-urlencode "dataId=${data_id}" \
    --data-urlencode "group=${group}" \
    --data-urlencode "type=text" \
    --data-urlencode "content@${file}")
  if [ "${code}" != "200" ]; then
    echo "publish ${data_id} failed: HTTP ${code} $(cat /tmp/publish.out)"
    exit 1
  fi
  echo "publish ${data_id} -> ${group} ok"
}

for f in "${SEED_DIR}"/sentinel/*.json; do
  name=$(basename "${f}" .json)
  publish "${name}" SENTINEL_GROUP "${f}"
done

sed "s#root123456#${MYSQL_ROOT_PASSWORD:-root123456}#" \
  "${SEED_DIR}/seata/seataServer.properties" > /tmp/seataServer.properties
publish seataServer.properties SEATA_GROUP /tmp/seataServer.properties

echo "seed finished"
