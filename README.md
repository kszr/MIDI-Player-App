# MIDI-Player-App
A MIDI Player app for Android. Android already has the ability to play MIDI files, but this app lets you change program/instrument, so it has that going for it, I suppose.

##Updates and/or current status
####Date: 12/23/2015
* MIDI player in place.
* Program change implemented, but in a very specific way:
  * A program change affects all tracks.
  * A program change affects all tracks from the beginning, so the user should have no expectations of being pleasantly surprised to hear a familiar old program the second time around.
  * The list of programs is provided as a constant, at least until I figure out how to access a phone's default soundbank, if any. Until then, if the user changes programs and does not perceive a marked difference in playback, it means that that particular program is probably not supported by Android. Or that there's some error in the code, which is more likely, actually.
 * I initially talked about incorporating the MIDI-to-Image/Image-to-MIDI thingo in the project. Now I see that changing programs is probably novel enough in itself to make this whole business worthwhile, so the MIDI-to-Image/Image-to-MIDI thing is no longer of paramount importance.

##TODO
* Make the program list searchable, since scrolling through a list of 127 instruments is probably not the most fun thing in the world, especially when time is of the essence.
* ~~Optimize program changing, if possible.~~
* Add something in the Settings menu option to make the app look passably professional.
* ~~Display current progress of playback, measured against the length of the file.~~

##External Libraries
* LeffelMania's [MIDI Library](https://github.com/LeffelMania/android-midi-lib), because Android stopped supporting ```javax.sound.midi``` some years ago.

*Icon made by [Freepik](http://www.flaticon.com/authors/freepik) from [www.flaticon.com](http://www.flaticon.com/)*, I am legally obligated to mention.
