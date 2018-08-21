[![](https://jitpack.io/v/adheus/playkit-dtg-android.svg)](https://jitpack.io/#adheus/playkit-dtg-android)
[![Travis](https://img.shields.io/travis/adheus/playkit-dtg-android.svg)](https://travis-ci.org/adheus/playkit-dtg-android)

This is a fork of from the original repo from Playkit (https://github.com/kaltura/playkit-dtg-android)

# PlayKit DTG - Download To Go

Kaltura PlayKit DTG is an Android library that enables downloading MPEG-DASH and HLS streams for offline viewing.

* Track selection for video/audio/captions
* Widevine modular DRM

Documentation: https://kaltura.github.io/playkit-dtg-android/

## Setup
Add it in your root build.gradle at the end of repositories:

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}

Add build.gradle dependency:

	dependencies {
		implementation 'com.github.adheus:playkit-dtg-android:develop-SNAPSHOT'
	}


Replace `develop-SNAPSHOT'` with the latest release.
