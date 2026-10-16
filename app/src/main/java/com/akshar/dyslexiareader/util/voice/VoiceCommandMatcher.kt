package com.akshar.dyslexiareader.util.voice

object VoiceCommandMatcher {

    data class CommandDefinition(
        val command: VoiceCommand,
        val keywords: List<String>
    )

    private val COMMAND_DICTIONARY = listOf(
        // NEXT LINE
        CommandDefinition(
            command = VoiceCommand.NEXT_LINE,
            keywords = listOf(
                // English
                "next line", "next", "down", "forward", "scroll down", "line down", "go down",
                // Hindi
                "अगली पंक्ति", "अगली लाइन", "आगे", "नीचे", "आगे बढ़ो", "अगला", "नीचे चलो",
                // Marathi
                "पुढची ओळ", "पुढे", "खाली", "पुढील ओळ", "खाली जा", "पुढची",
                // Gujarati
                "આગળની લીટી", "આગળ", "નીચે", "પછીની લીટી", "આગળ વધો",
                // Bengali
                "পরের লাইন", "পরবর্তী লাইন", "নিচে", "সামনে", "পরেরটা"
            )
        ),

        // PREVIOUS LINE
        CommandDefinition(
            command = VoiceCommand.PREVIOUS_LINE,
            keywords = listOf(
                // English
                "previous line", "previous", "back", "up", "scroll up", "line up", "go back", "last line",
                // Hindi
                "पिछली पंक्ति", "पिछली लाइन", "पीछे", "ऊपर", "वापस", "पिछला", "ऊपर चलो",
                // Marathi
                "मागची ओळ", "मागे", "वर", "मागील ओळ", "वर जा", "मागची",
                // Gujarati
                "પાછળની લીટી", "પાછળ", "ઉપર", "પહેલાની લીટી", "પાછા જાઓ",
                // Bengali
                "আগের লাইন", "পূর্ববর্তী লাইন", "উপরে", "পেছনে", "আগেরটা"
            )
        ),

        // READ / PLAY
        CommandDefinition(
            command = VoiceCommand.READ,
            keywords = listOf(
                // English
                "read", "read to me", "play", "speak", "start reading", "continue", "start", "listen",
                // Hindi
                "पढ़ो", "सुनाओ", "पढ़कर सुनाओ", "शुरू करो", "चालू करो", "बोल के बताओ",
                // Marathi
                "वाचा", "वाचून दाखवा", "ऐकवा", "सुरू करा", "बोला",
                // Gujarati
                "વાંચો", "વાંચી સંભળાવો", "સાંભળાવો", "શરૂ કરો", "બોલો",
                // Bengali
                "পড়ুন", "পড়ে শোনাও", "শুরু করুন", "শোনাও", "বলুন"
            )
        ),

        // STOP / PAUSE
        CommandDefinition(
            command = VoiceCommand.STOP,
            keywords = listOf(
                // English
                "stop", "pause", "quiet", "hush", "shut up", "hold on", "halt", "silence",
                // Hindi
                "रुको", "रुक जाओ", "बंद करो", "चुप", "शांत", "रोको",
                // Marathi
                "थांबा", "बंद करा", "शांत", "रोका",
                // Gujarati
                "રોકો", "બંધ કરો", "શાંત", "અટકો",
                // Bengali
                "থামুন", "থামো", "বন্ধ করুন", "চুপ", "শান্ত হও"
            )
        ),

        // BIGGER TEXT
        CommandDefinition(
            command = VoiceCommand.BIGGER_TEXT,
            keywords = listOf(
                // English
                "bigger text", "bigger", "larger", "zoom in", "increase font", "make it big", "large text",
                // Hindi
                "बड़ा करो", "बड़ा टेक्स्ट", "बड़ा फॉन्ट", "अक्षर बड़े करो", "ज़ूम इन", "बड़ा",
                // Marathi
                "मोठे करा", "मोठा फॉन्ट", "अक्षरे मोठी करा", "मोठे",
                // Gujarati
                "મોટું કરો", "મોટા અક્ષર", "મોટો ફોન્ટ", "મોટું",
                // Bengali
                "বড় করুন", "বড় লেখা", "ফন্ট বড় করুন", "বড়"
            )
        ),

        // SMALLER TEXT
        CommandDefinition(
            command = VoiceCommand.SMALLER_TEXT,
            keywords = listOf(
                // English
                "smaller text", "smaller", "decrease font", "zoom out", "make it small", "tiny text", "shrink",
                // Hindi
                "छोटा करो", "छोटा टेक्स्ट", "छोटा फॉन्ट", "अक्षर छोटे करो", "ज़ूम आउट", "छोटा",
                // Marathi
                "लहान करा", "लहान फॉन्ट", "अक्षरे लहान करा", "लहान",
                // Gujarati
                "નાનું કરો", "નાના અક્ષર", "નાનો ફોન્ટ", "નાનું",
                // Bengali
                "ছোট করুন", "ছোট লেখা", "ফন্ট ছোট করুন", "ছোট"
            )
        ),

        // SCAN PAGE
        CommandDefinition(
            command = VoiceCommand.SCAN_PAGE,
            keywords = listOf(
                // English
                "scan page", "scan", "take photo", "camera", "capture", "photograph", "new page",
                // Hindi
                "फोटो खींचो", "स्कैन करो", "कैमरा खोलो", "नया पन्ना", "स्कैन",
                // Marathi
                "फोटो काढा", "स्कॅन करा", "कॅमेरा उघडा", "नवीन पान", "स्कॅन",
                // Gujarati
                "ફોટો પાડો", "સ્કેન કરો", "કેમેરા ખોલો", "નવું પાનું", "સ્કેન",
                // Bengali
                "ছবি তুলুন", "স্ক্যান করুন", "ক্যামেরা খুলুন", "নতুন পাতা", "স্ক্যান"
            )
        ),

        // OPEN LIBRARY
        CommandDefinition(
            command = VoiceCommand.OPEN_LIBRARY,
            keywords = listOf(
                // English
                "open library", "library", "books", "home", "my lessons", "all books", "back to library",
                // Hindi
                "लाइब्रेरी खोलो", "लाइब्रेरी", "किताबें", "होम", "पाठ सूची", "मेरी किताबें",
                // Marathi
                "वाचनालय उघडा", "वाचनालय", "पुस्तके", "माझी पुस्तके", "होम",
                // Gujarati
                "લાઇબ્રેરી ખોલો", "લાઇબ્રેરી", "પુસ્તકો", "મારા પાઠ", "હોમ",
                // Bengali
                "লাইব্রেরি খুলুন", "লাইব্রেরি", "বইগুলো", "পাঠ তালিকা", "হোম"
            )
        )
    )

    /**
     * Matches raw spoken input forgivingly against supported commands.
     * Evaluates exact inclusion, word token match, and fuzzy Levenshtein distance.
     */
    fun matchCommand(spokenText: String): VoiceCommand {
        val cleanInput = normalizeText(spokenText)
        if (cleanInput.isBlank()) return VoiceCommand.UNKNOWN

        // 1. Direct contains check across phrases
        for (def in COMMAND_DICTIONARY) {
            for (keyword in def.keywords) {
                val cleanKeyword = normalizeText(keyword)
                if (cleanInput == cleanKeyword || cleanInput.contains(cleanKeyword) || cleanKeyword.contains(cleanInput)) {
                    return def.command
                }
            }
        }

        // 2. Token / word-level match with fuzzy tolerance
        val inputTokens = cleanInput.split(" ").filter { it.isNotBlank() }
        var bestCommand = VoiceCommand.UNKNOWN
        var lowestDistance = Int.MAX_VALUE

        for (def in COMMAND_DICTIONARY) {
            for (keyword in def.keywords) {
                val cleanKeyword = normalizeText(keyword)
                val keywordTokens = cleanKeyword.split(" ").filter { it.isNotBlank() }

                // Check distance against full phrase
                val phraseDistance = levenshteinDistance(cleanInput, cleanKeyword)
                val maxAllowedDistance = when {
                    cleanKeyword.length <= 4 -> 1
                    cleanKeyword.length <= 8 -> 2
                    else -> 3
                }

                if (phraseDistance <= maxAllowedDistance && phraseDistance < lowestDistance) {
                    lowestDistance = phraseDistance
                    bestCommand = def.command
                }

                // Check individual tokens
                for (inToken in inputTokens) {
                    for (kwToken in keywordTokens) {
                        val tokenDist = levenshteinDistance(inToken, kwToken)
                        val tokenThreshold = if (kwToken.length <= 4) 1 else 2
                        if (tokenDist <= tokenThreshold && tokenDist < lowestDistance) {
                            lowestDistance = tokenDist
                            bestCommand = def.command
                        }
                    }
                }
            }
        }

        return bestCommand
    }

    private fun normalizeText(text: String): String {
        return text.lowercase()
            .replace(Regex("[.,!?;:\"'\\-_()\\[\\]{}]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    /**
     * Standard dynamic programming Levenshtein distance algorithm.
     */
    fun levenshteinDistance(s1: String, s2: String): Int {
        val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }

        for (i in 0..s1.length) {
            dp[i][0] = i
        }
        for (j in 0..s2.length) {
            dp[0][j] = j
        }

        for (i in 1..s1.length) {
            for (j in 1..s2.length) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,      // deletion
                    dp[i][j - 1] + 1,      // insertion
                    dp[i - 1][j - 1] + cost // substitution
                )
            }
        }

        return dp[s1.length][s2.length]
    }
}
