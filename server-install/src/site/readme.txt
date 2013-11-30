
===================== NetHomeManager =========================

This is the NetHomeManager home automation server.

NetHomeManager is a home control software designed to control lamps and other
appliances and to gather information like temperatures and weather data and
present that and also use it for home automation.

This is a demo configuration with a number of lamps and thermometers which
gives you an idea of how the server works. If you have no hardware devices
attached which can actually control the lamps, you will not be able to see
any real world results when you operate the lamp-items. See http://wiki.nethome.nu
for compatible hardware interfaces.

Feel free to play around with the server and create new items and get a feel for
how the server works. Once you have attached any of the hardware interfaces, you
can start using this server to control your home.

HomeManager_32.bat - Starts the server in the Windows environment with 32 bits JVM
HomeManager_64.bat - Starts the server in the Windows environment with 64 bits JVM
HomeManager_32.sh  - Starts the server in the Linux environment with 32 bits JVM
HomeManager_64.sh  - Starts the server in the Linux environment with 64 bits JVM
HomeManager_macosx_carbon.sh - Starts the server in an older MAC OSX Environment
HomeManager_macosx_cocoa.sh - Starts the server in an 32 bit MAC OSX Environment
HomeManager_macosx_cocoa_64.sh - Starts the server in an 64 bit MAC OSX Environment
HomeManager_raspian.sh - Starts the server on a RaspberryPi

When the server has started you can reach the web management interface on the address:
  http://localhost:8020/home

For more information please refer to http://www.nethome.nu
