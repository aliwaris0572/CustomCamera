[![](https://jitpack.io/v/aliwaris0572/CustomCamera.svg)](https://jitpack.io/#aliwaris0572/CustomCamera)

# CustomCamera
This library helps user to control camera resolution.
Check MainActivity.java for more details on how to use.

# Gradle
Add it in your root build.gradle at the end of repositories

    allprojects {
		    repositories {
			    ...
			    maven { url 'https://jitpack.io' }
		    }
	    }
  
---------------------------------------------------------------

Then, add this in you app level build.gradle

    dependencies {
	            implementation 'com.github.aliwaris0572:CustomCamera:{latest_version}'
	    }
