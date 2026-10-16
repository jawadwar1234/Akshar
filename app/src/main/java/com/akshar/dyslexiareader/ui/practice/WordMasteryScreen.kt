package com.akshar.dyslexiareader.ui.practice

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akshar.dyslexiareader.R
import com.akshar.dyslexiareader.data.local.entity.WordMasteryEntity
import com.akshar.dyslexiareader.domain.repository.WordMasteryRepository
import com.akshar.dyslexiareader.ui.reader.TextToSpeechManager
import com.akshar.dyslexiareader.ui.theme.CalmTeal
import com.akshar.dyslexiareader.ui.theme.CharcoalTextPrimary
import com.akshar.dyslexiareader.ui.theme.CharcoalTextSecondary
import com.akshar.dyslexiareader.ui.theme.DesignTokens
import com.akshar.dyslexiareader.ui.theme.PaperCreamBackground
import com.akshar.dyslexiareader.ui.theme.PaperCreamBorder
import com.akshar.dyslexiareader.ui.theme.PaperCreamSurface
import com.akshar.dyslexiareader.ui.theme.TerracottaAccent
import com.akshar.dyslexiareader.ui.theme.WarmForestGreen
import com.akshar.dyslexiareader.ui.theme.WarmForestGreenContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordMasteryScreen(
    profileId: String,
    wordMasteryRepository: WordMasteryRepository,
    ttsManager: TextToSpeechManager,
    onNavigateBack: () -> Unit
) {
    val masteredWords by wordMasteryRepository.getMasteredWords(profileId)
        .collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Words You Have Mastered",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalTextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                            tint = CharcoalTextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PaperCreamBackground)
            )
        },
        containerColor = PaperCreamBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = DesignTokens.Spacing.lg),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.sm))

            // Mastery Count Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = DesignTokens.ElevationAndBorders.borderThin,
                        color = WarmForestGreen.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(16.dp)
                    ),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = WarmForestGreenContainer)
            ) {
                Row(
                    modifier = Modifier.padding(DesignTokens.Spacing.md),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = WarmForestGreen,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(DesignTokens.Spacing.md))
                    Column {
                        Text(
                            text = "${masteredWords.size} Words Mastered! 🎉",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = WarmForestGreen
                        )
                        Text(
                            text = "Tap any word to listen to its pronunciation and slow syllables.",
                            style = MaterialTheme.typography.bodySmall,
                            color = CharcoalTextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(DesignTokens.Spacing.lg))

            if (masteredWords.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No mastered words yet. Practice reading textbook lessons to grow your list!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = CharcoalTextSecondary
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.md),
                    verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.md),
                    contentPadding = PaddingValues(bottom = DesignTokens.Spacing.xxl),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(masteredWords) { wordItem ->
                        WordMasteryCard(
                            wordItem = wordItem,
                            onPlayPronunciation = { ttsManager.speakWord(wordItem.cleanWord) },
                            onPlaySyllables = { ttsManager.speakWordSlowlySyllables(wordItem.cleanWord) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WordMasteryCard(
    wordItem: WordMasteryEntity,
    onPlayPronunciation: () -> Unit,
    onPlaySyllables: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = DesignTokens.ElevationAndBorders.borderThin,
                color = PaperCreamBorder,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PaperCreamSurface)
    ) {
        Column(
            modifier = Modifier.padding(DesignTokens.Spacing.md)
        ) {
            Text(
                text = wordItem.word,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = CharcoalTextPrimary
            )
            Text(
                text = "Read ${wordItem.timesRead}x • Practiced ${wordItem.timesPracticed}x",
                style = MaterialTheme.typography.bodySmall,
                color = CharcoalTextSecondary
            )

            Spacer(modifier = Modifier.height(DesignTokens.Spacing.sm))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Syllables button
                IconButton(
                    onClick = onPlaySyllables,
                    modifier = Modifier
                        .size(36.dp)
                        .background(CalmTeal.copy(alpha = 0.12f), shape = CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Spa,
                        contentDescription = "Slow syllables",
                        tint = CalmTeal,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(DesignTokens.Spacing.xs))

                // Normal speech button
                IconButton(
                    onClick = onPlayPronunciation,
                    modifier = Modifier
                        .size(36.dp)
                        .background(TerracottaAccent.copy(alpha = 0.12f), shape = CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Pronounce word",
                        tint = TerracottaAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
