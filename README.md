[![](https://jitpack.io/v/fabiosassu/StackExpandableView.svg)](https://jitpack.io/#fabiosassu/StackExpandableView)

# StackExpandableView
A custom view that resembles the iOS notification group behavior

![device-2021-03-07-190110](https://user-images.githubusercontent.com/4465945/110250723-1bdc6400-7f7d-11eb-9405-6c341c2a350b.gif)

### Requirements
* A project configured with the AndroidX
* SDK 16 and and higher

### Install
Download via **Gradle**:

Add this to the **project `build.gradle`** file:
```gradle
allprojects {
    repositories {
        ...
        maven { url "https://jitpack.io" }
    }
}
```

And then add the dependency to the **module `build.gradle`** file:
```gradle
dependencies {
        implementation 'com.github.fabiosassu:StackExpandableView:latest_version'
}
```

Where the `latest_version` is the value from `Download` badge.

### Usage
#### Simple usage
All you need to do is to define a `StackExpandableView` item inside your layout:

```xml
 <it.fabiosassu.stackexpandableview.StackExpandableView
            android:id="@+id/horizontalStack"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            app:orientation="horizontal"
            app:animationDuration="1000"
            app:shownElements="5"
            app:parallaxOffset="8dp"/>
```

You can customize some aspects of this view:

- the `orientation` parameter is be used to specify the direction of the expansion It can be either `vertical` or `horizontal`. By default is `vertical`.
- the `animationDuration` parameter is used to define the duration of the animation in milliseconds. By default, is 300 milliseconds.
- the `shownElements` parameter is used to define how many elements we want to be shown. By default, is 3 elements.
- the `parallaxOffset` parameter is used to define the spacing betwen the underlying objects in dp. By default, is 8dp.

At runtime, you can set/add/remove a list of Views using the related methods

```kotlin
// this is used to set the list of Views
binding.horizontalStack.setWidgets(listOf<LinearLayout>())
// this is used to add a View to the existing one
binding.verticalStack.addWidget(LinearLayout(context))
// this is used to remove a View from the stack
binding.verticalStack.removeWidget(LinearLayout(context))
```

It is important that all the views that are added to the `StackExpandableView` have an id set (fo example using `ViewCompat.generateViewId()`), because it is used to order the views internally and also to look for the view when `removeWidget()` is called.
