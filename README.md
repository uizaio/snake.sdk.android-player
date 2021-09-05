
## Welcome to Uiza Snake Player SDK

Simple Streaming at scale.

Uiza is the complete toolkit for building a powerful video streaming application with unlimited scalability. We design Uiza so simple that you only need a few lines of codes to start streaming, but sophisticated enough for you to build complex products on top of it.

Read  [CHANGELOG here](https://github.com/uizaio/snake.sdk.android-player/blob/master/CHANGELOG.md).

## [](https://github.com/uizaio/uiza-android-player-sdk/blob/master/README.md#importing-the-library)Importing the Library

**Step 1. Add the  `JitPack`  repository to your  `build.gradle`  file**

    allprojects {
          repositories {
             maven { url 'https://jitpack.io' }
          }
    }

**Step 2. Add the dependency**

    implementation 'com.github.uizaio:snake.sdk.android-player:x.y.z'

Get latest release number  [HERE](https://github.com/uizaio/snake.sdk.android-player/blob/master/CHANGELOG.md).


**Note:**

-   The version of the ExoPlayer Extension IMA must match the version of the ExoPlayer library being used.
-   If you are using both ChromeCast and IMA Ads dependencies, we recommend using dependency  `com.google.android.gms:play-services-cast-framework:$version`  with  `version >= 18.1.0`  or  `version=16.2.0`  (support compat) to avoid dependency version conflicts


## Init SDK

1.  Init UZPlayer


    class UZApplication : MultiDexApplication() {  
      override fun onCreate() {  
         super.onCreate()  
        UZPlayer.init(com.uiza.sdk.R.layout.uzplayer_skin_default)  
      }  
    }

### Manifest

    <application
      android:name=".UZApplication "
    >

## How to play the video?:

**XML**

    <com.uiza.sdk.view.UZVideoView  
      android:id="@+id/uzVideoView"  
      android:layout_width="match_parent"  
      android:layout_height="wrap_content" />

**JAVA**

Manifest

    <activity
      android:name=".MainActivity "
      android:configChanges="keyboard|keyboardHidden|orientation|screenSize|screenLayout|smallestScreenSize|uiMode" />

In your  `activity`  or  `fragment`

    if (uzVideoView.isViewCreated()) {  
      val uzPlayback = UZPlayback(linkPlay = link)  
      uzVideoView.play(uzPlayback)  
    }


Don't forget to add in activity life cycle event:

    public override fun onDestroy() {  
      uzVideoView.onDestroyView()  
      super.onDestroy()  
    }  
      
    public override fun onResume() {  
      super.onResume()  
      uzVideoView.onResumeView()  
    }  
      
    public override fun onPause() {  
      super.onPause()  
      uzVideoView.onPauseView()  
    }  
      
    override fun onBackPressed() {  
      if (!uzVideoView.onBackPressed()) {  
      super.onBackPressed()  
     }



## How to customize your skin?

Only 3 steps, you can customize everything about player skin.

**Step 1:**  Create layout  _**uzplayer_skin_custom.xml**_  like  [THIS](https://github.com/uizaio/snake.sdk.android-player/blob/master/sampleplayer/src/main/res/layout/uzplayer_skin_custom.xml):

Please note  _`app:controller_layout_id="@layout/layout_controller_uz_custom`_

**Step 2:**  Create layout  _**layout_controller_uz_custom.xml**_  like  [THIS](https://github.com/uizaio/snake.sdk.android-player/blob/master/sampleplayer/src/main/res/layout/layout_controller_uz_custom.xml):

-   In this xml file, you can edit anything you like: position, color, drawable resouces...
-   You can add more view (TextView, Button, ImageView...).
-   You can remove any component which you dont like.
-   Please note: Don't change any view  `id`s if you are using it.

**Step 3:**  On class UZApplication:

    UZPlayer.init(R.layout.uzplayer_skin_custom)





That's enough!

But if you wanna change the player's skin when the player is playing, please you this function:

    uzVideoView.changeSkin(skinId)


##  (PIP)

From  `Android Nougat`  (Android SDK >= 24) Google supported  `PIP`. To implement, in  `AndroidManifest.xml`  add  `android:supportsPictureInPicture="true"`  inside  `Your Activity`  and review  [`PIPPlayerActivity`](https://github.com/uizaio/snake.sdk.android-player/blob/master/sampleplayer/src/main/java/com/uiza/sampleplayer/ui/playerpip/PlayerPipActivity.kt).


## For contributors

Uiza Checkstyle configuration is based on the Google coding conventions from Google Java Style that can be found at  [here](https://google.github.io/styleguide/javaguide.html).

## Supported devices

Support all devices which have  _**Android 5.0 (API level 21) above.**_  For a given use case, we aim to support UizaSDK on all Android devices that satisfy the minimum version requirement.

**Note:**  Some Android emulators do not properly implement components of Android’s media stack, and as a result do not support UizaSDK. This is an issue with the emulator, not with UizaSDK. Android’s official emulator (“Virtual Devices” in Android Studio) supports UizaSDK provided the system image has an API level of at least 23. System images with earlier API levels do not support UizaSDK. The level of support provided by third party emulators varies. Issues running UizaSDK on third party emulators should be reported to the developer of the emulator rather than to the UizaSDK team. Where possible, we recommend testing media applications on physical devices rather than emulators.

## Error message

Check this  [class](https://github.com/uizaio/snake.sdk.android-player/blob/master/uzplayer/src/main/java/com/uiza/sdk/exceptions/ErrorConstant.kt)  you can know error code and error message when use UZPlayer.

## Support

If you've found an error in this sample, please file an  [issue](https://github.com/uizaio/snake.sdk.android-player/issues)

Patches are encouraged, and may be submitted by forking this project and submitting a pull request through GitHub. Please feel free to contact me anytime:  [developer@uiza.io](mailto:developer@uiza.io)  for more details.

Address:  _33 Ubi Avenue 3 #08- 13, Vertex Tower B, Singapore 408868_  Email:  _[developer@uiza.io](mailto:developer@uiza.io)_  Website:  _[uiza.io](https://uiza.io/)_

## [](https://github.com/uizaio/uiza-android-player-sdk/blob/master/README.md#license)License

UizaSDK is released under the BSD license. See  [LICENSE](https://github.com/uizaio/uiza-android-player-sdk/blob/master/LICENSE)  for details.