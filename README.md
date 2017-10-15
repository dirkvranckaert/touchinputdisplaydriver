# Touch Input Display Drivern for Android Things
## Android Things and touch input displays
For the Raspberry Pi 3 the Android Things platform so far only supports the official RPi screen to be used with the touch input.
The cheaper screens (mostly Chinese) are not supported for touch input. That's the reason for creating this library. Initially I had only two screens: A 3.5" Kedei and a 5" Waveshare screen. Both are using the XPT2046 touch controller.
## Supported Displays
Out of the box the following displays are supported, however the system is free and open to add your own driver:
* Kedei HDMI (Tested with a 3.5")
* Waveshare HDMI (Tested with a 5")
## Usage
### Default drivers
You can use the library in two different ways:

1. Using the `TouchScreenDriverApplication`
Add an `Application` to your source code and reference it from the AndroidManifest. Then extend your application from the `TouchScreenDriverApplication` and override the `getDriverProfile()` method.
The `DriverProfile` can be initiated using `WaveshareProfile.getInstance(WaveshareProfile.DIMENSION_800_480);` or `KedeiProfile.getInstance(KedeiProfile.DIMENSION_480_320);`. This will make sure the correct driver for your display configuration/dimensions will be loaded.

2. Implementing the `TouchScreenDriverManager` yourself
If you cannot extend your `Application` from the `TouchScreenDriverApplication` then you can use the `TouchScreenDriverManager` to load and unload the driver in the `onCreate` and `onDestroy` methods of your `Application` instance. The manager is a singleton class that manages any state for you. Using this method you will also have to impelement a callback that provides `DriverProfile` for your screen.

### Custom Drivers
Adding a custom driver is really simple. The hardest part in a custom driver is knowing how to read from your touch input display.

To implement the driver in the library you should extend the `eu.vranckaert.touch.driver.Driver` class or the `eu.vranckaert.touch.driver.SpiDriver` class in case your touch input display is SPI compatible. If your touch input display uses the XPT2046 touch controller you can even directly extend the `eu.vranckaert.touch.driver.XPT2046Driver` class. It's the driver class that will contain the reading/writing logic for your touch display.

Next you need a `eu.vranckaert.touch.profile.DriverProfile` which needs a `Vendor` (or `Vendor.UNKNOWN` in case your vendor is not yet listed), and a `ScreenDimension` specifying the width, height and screen ratio. The `Ratio` is an enum with fixed ratio values, or again a `Ratio.UNKNOWN` if the ratio is not yet listed.

## Contributing
I would like to see new drivers added to this library. So please let me knonw or please contribute to the code base to get your dirver added!
