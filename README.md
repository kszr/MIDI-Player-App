# MIDI-Player-App
A MIDI Player app for Android. Android already has the ability to play MIDI files, but this app lets you change program/instrument, so it has that going for it, I suppose.

###A note on changing programs
Program changing is implemented in a very specific way: Changing programs changes the program in all the tracks from the beginning to the one selected. There really isn't any reasonable way to change programs on individual tracks. Which is to say that any attempt to implement such a thing would be unreasonable, and would no doubt occasion great consternation to the unassuming user who only wants to change all the tracks of an ochestral piece to - say - xylophone, and may not have the time to go about doing so for all 112 tracks individually.

##TODO
* Make the program list searchable, since scrolling through a list of 128 instruments is probably not the most fun thing in the world, especially when time is of the essence.
* Add something in the Settings menu option to make the app look passably professional.
* Implement file saving. Possibly sharing by email.

##External Libraries
* LeffelMania's [MIDI Library](https://github.com/LeffelMania/android-midi-lib), because Android stopped supporting ```javax.sound.midi``` some years ago.

*Icon made by [Freepik](http://www.flaticon.com/authors/freepik) from [www.flaticon.com](http://www.flaticon.com/)*, I am legally obligated to mention.
