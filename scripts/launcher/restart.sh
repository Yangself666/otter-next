#!/usr/bin/env bash
script_dir=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd) || exit 1
exec bash "$script_dir/otter.sh" restart "$@"
