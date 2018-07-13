# Running this

1. Download `secrets.xml`, which goes in `frontend/app/src/main/res/values`.
   This contains client IDs for Google login services.
2. Download `keystore.jks`, which goes in `frontend/app`. Google login services
   check that the signing of the app matches what I told them it should be, so
   this is also important.
3. Make sure that the server is running! The file `BackendClient.java` contains
   a defintion for the server address. It should be valid. If not, you will have
   to fix this or run your own server (and point BackendClient to your own
   address).
