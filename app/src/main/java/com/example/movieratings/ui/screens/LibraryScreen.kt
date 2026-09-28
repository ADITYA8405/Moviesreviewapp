package com.example.movieratings.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import com.example.movieratings.ui.theme.AppIcons
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.movieratings.data.model.MediaItem
import com.example.movieratings.data.model.Review
import com.example.movieratings.data.repository.SavedMovie
import com.example.movieratings.ui.theme.White
import com.example.movieratings.viewmodel.UserLibraryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Tab identifiers
private const val TAB_FAVOURITES = 0
private const val TAB_WATCHLIST  = 1
private const val TAB_REVIEWS   = 2

// Colours consistent with the existing dark theme
private val CardBg       = Color(0xFF1A1A1A)
private val DividerColor = Color(0xFF2A2A2A)
private val SubtleText   = Color(0xFF888888)
private val StarGold     = Color(0xFFFFC107)

/**
 * Library/Profile screen showing the user's Favourites, Watchlist, and Reviews.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: UserLibraryViewModel,
    userEmail: String?,
    onBackClick: () -> Unit,
    onMovieClick: (SavedMovie) -> Unit,
    onReviewClick: (Int) -> Unit = {}
) {
    val favourites by viewModel.favourites.collectAsState()
    val watchlist  by viewModel.watchlist.collectAsState()
    val myReviews  by viewModel.myReviews.collectAsState()

    var selectedTab by remember { mutableIntStateOf(TAB_FAVOURITES) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "My Library",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── User info header ─────────────────────────────────────────
            if (userEmail != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // Avatar
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), CircleShape)
                    ) {
                        Text(
                            text  = userEmail.firstOrNull()?.uppercaseChar()?.toString() ?: "U",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = userEmail,
                            style = MaterialTheme.typography.bodyMedium,
                            color = White,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row {
                            StatChip("${favourites.size} Favs")
                            Spacer(modifier = Modifier.width(8.dp))
                            StatChip("${watchlist.size} Watchlist")
                            Spacer(modifier = Modifier.width(8.dp))
                            StatChip("${myReviews.size} Reviews")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            // ── Tab bar ──────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TabChip(
                    label    = "Favourites",
                    icon     = Icons.Filled.Favorite,
                    count    = favourites.size,
                    selected = selectedTab == TAB_FAVOURITES,
                    onClick  = { selectedTab = TAB_FAVOURITES },
                    modifier = Modifier.weight(1f)
                )
                TabChip(
                    label    = "Watchlist",
                    icon     = AppIcons.Bookmark,
                    count    = watchlist.size,
                    selected = selectedTab == TAB_WATCHLIST,
                    onClick  = { selectedTab = TAB_WATCHLIST },
                    modifier = Modifier.weight(1f)
                )
                TabChip(
                    label    = "Reviews",
                    icon     = AppIcons.RateReview,
                    count    = myReviews.size,
                    selected = selectedTab == TAB_REVIEWS,
                    onClick  = { selectedTab = TAB_REVIEWS },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ── Tab content ──────────────────────────────────────────────
            when (selectedTab) {
                TAB_FAVOURITES -> SavedMovieList(
                    items = favourites,
                    emptyText = "No favourites yet.\nTap the ❤️ on any movie to save it here.",
                    onMovieClick = onMovieClick
                )
                TAB_WATCHLIST -> SavedMovieList(
                    items = watchlist,
                    emptyText = "Your watchlist is empty.\nTap the 🔖 on any movie to add it.",
                    onMovieClick = onMovieClick
                )
                TAB_REVIEWS -> ReviewList(
                    reviews = myReviews,
                    emptyText = "You haven't written any reviews yet.\nOpen a movie and tap \"Rate & Review\".",
                    onMovieClick = onReviewClick
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Tab chip
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TabChip(
    label: String,
    icon: ImageVector,
    count: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else CardBg,
        animationSpec = tween(200),
        label = "tabBg"
    )
    val contentColor = if (selected) White else SubtleText

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .border(
                width = 1.dp,
                color = if (selected) Color.Transparent else DividerColor,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = "$count",
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
            fontWeight = FontWeight.Bold
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Stat chip (small pill showing counts in user header)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun StatChip(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = SubtleText
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// SavedMovieList (Favourites / Watchlist)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SavedMovieList(
    items: List<SavedMovie>,
    emptyText: String,
    onMovieClick: (SavedMovie) -> Unit
) {
    if (items.isEmpty()) {
        EmptyPlaceholder(text = emptyText)
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(items, key = { it.movieId }) { movie ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardBg)
                    .border(1.dp, DividerColor, RoundedCornerShape(12.dp))
                    .clickable { onMovieClick(movie) }
                    .padding(10.dp)
            ) {
                // Poster
                AsyncImage(
                    model = movie.posterPath?.let { "https://image.tmdb.org/t/p/w200$it" }
                        ?: "https://via.placeholder.com/200x300?text=No+Poster",
                    contentDescription = movie.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .width(60.dp)
                        .height(90.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = movie.title.ifBlank { "Unknown Title" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = White,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (movie.voteAverage != null) {
                            Icon(
                                Icons.Filled.Star,
                                contentDescription = null,
                                tint = StarGold,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = String.format("%.1f", movie.voteAverage),
                                style = MaterialTheme.typography.labelSmall,
                                color = SubtleText
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                        }
                        if (!movie.releaseDate.isNullOrBlank()) {
                            Text(
                                text = movie.releaseDate.take(4),   // year only
                                style = MaterialTheme.typography.labelSmall,
                                color = SubtleText
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ReviewList (My Reviews tab)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ReviewList(
    reviews: List<Review>,
    emptyText: String,
    onMovieClick: (Int) -> Unit
) {
    if (reviews.isEmpty()) {
        EmptyPlaceholder(text = emptyText)
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(reviews, key = { "${it.movieId}_${it.userId}" }) { review ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardBg)
                    .border(1.dp, DividerColor, RoundedCornerShape(12.dp))
                    .clickable { onMovieClick(review.movieId) }
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Movie #${review.movieId}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = White,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    // Stars
                    Row {
                        repeat(5) { idx ->
                            Icon(
                                Icons.Filled.Star,
                                contentDescription = null,
                                tint = if (idx < review.rating) StarGold else Color(0xFF3A3A3A),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
                if (review.reviewText.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = review.reviewText,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFBBBBBB),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 18.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatDateShort(review.updatedAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = SubtleText
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Empty placeholder
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun EmptyPlaceholder(text: String) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = SubtleText,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Utility
// ─────────────────────────────────────────────────────────────────────────────

private fun formatDateShort(epochMillis: Long): String {
    if (epochMillis == 0L) return ""
    return SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(epochMillis))
}
