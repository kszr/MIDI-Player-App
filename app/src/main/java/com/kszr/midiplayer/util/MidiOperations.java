package com.kszr.midiplayer.util;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import midi.MidiFile;
import midi.MidiTrack;
import midi.event.MidiEvent;
import midi.event.ProgramChange;
import midi.event.meta.EndOfTrack;

/**
 * Functions that manipulate MidiFiles in specific ways.
 * Created by abhishekchatterjee on 12/25/15.
 */
public final class MidiOperations {
    /**
     * Changes the program of the MidiFile to that specified by program.
     * @param midiFile The MidiFile to modify.
     * @param program The program to change to.
     */
    public static void changeProgram(MidiFile midiFile, int program) {
        ArrayList<MidiTrack> tracks = midiFile.getTracks();
        for (MidiTrack track : tracks) {
            TreeSet<MidiEvent> eventSet = track.getEvents();
            MidiEvent putativeEOT = eventSet.last();

            // Need to remove EndOfTrack event for now, because a track is not
            // mutable otherwise.
            if (putativeEOT.getClass().equals(EndOfTrack.class)) {
                track.removeEvent(eventSet.last());
                eventSet = track.getEvents();
            }
            List<MidiEvent> eventsToRemove = new ArrayList<>();

            // Need to remove any ProgramChange events that might conflict
            // with the new ProgramChange.
            for (MidiEvent event : eventSet)
                if (event.getClass().equals(ProgramChange.class))
                    eventsToRemove.add(event);
            for (MidiEvent event : eventsToRemove)
                track.removeEvent(event);
            track.insertEvent(new ProgramChange(0, 0, program));

            //Adding EndOfTrack.
            track.closeTrack();
        }
        midiFile = new MidiFile(midiFile.getResolution(), tracks);
    }
}
