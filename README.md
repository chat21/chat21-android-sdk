Chat21 is the core of the open source live chat platform [Tiledesk.com](http://www.tiledesk.com).

# Chat21 SDK Documentation 
 
## Features

With Chat21 Android SDK you can:

- Send a direct message to a user (one to one message)
- Emoji support
- Attach pictures support
- Create group chat 
- View the messages history
- View the group list 
- The read receipts feature allows your users to see when a message has been sent, delivered and read
- Conversations list view with the last messages sent (like Whatsapp)
- With the Presense Manager you can view when a user is online or offline and the inactivity period
- View the user profile with fullname and email
- Login with email and password (Use firebase email and password authentication method )
- Signup with fullname, email, password and profile picture
- Contacts list view with fulltext search for fullname field

## Sample

### Screenshots
<img src="https://image.ibb.co/i8zfJS/68747470733a2f2f6c68332e676f6f676c6575736572636f6e74656e742e636f6d2f7832497653466674336f7176733532706d44654f316b504841696e5642435732587a737947555730415a4c53644c687175762d5973464e574c5f482d4a677439576f73323d683930302d7277.png" width="250">| <img src="https://image.ibb.co/ndYwsn/68747470733a2f2f6c68332e676f6f676c6575736572636f6e74656e742e636f6d2f346a6a6b4d3476397553447a613234742d41355f6a5a77316a54747542716d4b6e595870723559706e625f5f67706361586b794e62787a5032756b45306e52663475303d683930302d7277.png" width="250"> | <img src="https://image.ibb.co/jih0JS/68747470733a2f2f6c68332e676f6f676c6575736572636f6e74656e742e636f6d2f30564855496f524f566f6d6d744c4e5465454154626c3862776c6144347338734b3847325a764f78314d516a674a703639364f723638304133665551597944364659493d683930302d7277.png" width="250">

<img src="https://image.ibb.co/ce5LJS/68747470733a2f2f6c68332e676f6f676c6575736572636f6e74656e742e636f6d2f435956554639542d6437464d79326430486f48716f633378624c566e63444464336930516556655a5a6a37704d314b62524367795f4967656a636e3654503948585f673d683930302d7277.png" width="250"> | <img src="https://image.ibb.co/hm7d57/68747470733a2f2f6c68332e676f6f676c6575736572636f6e74656e742e636f6d2f6b2d455257784343484f4d7a4f6d6972414f78596a63724a625044684d6773594443562d386c6c6264713030594d72644b637749546e655f31465a574b426b5042413d683930302d7277.png" width="250">

### Google Play Demo
[![get_it](http://evolvex.it/mobyx/images/nav/gplay-blk.png)](https://play.google.com/store/apps/details?id=chat21.android.demo)

### Demo

Demo app source code is available [here](https://github.com/chat21/chat21-android-demo) 

Yo can build your own chat following our [official tutorial](http://www.chat21.org/docs/android/get-started/)

## Pre requisites

Before you begin, you need a few things to set up in your environment:

* a Firebase project correctly configured  
* Chat21 Firebase cloud functions installed. See detailed [instructions](https://github.com/chat21/chat21-cloud-functions)
* google-services.json for you app. See official [documentation](https://developers.google.com/android/guides/google-services-plugin)

## Firebase libs

### /project/build.gradle

First, include the google-services plugin and the Google’s Maven repository to your root-level build.gradle file:

```
buildscript {
    // ...
    dependencies {
        // ...
        classpath 'com.google.gms:google-services:3.1.1'
    }
}

allprojects {
    // ...
    repositories {
        // ...
        google()
    }
}

```

<div style="text-align:right;">
    <a target="_top" href="https://github.com/chat21/chat21-android-demo/blob/development_v2/build.gradle">build.gradle
        <span>
            <img style="vertical-align:middle;color:#0566D6;" src="https://github.com/chat21/chat21-android-sdk/blob/master/resources/ic_open_in_new_white_24px.svg" alt="open">
        </span>
    </a>
</div>


### /project/app/build.gradle
Then, in your module Gradle file (usually the app/build.gradle), add the apply plugin line at the bottom of the file to enable the Gradle plugin:

```
apply plugin: 'com.android.application'
// ...
dependencies {
    // ...
    implementation "com.google.android.gms:play-services:11.8.0"
}
// ... 
apply plugin: 'com.google.gms.google-services'

```

<div style="text-align:right;">
    <a target="_top" href="https://github.com/chat21/chat21-android-demo/blob/master/build.gradle">build.gradle
        <span>
            <img style="vertical-align:middle;color:#0566D6;" src="https://github.com/chat21/chat21-android-sdk/blob/master/resources/ic_open_in_new_white_24px.svg" alt="open">
        </span>
    </a>
</div>

## Install Chat21 libraries
Set:
* minimun SDK at least at ***API 19*** 
* targetSdkVersion at ***API 22*** 

Add the following to your app/build.gradle file:

```
defaultConfig {
// ...
multiDexEnabled true
}
dependencies {
// ...
compile 'com.android.support:multidex:1.0.1'
compile "com.google.android.gms:play-services:11.8.0"
compile 'com.android.support:design:26.1.0'

compile 'org.chat21.android:chat21:1.0.10'
compile 'com.vanniktech:emoji-ios:0.5.1'
}
// ...
configurations.all {
    resolutionStrategy.eachDependency { DependencyResolveDetails details ->
        def requested = details.requested
        if (requested.group == 'com.android.support') {
            if (!requested.name.startsWith("multidex")) {
                details.useVersion '26.1.0'
            }
        }
    }
}
```
<div style="text-align:right;">
    <a target="_top" href="https://github.com/chat21/chat21-android-demo/blob/master/app/build.gradle">build.gradle
        <span>
            <img style="vertical-align:middle;color:#0566D6;" src="https://github.com/chat21/chat21-android-sdk/blob/master/resources/ic_open_in_new_white_24px.svg" alt="open">
        </span>
    </a>
</div>

### Google Play Services plugin

Finally, as described in the [documentation](https://firebase.google.com/docs/android/setup#manually_add_firebase), paste this statement as the last line of the file:

`apply plugin: 'com.google.gms.google-services'`

At the end, you'll download a `google-services.json` file. For more informations refer to the relative [documentation](https://support.google.com/firebase/answer/7015592)


### Application

Create a custom Application class

```
public class AppContext extends Application {

@Override
protected void attachBaseContext(Context base) {
    super.attachBaseContext(base);
           MultiDex.install(this); // add this
    }
}
```

<div style="text-align:right;">
    <a target="_top" href="https://github.com/chat21/chat21-android-demo/blob/master/app/src/main/java/chat21/android/demo/AppContext.java">AppContext.java
        <span>
            <img style="vertical-align:middle;color:#0566D6;" src="https://github.com/chat21/chat21-android-sdk/blob/master/resources/ic_open_in_new_white_24px.svg" alt="open">
        </span>
    </a>
</div>

and add it to the Manifest.xml

```
 <application
             android:name=".AppContext"
             android:icon="@mipmap/ic_launcher"
             android:label="@string/app_name"
             android:theme="@style/AppTheme"
             ...
</application> 

```

<div style="text-align:right;">
    <a target="_top" href="https://github.com/chat21/chat21-android-demo/blob/master/app/src/main/AndroidManifest.xml">AndroidManifest.xml
        <span>
            <img style="vertical-align:middle;color:#0566D6;" src="https://github.com/chat21/chat21-android-sdk/blob/master/resources/ic_open_in_new_white_24px.svg" alt="open">
        </span>
    </a>
</div>

### Style

Replace the default parent theme in your styles.xml

```
<style name="AppTheme" parent="Theme.AppCompat.Light.DarkActionBar">

```

to 

```
<style name="AppTheme" parent="Theme.AppCompat.Light.NoActionBar">

```
 
you will obtain something like :

```
  <style name="AppTheme" parent="Theme.AppCompat.Light.NoActionBar">
   <!-- Customize your theme here. -->
   <item name="colorPrimary">@color/colorPrimary</item>
   <item name="colorPrimaryDark">@color/colorPrimaryDark</item>
   <item name="colorAccent">@color/colorAccent</item>
</style> 

```

<div style="text-align:right;">
    <a target="_top" href="https://github.com/chat21/chat21-android-demo/blob/master/app/src/main/res/values/styles.xml">styles.xml
        <span>
            <img style="vertical-align:middle;color:#0566D6;" src="https://github.com/chat21/chat21-android-sdk/blob/master/resources/ic_open_in_new_white_24px.svg" alt="open">
        </span>
    </a>
</div>

### Chat21 SDK initialization

#### ChatManager

The Chat21 SDK provide a ***Chat.Configuration*** object which allows to set some custom behaviour 
and settings for your chat.

To create a new instance of Chat21 SDK you have to create your own configuration (using the
Chat21 SDK Chat.Configuration.Builder) and use it as paramater for the method `Chat.initialize(configuration);` 

```
    // optional
    //enable persistence must be made before any other usage of FirebaseDatabase instance.
    FirebaseDatabase.getInstance().setPersistenceEnabled(true);

    // mandatory
    // it creates the chat configurations
    ChatManager.Configuration mChatConfiguration =
            new ChatManager.Configuration.Builder(<APP_ID>)
                    .firebaseUrl(<FIREBASE_DATABASE_URL>)
                    .storageBucket(<STORAGE_BUCKET>)
                    .build();
            
    ChatManager.start(<CONTEXT>, mChatConfiguration, <LOGGED_USER>);

```

Replace:

- `<APP_ID>` with your appId;
- `<FIREBASE_URL>` with your Firebae Database URL of your Firebase App;
- `<STORAGE_BUCKET>` with your Firebae Storage Bucket URL of your Firebase App;
- `<CONTEXT>` with your Context;
- `<LOGGED_USER>` with your current logged user, assuming it is an instance of IChatUser

<div style="text-align:right;">
    <a target="_top" href="https://github.com/chat21/chat21-android-demo/blob/development_v2/app/src/main/java/chat21/android/demo/AppContext.java">AppContext.java
        <span>
            <img style="vertical-align:middle;color:#0566D6;" src="https://github.com/chat21/chat21-android-sdk/blob/master/resources/ic_open_in_new_white_24px.svg" alt="open">
        </span>
    </a>
</div>

#### ChatUI

ChatUI allows you to quickly connect common UI elements to Chat21 SDK APIs.

ChatUI lets you start your chat with both an activity and a inside a fragment.

Initialize the ChatUI component with the following instruction

```
ChatUI.getInstance().setContext(this);
 ```

##### Launch with an activity

It starts a new activity that contains the list of conversations.

```
 ChatUI.getInstance().showConversationsListActivity();

```

<div style="text-align:right;">
    <a target="_top" href="https://github.com/chat21/chat21-android-demo/blob/master/app/src/main/java/chat21/android/demo/HomeFragment.java">Example.java
        <span>
            <img style="vertical-align:middle;color:#0566D6;" src="https://github.com/chat21/chat21-android-sdk/blob/master/resources/ic_open_in_new_white_24px.svg" alt="open">
        </span>
    </a>
</div>

##### Launch with a fragment

You have to create a fragment with a container inside.

The chat will start inside this container where the list of conversations is shown.


```
<android.support.design.widget.CoordinatorLayout 
    android:layout_width="match_parent"
    android:layout_height="match_parent">
    
    <FrameLayout
        android:id="@+id/container"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

</android.support.design.widget.CoordinatorLayout>
```

Now you can show your chat with the following method:

```
  ChatUI.getInstance().openConversationsListFragment(getChildFragmentManager(), R.id.container);

```

<div style="text-align:right;">
    <a target="_top" href="https://github.com/chat21/chat21-android-demo/blob/master/app/src/main/java/chat21/android/demo/ChatFragment.java">ChatFragment.java
        <span>
            <img style="vertical-align:middle;color:#0566D6;" src="https://github.com/chat21/chat21-android-sdk/blob/master/resources/ic_open_in_new_white_24px.svg" alt="open">
        </span>
    </a>
</div>

### Common Issues

- Conflicts within `com.android.support`

    Error:
    ```
    * What went wrong:
    Execution failed for task ':app:processDebugResources'.
    > Failed to execute aapt
    ```
    Solution:
    Copy this block at the bottom of your file **/project/app/build.gradle**
    ```
    configurations.all {
        resolutionStrategy.eachDependency { DependencyResolveDetails details ->
            def requested = details.requested
            if (requested.group == 'com.android.support') {
                if (!requested.name.startsWith("multidex")) {
                    details.useVersion '26.1.0'
                }
            }
        }
    }
    ```

- MultiDex

    Error:
    ```
    Error:Execution failed for task ':app:transformDexArchiveWithExternalLibsDexMergerForDebug'.
    > java.lang.RuntimeException: java.lang.RuntimeException: com.android.builder.dexing.DexArchiveMergerException: Unable to merge dex
    ```
    Solution:
    Make sure you have added `multiDexEnabled true ` inside of **/project/app/build.gradle**

    Copy this block inside of your custom Application class
     ```
      @Override
      protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
      }
     ```

- Theme

    Error:
    ```
        RuntimeException: Unable to start activity ComponentInfo{my.sample.package.myapplication/chat21.android.ui.conversations.activities.ConversationListActivity}: java.lang.IllegalStateException:
        This Activity already has an action bar supplied by the window decor. Do not request Window.FEATURE_SUPPORT_ACTION_BAR and set windowActionBar to false in your theme to use a Toolbar instead.
    ```

    Solution:
    See the Style section

- Application name exceptions:

    Error:

    ```
        /android/MyApp/app/src/main/AndroidManifest.xml:30:9 Error:
        Attribute application@label value=(@string/application_name) from AndroidManifest.xml:30:9
        is also present at {Library Name} value=(@string/app_name)
        Suggestion: add 'tools:replace="android:label"' to <application> element at AndroidManifest.xml:26:5 to override
    ```

    Solution:
    Add the ` tools:replace="android:label"` to override the Chat21 SDK app name and default icon:

        ```
        <application
            android:name=".AppContext"
            android:allowBackup="true"
            android:icon="@mipmap/ic_launcher"
            android:label="@string/app_name"
            android:roundIcon="@mipmap/ic_launcher_round"
            android:supportsRtl="true"
            android:theme="@style/AppTheme"
            tools:replace="android:label, android:icon"> <!-- add this -->

            . . .

        </application>
        ```

<div style="text-align:right;">
    <a target="_top" href="https://github.com/chat21/chat21-android-demo/blob/master/app/src/main/AndroidManifest.xml">AndroidManifest.xml
        <span>
            <img style="vertical-align:middle;color:#0566D6;" src="https://github.com/chat21/chat21-android-sdk/blob/master/resources/ic_open_in_new_white_24px.svg" alt="open">
        </span>
    </a>
</div>


## Deploy JCenter

Follow this [guide](https://github.com/chat21/chat21-android-sdk/wiki/JCenter-Deploy) to deploy your own library to JCenter
