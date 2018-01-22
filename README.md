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

## Let's see it in action

See the sample app [source code](https://github.com/chat21/chat21-android-demo) 

<img src="https://github.com/chat21/chat21-android-sdk/blob/master/resources/screen1.png" width="250"> | <img src="https://github.com/chat21/chat21-android-sdk/blob/master/resources/screen2.png" width="250"> | <img src="https://github.com/chat21/chat21-android-sdk/blob/master/resources/screen3.png" width="250">

<img src="https://github.com/chat21/chat21-android-sdk/blob/master/resources/screen4.png" width="250"> | <img src="https://github.com/chat21/chat21-android-sdk/blob/master/resources/screen5.png" width="250">


## Pre requisites
It is assumed that you are using an existing Firebase project or that you have created a new one 
on the Firebase console.
if it was not done, follow the [Firebase Documentation](https://firebase.google.com/docs/android/setup) to create a new app on the Firebase console

Set the firebase database permissions rules to :

```

{
    "rules": {
      "apps": {
        "$app_id": {
          "contacts": {
            ".read": "auth != null",
            "$uid":{
                ".write": "$uid === auth.uid"
            }
          },
          "groups":{
            ".read": false,
            ".write": "auth != null",
            "$group_id":{
                ".validate":"newData.hasChildren(['name','members', 'owner'])"
            }
          },
          "presence": {
            ".read": "auth != null",
            "$uid":{
                ".write": "$uid === auth.uid"
            }
        },
          "users":{
            "$uid":{
                ".read": "$uid === auth.uid",
                ".write": "$uid === auth.uid",
                "messages" : {
                  "$message_id":{
                    ".validate": "(!newData.hasChildren(['status']) || ( newData.hasChildren(['status']) && newData.child('status').isNumber() && newData.child('status').val()==200) )"
                  }
                }
            }
          }
        }
      }
    }
  }
```

## Add Chat21 SDK dependencies

### Gradle Scripts

Download the Chat21 SDK in your work directory. 

#### /project/build.gradle

add Google Play Service classpath and Google dependencies and sync.


```
buildscript {

    repositories {
        jcenter()
        google()
    }

    dependencies {
        classpath 'com.android.tools.build:gradle:3.0.1'
        classpath 'com.google.gms:google-services:3.1.1'
    }
}

allprojects {
    repositories {
        jcenter()
        google()

        maven {
            url 'https://maven.fabric.io/public'
        }
    }
}

. . . 

```

<div style="text-align:right;">
    <a target="_top" href="https://github.com/chat21/chat21-android-demo/blob/master/build.gradle">build.gradle
        <span>
            <img style="vertical-align:middle;color:#0566D6;" src="https://github.com/chat21/chat21-android-sdk/blob/master/resources/ic_open_in_new_white_24px.svg" alt="open">
        </span>
    </a>
</div>


#### /project/settings.gradle

Open your settings.gradle, paste these two lines and sync

```
include ':chat'
project(':chat').projectDir = new File('<CHAT_LIBRARY_FOLDER_PATH>/chat21-android-sdk/chat/')
```
replace `<CHAT_LIBRARY_FOLDER_PATH>` with your Chat21 SDK folder path.

<div style="text-align:right">
    <a target="_top" href="https://github.com/chat21/chat21-android-demo/blob/master/settings.gradle">settings.gradle
        <span>
            <img style="vertical-align:middle;color:#0566D6;" src="https://github.com/chat21/android-sdk/blob/0.10.x/resources/ic_open_in_new_white_24px.svg" alt="open">
        </span>
    </a>
</div>


#### /project/app/build.gradle

##### Basic configurations
Set yout minimun SDK at least at ***API 19*** 

Your android should be like this: 

```
android {
   . . . 
    
    defaultConfig {
    
        . . . 
        
        minSdkVersion 19
        targetSdkVersion 22
        
         . . .
        
        // multidex support
        multiDexEnabled true
    }

    
    packagingOptions {
        exclude 'META-INF/LICENSE'
        exclude 'META-INF/NOTICE'
        exclude 'META-INF/license.txt'
        exclude 'META-INF/notice.txt'
        exclude 'META-INF/DEPENDENCIES'
    }
    
    dependencies {
    
        // multidex
        implementation 'com.android.support:multidex:1.0.1'

        // google play service
        implementation 'com.google.android.gms:play-services:11.8.0'
   
        // chat
        implementation project(':chat')
        
        . . . 
    }
}
```
<div style="text-align:right">
    <a target="_top" href="https://github.com/chat21/chat21-android-demo/blob/master/app/build.gradle">/app/build.gradle
        <span>
            <img style="vertical-align:middle;color:#0566D6;" src="https://github.com/chat21/android-sdk/blob/0.10.x/resources/ic_open_in_new_white_24px.svg" alt="open">
        </span>
    </a>
</div>

##### Google Play Services plugin

Finally, as described in the [Firebase documentation](https://firebase.google.com/docs/android/setup#manually_add_firebase), paste this statement as the last line of the file:

`apply plugin: 'com.google.gms.google-services'`

At the end, you'll download a `google-services.json` file. For more informations refer to the relative [Firebase documentation](https://support.google.com/firebase/answer/7015592)


### AndroidManifest.xml

Let's set up  the AndroidManifest.xml

#### Permissions

Runtime permissions are currently not supported.

Then you must declare the permissions in the manifest and set the targetSdkVersion at ***API 22*** 

The Chat21 SDK needs the following permissions: 


    ```
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
    ```
    
<div style="text-align:right">
    <a target="_top" href="https://github.com/chat21/chat21-android-demo/blob/master/app/src/main/AndroidManifest.xml">AndroidManifest.xml
        <span>
            <img style="vertical-align:middle;color:#0566D6;" src="https://github.com/chat21/android-sdk/blob/0.10.x/resources/ic_open_in_new_white_24px.svg" alt="open">
        </span>
    </a>
</div>

#### Application

In your `<application></application>` :

- define your custom application class:
    
    ***this is a mandatory step***. You have to create your own application class in which we'll 
     initialize and add extra customization for the Chat21 SDK

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
            new ChatManager.Configuration.Builder(<APP_ID>).build();
    ChatManager.start(<CONTEXT>, mChatConfiguration, <LOGGED_USER>);

    // init the contacts list
    ChatManager.getInstance().initContactsSyncronizer();

```

Replace:

- `<APP_ID>` with your appId;
- `<CONTEXT>` with your Context;
- `<LOGGED_USER>` with your current logged user, assuming it is an instance of IChatUser

<div style="text-align:right">
    <a target="_top" href="https://github.com/chat21/chat21-android-demo/blob/master/app/src/main/java/it/chat21/android/demo/AppContext.java">AppContext.java
       <span>
           <img style="vertical-align:middle;color:#0566D6;" src="https://github.com/chat21/android-sdk/blob/0.10.x/resources/ic_open_in_new_white_24px.svg" alt="open">
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

<div style="text-align:right">
    <a target="_top" href="https://github.com/chat21/chat21-android-demo/blob/master/app/src/main/java/it/chat21/android/demo/HomeFragment.java">Example.java
        <span>
            <img style="vertical-align:middle;color:#0566D6;" src="https://github.com/chat21/android-sdk/blob/0.10.x/resources/ic_open_in_new_white_24px.svg" alt="open">
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
  ChatUI.getInstance().showConversationsListFragment(getChildFragmentManager(), R.id.container);

```

<div style="text-align:right">
    <a target="_top" href="https://github.com/chat21/chat21-android-demo/blob/master/app/src/main/java/it/chat21/android/demo/ChatFragment.java">ChatFragment.java
        <span>
            <img style="vertical-align:middle;color:#0566D6;" src="https://github.com/chat21/android-sdk/blob/0.10.x/resources/ic_open_in_new_white_24px.svg" alt="open">
        </span>
    </a>
</div>

### Style.xml

The Chat21 SDk supports most customizable android [Toolbar](https://developer.android.com/training/appbar/setting-up.html) instead of old ActionBar.

In your style.xml change your parent theme from 

```
<style name="AppTheme" parent="Theme.AppCompat.Light.DarkActionBar">

``` 

to 
```
 <style name="AppTheme" parent="Theme.AppCompat.Light.NoActionBar">
 ```

<div style="text-align:right">
    <a target="_top" href="https://github.com/chat21/chat21-android-demo/blob/master/app/src/main/res/values/styles.xml">styles.xml
        <span>
            <img style="vertical-align:middle;color:#0566D6;" src="https://github.com/chat21/android-sdk/blob/0.10.x/resources/ic_open_in_new_white_24px.svg" alt="open">
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
                    details.useVersion '25.3.0'
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
    See the [Style Chapter](#Style.xml)

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

        <div style="text-align:right">
              <a target="_top" href="https://github.com/chat21/chat21-android-demo/blob/master/app/src/main/AndroidManifest.xml">AndroidManifest.xml
                  <span>
                      <img style="vertical-align:middle;color:#0566D6;" src="https://github.com/chat21/android-sdk/blob/0.10.x/resources/ic_open_in_new_white_24px.svg" alt="open">
                  </span>
              </a>
          </div>
