#!/usr/bin/env bash

DEFAULT_SINK=$(pacmd stat | awk -F": " '/^Default sink name: /{print $2}')
SINK_DATA=$(pacmd list-sinks)

VOLUME=$(echo "$SINK_DATA" | awk '/^\s+name: /{def = $2 == "<'$DEFAULT_SINK'>"} /^\s+volume: / && def {print $5}')
MUTED=$(echo "$SINK_DATA" | awk '/^\s+name: /{def = $2 == "<'$DEFAULT_SINK'>"} /^\s+muted: / && def {print $2}')

echo "$VOLUME;$MUTED"