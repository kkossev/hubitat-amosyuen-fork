Summary of the changes in version 3.1.7 compared to version 3.0.0 (June 2021) :
* added more than 20 other manufacturers fingerprints. Depending on the partucular motor 'Manufactuer', the driver should:
  * use automatically the correct commands for Open/Close/Stop (some newer models had the Open, Stop and Close commands swapped in various combinations)
  * report correctly the current and the final positions;
  * show correctly the final 'Open', 'Closed' or 'Partially Open' states for these new models that do not send the final state and stuck on 'opening' or 'closing'.
  * some newer models repot the destination and the current position (while moving) in one and the same way, which resulted in 'jumps' of the position reporting.
  * some models do not accept 'Open' and 'Close' commands, these are substituted with SetPosition 100% / 0% commands.
* added capability 'Switch' and 'Switch Level'- needed for Google Home full compatibility. Commands 'on', 'off' and setLevel() can be now used for opending and closing the blinds.
* added capability 'Battery' - unfortunately, the battery reporting is implemented by Zemismart for few models only.
* added Info log option (default value is 'on')
* Improved debug and trace logs (more information useful for troubleshooting)
* added 'Show Advanced Options' (default value is 'off'). When switched 'on', the Preferences section will allow modifying of some new 'advanced' parameters, 
which normally should have their default settings already automatically configured correctly, depending on the particular model/Manufacturer :
  * 'Invert position reporting' - some newer models have the position reported inverted (closed should be always reported as 0%, open should be always reported as 100%)
  * 'Ignore the first Position report'
  * 'Substitute Open/Close commands with SetPosition'
  * 'Position report timeout, ms'
  * 'Enable trace logging' and 'Log unexpected messages' - the existing detailed debug logging options are now moved to the 'Advanced' section. Normally these should
be left disabled.
* Configure button now loads the default settings for the device, depending on the 'Manufacturer' value (similar to the default values loaded when the motor is first paired to HE hub)
* Some other minor improvements and bugs fixes.
* 
