# Firebase WebRTC Android App -

## Working of the app -
> The application utilizes Firebase for seamless sign-up and login functionality. Upon launching the app, users are prompted to sign in. If the user already exists, they are logged in automatically; otherwise they are registered as new user, and they are logged in automatically.
>
> After signing in, the user is presented with a list of app users, categorized as ONLINE or OFFLINE. Users can initiate calls and send call requests to those who are marked as 'ONLINE'. The recipient of the call request receives a notification and can manually accept the call. Upon acceptance, both users are directed to the VideoCall Activity.
>
> Once in the VideoCall Activity, users can interact with each other via video call, facilitated by the established connection.


## Implemented the following things inside the app - 

> -  Created a peer-to-peer video conferencing feature using WebRTC in Kotlin.
> -  Used Firebase as a server to handle requests.
> -  Add features for muting/unmuting the microphone.
> -  Added feature of enabling/disabling the camera.
> -  Added feature of switching the camera from front to back and vice-versa.
> -   User can see the list of all the users.
> -   Used dependency injection along with repository pattern for writing clean Kotlin code.

## Things unable to implement - 

> - Minimum 3 Peers Requirement: Despite extensive research, I couldn't find adequate documentation or detailed instructions on integrating the MediaSoup Library with Native Android for creating a meeting app with more than 2 users. Therefore, I was unable to implement the minimum 3 peers requirement due to the lack of resources and guidance.
>
> - Jetpack Compose: Due to my limited familiarity with Android Jetpack Compose and lack of prior experience, I decided against utilizing it for this project. As a result, the implementation of Jetpack Compose was not pursued.
>
> - Video Call Camera Functionality: Although I attempted to troubleshoot the issue extensively, the camera functionality for video calls did not work as intended. Despite correctly achieving the target user on both ends, I encountered a persistent bug preventing the transmission of the camera stream to the other user. Despite efforts, the root cause of this issue remains elusive.






