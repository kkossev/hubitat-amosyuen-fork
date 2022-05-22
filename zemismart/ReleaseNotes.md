In the past several weeks, there were some major additions and improvements to this Zemismart Zigbee driver. These changes are available in AmosYuen new development branch: https://raw.githubusercontent.com/amosyuen/hubitat-zemismart-zigbee/development/Zemismart%20Zigbee%20Blind.groovy

The improvements in version 3.2.0 should have effect on the following models (Manufacturers) : _TZE200_rddyvrci, _TZE200_fzo2pocs, _TZE200_wmcdj3aq, _TZE200_cowvfni3, _TYST11_cowvfni3, _TZE200_nueqqe6k, _TZE200_zah67ekd, _TZE200_xuzcvlku, _TZE200_gubdgai2, _TZE200_5sbebbzs, _TZE200_nogaemzt, _TZE200_xaabybja, _TZE200_yenbr4om, _TZE200_zpzndjez, _TZE200_zuz7f94z, _TZE200_rmymn92d


A brief summary of the changes in version 3.2.0 compared to version 3.0.0 (June 2021) :
* Added more than 20 new Tuya (Zemismart) Zigbee blinds/motors manufacturers fingerprints. 
* Depending on the particular motor 'Manufactuer', the driver will:
  * use automatically the correct commands for Open/Close/Stop (some newer models had the Open, Stop and Close commands swapped in various combinations)
  * report correctly the current and the final positions;
  * show correctly 'closed' as position 0% and 'open' as position 100% 
  * show correctly the final 'Open', 'Closed' or 'Partially Open' states for these new models that do not send the final state and stuck on 'opening' or 'closing'.
* Some models repot the destination and the current position (while moving) in one and the same way, which resulted in 'jumps' of the position reporting.
* Some models do not accept 'Open' and 'Close' commands, these are now substituted with SetPosition 100% / 0% commands.
* Added capability 'Switch' and 'Switch Level'- needed for Google Home full compatibility. Commands 'on', 'off' and setLevel() can be now used for opening and closing the blind/curtains.
* Added capability 'Battery' (unfortunately, the battery reporting is implemented by Zemismart for just a few models).
* Added Info log option (default value is 'on')
* Improved debug and trace logs (more information useful for troubleshooting)
* Added 'Show Advanced Options' preference(default value is 'off'). When switched 'on', the Preferences section will allow modifying of some new 'advanced' parameters, 
which normally should have their default settings already automatically configured correctly, depending on the particular model/Manufacturer :
  * 'Invert position reporting' - turn the option on if your model reports the current position inverted (closed should be always reported as 0%, open should be always reported as 100%)
  * 'Ignore the first Position report' - turn the option on if the position reporting jumps between the final and the current position.
  * 'Substitute Open/Close commands with SetPosition' - turn the option on if Open and Close commands do not work for your model when configured in 'lift' mode
  * 'Position report timeout, ms' - increase the timeout if the motor reports Open or Close while still in motion.
  * 'Enable trace logging' and 'Log unexpected messages' - the existing detailed debug logging options are now moved to the 'Advanced' section. Normally these should
be left disabled.
* Configure button now loads the default settings for the device, depending on the 'Manufacturer' value (similar to the default values loaded when the motor is first paired to HE hub)
* Some other minor improvements and bugs fixes.

As there were a lot of changes in the driver code, I would like to ask these of us who use the old 'production' version 3.0.0 (available in HPM) and have the time to test the latest version (available in GitHub development branch linked above) to try it out and let me know if there are any remaining issues.

The new version 3.2.X should be backward compatible with all the previous models using this driver. If no major issues reported, the update to the new version will be pushed via Hubitat Package Manager (HPM) in the next week.


