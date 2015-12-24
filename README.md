# MIDI-Player-App
A MIDI Player Android app. Whether this will amount to anything is a question best left to history.

##Notes
This is very likely going to end up being not just a MIDI player, because Android already has the ability to play MIDI files, and there doesn't seem to be any need to make another MIDI player. ~~So in the interest of making this project worthwhile, I might end up incorporating the MIDI-to-Image/Image-to-MIDI thing in it.~~

##Updates and/or current status
####Date: 12/23/2015
* MIDI player in place.
* Program change implemented, but in a very specific way:
  * A program change affects all tracks.
  * A program change affects all tracks from the beginning, so the user should have no expectations of being pleasantly surprised to hear the familiar old program the second time around.
  * The list of programs is provided as a constant, at least until I figure out how to access a phone's default soundbank, if any. Until then, if the user changes programs and does not perceive a marked difference in playback, it means that that particular program is probably not supported by Android. Or that there's some error in the code, which is more likely, actually.
 * If you recall (or if the sentence above that has been prominently stricken through caught your eye), I initially talked about incorporating the MIDI-to-Image/Image-to-MIDI thingo in the project. Now I see that changing programs is probably novel enough by itself to make this whole business worthwhile, so the MIDI-to-Image/Image-to-MIDI thing is no longer of paramount importance.

##External Libraries
* LeffelMania's [MIDI Library](https://github.com/LeffelMania/android-midi-lib), because Android stopped supporting ```javax.sound.midi``` some years ago.

*Icon made by [Freepik](http://www.flaticon.com/authors/freepik) from [www.flaticon.com](http://www.flaticon.com/)*, I am obligated to mention.
