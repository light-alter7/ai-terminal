AI Terminal bootstrap helpers
=============================

This directory contains small, portable helpers that are copied to the
application's private usr/bin directory on first launch.

The Android system already supplies /system/bin/sh, so shell commands work
without a bundled distribution. A Python or full Termux-style userland is not
included in this source archive; it must be supplied separately as a
versioned, tested runtime bundle before Python/package commands can be
advertised.