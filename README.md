Magnite and the Magnite logo are trademarks of Magnite, Inc. Use of these marks is subject to Magnite’s trademark policy. The usage of the product is governed under Magnite Source‑Available License (Polyform‑style, Licensed Users Only) detailed in LICENSE.md.txt

To note, this is a sample app provided solely for the purpose of demonstrating an integration of the MagniteSDK into a publisher app. The code here should NOT be used for any other purpose.

# Magnite Ads SDK Demo

## Project structure

- [app-compose](app-compose) - contains a demo project with Kotlin 2.0, [Flows](https://developer.android.com/kotlin/flow) and [Jetpack Compose](https://developer.android.com/develop/ui/compose/documentation).
- [app-java](app-java) - contains a demo project with Java, [LiveData](https://developer.android.com/topic/libraries/architecture/livedata) and [XML layout](https://developer.android.com/develop/ui/views/layout/declaring-layout).

## Ad formats

- Interstitial
- Rewarded Video
- Banner (320x50)
- Mrec (300x250)
- Native

## Gradle dependency

```groovy
dependencies {
    // noinspection GradleDynamicVersion
    implementation 'com.magnite.sdk:ads-sdk:1.+'
}
```

```kotlin
dependencies {
    // noinspection GradleDynamicVersion
    implementation("com.magnite.sdk:ads-sdk:1.+")
}
```

## SDK initialization

In the examples below make sure to use your own App ID.

**Kotlin**

```kotlin
MagniteSDK.initParams(applicationContext, "YOUR_APP_ID")
    .setCallback { /* ready to request ads */ }
    .init()
```

**Java**

```java
MagniteSDK.initParams(applicationContext, "YOUR_APP_ID")
    .setCallback(new Runnable() {
        public void run () {
            // ready to request ads        
        }
    })
    .init();
```


