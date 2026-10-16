package com.akshar.dyslexiareader.util.voice

/**
 * Voice commands recognized by Akshar across 5 Indian languages + English.
 */
enum class VoiceCommand(val defaultDescription: String) {
    NEXT_LINE("Move to next line"),
    PREVIOUS_LINE("Move to previous line"),
    READ("Start reading aloud"),
    STOP("Stop reading aloud"),
    BIGGER_TEXT("Increase text size"),
    SMALLER_TEXT("Decrease text size"),
    SCAN_PAGE("Open camera to scan a page"),
    OPEN_LIBRARY("Open library lessons"),
    UNKNOWN("Command not recognized")
}
