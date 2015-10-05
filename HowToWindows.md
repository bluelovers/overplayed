# System Requirements #

  * **Windows 7** or **8**, **32-bit** or **64-bit** has been tested, but the server should work on any version of Windows that PPJoy Supports

  * **PPJoy** installed with at least **one virtual joystick** created.

# PPJoy installation (only has to be done once) #

PPJoy is a virtual joystick that the server uses to send game controller input to games and applications.

  * PPJoy can be downloaded at:
    * **32-bit**: http://ppjoy.bossstation.dnsalias.org/
    * **64-bit**: https://code.google.com/p/steel-batallion-64/downloads/detail?name=ppjoysetup-0-8-4-6.exe
      * **_Note 64-bit Windows Vista, 7, or 8 only (except Ultimate):_ Windows will be put in test mode upon installation.**
        * It does not affect Windows except for a logo at the bottom right corner of the desktop. A helpful article explaining this can be found here: http://www.jim-melton.com/tag/ppjoy-windows-7-64-bit/ And the Microsoft knowledge base for reverting it can be found at: http://support.microsoft.com/kb/2509241 (but this will disable PPJoy)

  * After installation:
    * Run **Configure Game Controllers**
    * Click **Add...**
    * Click **Add**
    * Click **Done**

# Running overplayed server #
  * Download and run the overplayed server: https://code.google.com/p/overplayed/downloads/
    * If you get **msvcp110.dll missing** error, install the Visual C++ Redistributable for Visual Studio 2012 (x86): http://www.microsoft.com/en-us/download/details.aspx?id=30679

# Running overplayed #
  * The overplayed client for Android can be downloaded at Google Play: https://play.google.com/store/apps/details?id=com.gphrost.Overplayed
  * Enter the server's IP address or hostname and press **Start**
  * To enter text or to touch behind a control press the **Hide** button. Press it again to bring the controls back.
  * Press **Menu** for options and to quit.

# Advanced #
  * To change what port the server uses, use the port number as an argument when running the server, i.e. overplayed 12345.
  * overplayed only uses UDP, in case you want to port forward.
  * x360ce (xbox 360 controller emulator: https://code.google.com/p/x360ce/) can be used for maximum game compatibility. A configuration file I made can be found here: https://code.google.com/p/overplayed/downloads/detail?name=x360ce.ini