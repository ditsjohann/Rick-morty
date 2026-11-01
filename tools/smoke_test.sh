#!/usr/bin/env bash
# Headless dedicated-server smoke test: boots the dev server, generates chunks in both custom dimensions,
# places every custom feature and summons every mob, then scans the log for errors.
set -u
cd "$(dirname "$0")/.."
mkdir -p run
echo "eula=true" > run/eula.txt
cat > run/server.properties <<'PROPS'
online-mode=false
level-seed=12345
server-port=25599
spawn-protection=0
difficulty=normal
view-distance=4
simulation-distance=4
PROPS
rm -rf run/world run/crash-reports
(
  sleep "${BOOT_WAIT:-75}"
  echo "execute in mayhem:gloopverse run forceload add 0 0 48 48"; sleep 20
  echo "execute in mayhem:meatverse run forceload add 0 0 48 48"; sleep 20
  echo "execute in mayhem:gloopverse run locate biome mayhem:jelly_shallows"; sleep 5
  echo "forceload add 0 0 32 32"; sleep 8
  echo "fill 0 100 0 24 100 24 minecraft:grass_block"; sleep 2
  echo "place feature mayhem:crashed_saucer 12 101 12"; sleep 3
  echo "execute in mayhem:meatverse if biome 8 40 8 mayhem:flesh_caverns run say MEATVERSE_BIOME_OK"; sleep 2
  echo "execute in mayhem:meatverse run place feature mayhem:flesh_tendril 8 -20 8"; sleep 2
  echo "execute in mayhem:gloopverse run place feature mayhem:flux_spire 8 100 8"; sleep 2
  for e in glooper meat_crawler watcher customs_agent zorp defense_toaster thought_leech omni_eye portal black_hole; do
    echo "summon mayhem:$e 0 120 0"; sleep 1
  done
  echo "execute in mayhem:meatverse run summon mayhem:omni_eye 8 40 8"; sleep 1
  echo "execute in mayhem:meatverse run summon mayhem:meat_crawler 8 40 8"; sleep 10
  echo "kill @e[type=mayhem:meat_crawler]"; sleep 3
  echo "execute in mayhem:meatverse run kill @e[type=mayhem:meat_crawler]"; sleep 3
  echo "execute in mayhem:meatverse run kill @e[type=mayhem:omni_eye]"; sleep 3
  echo "list"; sleep 2
  echo "stop"
) | timeout 600 ./gradlew --no-daemon runServer > run/smoke.log 2>&1
grep -v JAVA_TOOL run/smoke.log | grep -vi "data fixer" | grep -E "ERROR|Exception|Done \(|Placed|Summoned|Killed|Failed|Could not|Unknown|biome|Marked|nearest|Stopping the server|\[Server\]" | head -60
