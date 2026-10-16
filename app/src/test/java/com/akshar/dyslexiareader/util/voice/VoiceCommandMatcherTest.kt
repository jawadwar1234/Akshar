package com.akshar.dyslexiareader.util.voice

import org.junit.Assert.assertEquals
import org.junit.Test

class VoiceCommandMatcherTest {

    @Test
    fun matchCommand_englishCommands_exactAndSynonyms() {
        assertEquals(VoiceCommand.NEXT_LINE, VoiceCommandMatcher.matchCommand("next line"))
        assertEquals(VoiceCommand.NEXT_LINE, VoiceCommandMatcher.matchCommand("go down"))
        assertEquals(VoiceCommand.NEXT_LINE, VoiceCommandMatcher.matchCommand("forward"))

        assertEquals(VoiceCommand.PREVIOUS_LINE, VoiceCommandMatcher.matchCommand("previous line"))
        assertEquals(VoiceCommand.PREVIOUS_LINE, VoiceCommandMatcher.matchCommand("go back"))
        assertEquals(VoiceCommand.PREVIOUS_LINE, VoiceCommandMatcher.matchCommand("scroll up"))

        assertEquals(VoiceCommand.READ, VoiceCommandMatcher.matchCommand("read to me"))
        assertEquals(VoiceCommand.READ, VoiceCommandMatcher.matchCommand("play"))
        assertEquals(VoiceCommand.READ, VoiceCommandMatcher.matchCommand("start reading"))

        assertEquals(VoiceCommand.STOP, VoiceCommandMatcher.matchCommand("stop"))
        assertEquals(VoiceCommand.STOP, VoiceCommandMatcher.matchCommand("pause"))
        assertEquals(VoiceCommand.STOP, VoiceCommandMatcher.matchCommand("quiet"))

        assertEquals(VoiceCommand.BIGGER_TEXT, VoiceCommandMatcher.matchCommand("bigger text"))
        assertEquals(VoiceCommand.BIGGER_TEXT, VoiceCommandMatcher.matchCommand("zoom in"))
        assertEquals(VoiceCommand.BIGGER_TEXT, VoiceCommandMatcher.matchCommand("increase font"))

        assertEquals(VoiceCommand.SMALLER_TEXT, VoiceCommandMatcher.matchCommand("smaller text"))
        assertEquals(VoiceCommand.SMALLER_TEXT, VoiceCommandMatcher.matchCommand("zoom out"))
        assertEquals(VoiceCommand.SMALLER_TEXT, VoiceCommandMatcher.matchCommand("shrink"))

        assertEquals(VoiceCommand.SCAN_PAGE, VoiceCommandMatcher.matchCommand("scan page"))
        assertEquals(VoiceCommand.SCAN_PAGE, VoiceCommandMatcher.matchCommand("take photo"))
        assertEquals(VoiceCommand.SCAN_PAGE, VoiceCommandMatcher.matchCommand("camera"))

        assertEquals(VoiceCommand.OPEN_LIBRARY, VoiceCommandMatcher.matchCommand("open library"))
        assertEquals(VoiceCommand.OPEN_LIBRARY, VoiceCommandMatcher.matchCommand("my lessons"))
        assertEquals(VoiceCommand.OPEN_LIBRARY, VoiceCommandMatcher.matchCommand("books"))
    }

    @Test
    fun matchCommand_hindiCommands() {
        assertEquals(VoiceCommand.NEXT_LINE, VoiceCommandMatcher.matchCommand("अगली पंक्ति"))
        assertEquals(VoiceCommand.NEXT_LINE, VoiceCommandMatcher.matchCommand("आगे बढ़ो"))
        assertEquals(VoiceCommand.NEXT_LINE, VoiceCommandMatcher.matchCommand("नीचे"))

        assertEquals(VoiceCommand.PREVIOUS_LINE, VoiceCommandMatcher.matchCommand("पिछली पंक्ति"))
        assertEquals(VoiceCommand.PREVIOUS_LINE, VoiceCommandMatcher.matchCommand("ऊपर चलो"))
        assertEquals(VoiceCommand.PREVIOUS_LINE, VoiceCommandMatcher.matchCommand("पीछे"))

        assertEquals(VoiceCommand.READ, VoiceCommandMatcher.matchCommand("पढ़ो"))
        assertEquals(VoiceCommand.READ, VoiceCommandMatcher.matchCommand("सुनाओ"))
        assertEquals(VoiceCommand.READ, VoiceCommandMatcher.matchCommand("पढ़कर सुनाओ"))

        assertEquals(VoiceCommand.STOP, VoiceCommandMatcher.matchCommand("रुको"))
        assertEquals(VoiceCommand.STOP, VoiceCommandMatcher.matchCommand("बंद करो"))
        assertEquals(VoiceCommand.STOP, VoiceCommandMatcher.matchCommand("शांत"))

        assertEquals(VoiceCommand.BIGGER_TEXT, VoiceCommandMatcher.matchCommand("बड़ा करो"))
        assertEquals(VoiceCommand.BIGGER_TEXT, VoiceCommandMatcher.matchCommand("अक्षर बड़े करो"))

        assertEquals(VoiceCommand.SMALLER_TEXT, VoiceCommandMatcher.matchCommand("छोटा करो"))
        assertEquals(VoiceCommand.SMALLER_TEXT, VoiceCommandMatcher.matchCommand("अक्षर छोटे करो"))

        assertEquals(VoiceCommand.SCAN_PAGE, VoiceCommandMatcher.matchCommand("फोटो खींचो"))
        assertEquals(VoiceCommand.SCAN_PAGE, VoiceCommandMatcher.matchCommand("स्कैन करो"))

        assertEquals(VoiceCommand.OPEN_LIBRARY, VoiceCommandMatcher.matchCommand("लाइब्रेरी खोलो"))
        assertEquals(VoiceCommand.OPEN_LIBRARY, VoiceCommandMatcher.matchCommand("किताबें"))
    }

    @Test
    fun matchCommand_marathiCommands() {
        assertEquals(VoiceCommand.NEXT_LINE, VoiceCommandMatcher.matchCommand("पुढची ओळ"))
        assertEquals(VoiceCommand.PREVIOUS_LINE, VoiceCommandMatcher.matchCommand("मागची ओळ"))
        assertEquals(VoiceCommand.READ, VoiceCommandMatcher.matchCommand("वाचा"))
        assertEquals(VoiceCommand.STOP, VoiceCommandMatcher.matchCommand("थांबा"))
        assertEquals(VoiceCommand.BIGGER_TEXT, VoiceCommandMatcher.matchCommand("मोठे करा"))
        assertEquals(VoiceCommand.SMALLER_TEXT, VoiceCommandMatcher.matchCommand("लहान करा"))
        assertEquals(VoiceCommand.SCAN_PAGE, VoiceCommandMatcher.matchCommand("फोटो काढा"))
        assertEquals(VoiceCommand.OPEN_LIBRARY, VoiceCommandMatcher.matchCommand("वाचनालय उघडा"))
    }

    @Test
    fun matchCommand_gujaratiCommands() {
        assertEquals(VoiceCommand.NEXT_LINE, VoiceCommandMatcher.matchCommand("આગળની લીટી"))
        assertEquals(VoiceCommand.PREVIOUS_LINE, VoiceCommandMatcher.matchCommand("પાછળની લીટી"))
        assertEquals(VoiceCommand.READ, VoiceCommandMatcher.matchCommand("વાંચો"))
        assertEquals(VoiceCommand.STOP, VoiceCommandMatcher.matchCommand("રોકો"))
        assertEquals(VoiceCommand.BIGGER_TEXT, VoiceCommandMatcher.matchCommand("મોટું કરો"))
        assertEquals(VoiceCommand.SMALLER_TEXT, VoiceCommandMatcher.matchCommand("નાનું કરો"))
        assertEquals(VoiceCommand.SCAN_PAGE, VoiceCommandMatcher.matchCommand("ફોટો પાડો"))
        assertEquals(VoiceCommand.OPEN_LIBRARY, VoiceCommandMatcher.matchCommand("લાઇબ્રેરી ખોલો"))
    }

    @Test
    fun matchCommand_bengaliCommands() {
        assertEquals(VoiceCommand.NEXT_LINE, VoiceCommandMatcher.matchCommand("পরের লাইন"))
        assertEquals(VoiceCommand.PREVIOUS_LINE, VoiceCommandMatcher.matchCommand("আগের লাইন"))
        assertEquals(VoiceCommand.READ, VoiceCommandMatcher.matchCommand("পড়ুন"))
        assertEquals(VoiceCommand.STOP, VoiceCommandMatcher.matchCommand("থামুন"))
        assertEquals(VoiceCommand.BIGGER_TEXT, VoiceCommandMatcher.matchCommand("বড় করুন"))
        assertEquals(VoiceCommand.SMALLER_TEXT, VoiceCommandMatcher.matchCommand("ছোট করুন"))
        assertEquals(VoiceCommand.SCAN_PAGE, VoiceCommandMatcher.matchCommand("ছবি তুলুন"))
        assertEquals(VoiceCommand.OPEN_LIBRARY, VoiceCommandMatcher.matchCommand("লাইব্রেরি খুলুন"))
    }

    @Test
    fun matchCommand_forgivingFuzzyMatchWithTypos() {
        // Minor typo tolerance
        assertEquals(VoiceCommand.NEXT_LINE, VoiceCommandMatcher.matchCommand("nexxt line"))
        assertEquals(VoiceCommand.STOP, VoiceCommandMatcher.matchCommand("stopp"))
        assertEquals(VoiceCommand.READ, VoiceCommandMatcher.matchCommand("pleay"))
        assertEquals(VoiceCommand.BIGGER_TEXT, VoiceCommandMatcher.matchCommand("biggr"))
        assertEquals(VoiceCommand.SMALLER_TEXT, VoiceCommandMatcher.matchCommand("smalr"))
    }

    @Test
    fun matchCommand_unknownOrGibberish_returnsUnknown() {
        assertEquals(VoiceCommand.UNKNOWN, VoiceCommandMatcher.matchCommand("xyz123"))
        assertEquals(VoiceCommand.UNKNOWN, VoiceCommandMatcher.matchCommand(""))
        assertEquals(VoiceCommand.UNKNOWN, VoiceCommandMatcher.matchCommand("   "))
    }

    @Test
    fun levenshteinDistance_computesAccurately() {
        assertEquals(0, VoiceCommandMatcher.levenshteinDistance("kitten", "kitten"))
        assertEquals(3, VoiceCommandMatcher.levenshteinDistance("kitten", "sitting"))
        assertEquals(1, VoiceCommandMatcher.levenshteinDistance("next", "nekt"))
    }
}
