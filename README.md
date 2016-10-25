# autolaunch-android-experiments
![Cover](../david/screenshots/cover.png?raw=true)

An application for prototyping and testing Pearl magnetic mount detection algorithms.

## Overview

[`MagneticMountDetector`](https://github.com/Kamama/autolaunch-android-experiments/blob/david/app/src/main/java/com/test/compassapp/MagneticMountDetector.java) receives sensor data and passes it to a `SensorAlgo` which returns a confidence double
between `FULL_CONFIDENCE_OFF_MAGNET` and `FULL_CONFIDENCE_ON_MAGNET`, with `ZERO_CONFIDENCE` being the midpoint.

To add [algorithms](https://github.com/Kamama/autolaunch-android-experiments/tree/david/app/src/main/java/com/test/compassapp/algos), you implement the `SensorAlgo` interface, then call `magneticMountDetector.setAlgo(yourAlgo)`.

Currently, the [`YZThresholdAlgo`](https://github.com/Kamama/autolaunch-android-experiments/blob/david/app/src/main/java/com/test/compassapp/algos/YZThresholdAlgo.java) seems to be the most effective, as it leverages the fact that the placement of the Pearl mount provokes nearly equal and opposite field readings on the Y and Z axes.


