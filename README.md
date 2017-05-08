# WhereAmI

App description:

Android app for showing current location on the Google Map.
Application also shows GPS coordinates and user's address and allows user to put markers on a map.


Description of program solutions & problems:

*User interface:

User interface consists of 2 TextViews and 1 Fragment.
One TextView is for GPS coordinates of user's current position.(Latitude and Longitude)
Another TextView is for user's address (Street address, city and country).
Fragment is used for working with map.


*Working with maps

Google API for Maps is used for working with maps. 
It was first necessary to register for developer key which is than defined in app's manifest.
And also before using maps, it was necessary to add external play-services library.
In the layout file a fragment is set which is later used for representing a map, and it is special type of fragment - the one in a gms.map namespace.
In the app's MainActivity a fragment is retrieved and than a listener for GoogleMap object is set.
Map retrieving is asynchronous and a callback method onMapReady is used through interface which Activity implements.
By clicking on a map a marker is created with a same icon as a app's icon. 

Source: https://loomen.carnet.hr/pluginfile.php/787643/mod_resource/content/2/LV5%20-%20predlo%C5%BEak%20%282017%29.pdf


*Working with GPS

To get GPS coordinates of a device, it is first neccesary to ask permission for accessing location, inside manifest file.
After that, using an object of the LocationManager class the location can be sought.
To start and to stop listening on a location appearing a LocationListener object is set and removed. 
During app's working time (while the MainActivity is active) GPS coordinates are shown on appropriate TextView.
Tracking stops in onPause method.

Source: https://loomen.carnet.hr/pluginfile.php/787643/mod_resource/content/2/LV5%20-%20predlo%C5%BEak%20%282017%29.pdf


*Working with Geocoder 

Geocoding is done using a Geocoder class. When using Geocoder class it is necessary to check if the class is defined in a system. 
If it is, than it is possible to instantiate object of Geocoder class and forward appropriate location to that object.
Transforming GPS coordinates to location is done on a remote server and the permission for accessing Internet is required.
Decoding GPS coordinates for a result returns a list of objects of Address class, and every object contains information about that location such as: country, city, street address...

Source: https://loomen.carnet.hr/pluginfile.php/787643/mod_resource/content/2/LV5%20-%20predlo%C5%BEak%20%282017%29.pdf


*Working with Camera

Before starting development on your application with the Camera API, you should make sure your manifest has the appropriate declarations to allow use of camera hardware and other related features.
The Android way of delegating actions to other applications is to invoke an Intent that describes what you want done. 
This process involves three pieces: The Intent itself, a call to start the external Activity, and some code to handle the image data when focus returns to your activity.
The Android Camera application saves a full-size photo if you give it a file to save into. You must provide a fully qualified file name where the camera app should save the photo.
Generally, any photos that the user captures with the device camera should be saved on the device in the public external storage so they are accessible by all apps. 
The proper directory for shared photos is provided by getExternalStoragePublicDirectory(), with the DIRECTORY_PICTURES argument. 
Because the directory provided by this method is shared among all apps, reading and writing to it requires the READ_EXTERNAL_STORAGE and WRITE_EXTERNAL_STORAGE permissions, respectively. 

Source: https://developer.android.com/training/camera/photobasics.html


*Working with Notifications

Notifications are created and managed using NotificationManager class.
That class allows showing a notification, and for its creating the Notification.Builder class is used.
While creating, some basic information about notification are defined. (Such as: text, title, icon, vibration sample,...).
Also, a PendingIntent concept is used for triggering at a later time.
It can be triggered by an app which created that intent. 

Source : https://loomen.carnet.hr/pluginfile.php/787643/mod_resource/content/2/LV5%20-%20predlo%C5%BEak%20%282017%29.pdf