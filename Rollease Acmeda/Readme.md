# Hubitat Drivers for Rollease Acmeda Hub & Shades

These drivers add support for Rollease Acmeda Shades in Hubitat.

This was tested with the Automate Pulse 2 Hub, but may work with the older generation hub since it uses the same ARC serial protocol via Telnet.

# Installation
1) Make sure your hub has been configured and the shades added via the Automate Pulse App (https://www.rolleaseacmeda.com/us/products/product-detail/automate-pulse-2US)
2) Add both the hub and shade drivers in Hubitat (Under "Driver Code")
3) Create a new virtual device for the hub, with the type "Rollease Acmeda Hub"
4) Add the hub's IP address to the new hub device and save the preferences
5) Click "Configure" withun the hub device to scan for shades
6) A new shade device will be created for each motor/shade that's found.


