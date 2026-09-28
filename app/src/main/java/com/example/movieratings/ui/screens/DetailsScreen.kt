package com.example.movieratings.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.movieratings.data.model.MediaItem
import com.example.movieratings.data.model.Review
import com.example.movieratings.ui.theme.White
import com.example.movieratings.viewmodel.ReviewViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val StarGold     = Color(0xFFFFC107)
private val StarEmpty    = Color(0xFF3A3A3A)
private val CardBg       = Color(0xFF1A1A1A)
private val OwnCardBg    = Color(0xFF1E1418)   // very subtle warm tint for own review
private val OwnCardBorder= Color(0xFF8B0000).copy(alpha = 0.6f)
private val SubtleText   = Color(0xFF888888)
private val DividerColor = Color(0xFF2A2A2A)

/**
 * Movie/TV details screen with an improved rating & review section.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    item: MediaItem?,
    onBackClick: () -> Unit,
    currentUserId: String?,
    currentUserEmail: String?,
    reviewViewModel: ReviewViewModel = viewModel()
) {
    LaunchedEffect(item?.id, currentUserId) {
        if (item != null && currentUserId != null) {
            reviewViewModel.init(
                movieId   = item.id,
                userId    = currentUserId,
                userEmail = currentUserEmail ?: currentUserId
            )
        }
    }

    val reviews     by reviewViewModel.reviews.collectAsState()
    val userReview  by reviewViewModel.userReview.collectAsState()
    val isEditing   by reviewViewModel.isEditing.collectAsState()
    val draftRating by reviewViewModel.draftRating.collectAsState()
    val draftText   by reviewViewModel.draftText.collectAsState()
    val isSaving    by reviewViewModel.isSaving.collectAsState()
    val errorMsg    by reviewViewModel.errorMessage.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        if (item == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Movie not found", style = MaterialTheme.typography.bodyLarge)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // ── Backdrop with gradient overlay ────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            ) {
                AsyncImage(
                    model = item.backdropUrl,
                    contentDescription = "${item.displayTitle} backdrop",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // Gradient: transparent → black at bottom
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0xFF000000)),
                                startY = 100f
                            )
                        )
                )
                // TMDb badge
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(14.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF000000).copy(alpha = 0.55f))
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = StarGold,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${item.voteAverage ?: "N/A"}  TMDb",
                        style = MaterialTheme.typography.labelMedium,
                        color = White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ── Title & date ──────────────────────────────────────────────
            Text(
                text = item.displayTitle,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Text(
                text = item.displayDate,
                style = MaterialTheme.typography.labelMedium,
                color = SubtleText,
                modifier = Modifier.padding(start = 16.dp, top = 3.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // ── Poster + overview row ──────────────────────────────────────
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.Top
            ) {
                AsyncImage(
                    model = item.posterUrl,
                    contentDescription = "${item.displayTitle} poster",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .width(100.dp)
                        .height(150.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = item.overview ?: "No description available.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFCCCCCC),
                    lineHeight = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
            HorizontalDivider(color = DividerColor, thickness = 1.dp)
            Spacer(modifier = Modifier.height(20.dp))

            // ── Review section ────────────────────────────────────────────
            if (currentUserId != null) {
                ReviewSection(
                    movieId        = item.id,
                    userId         = currentUserId,
                    userEmail      = currentUserEmail ?: currentUserId,
                    userReview     = userReview,
                    allReviews     = reviews,
                    isEditing      = isEditing,
                    draftRating    = draftRating,
                    draftText      = draftText,
                    isSaving       = isSaving,
                    errorMessage   = errorMsg,
                    onRatingChange  = reviewViewModel::setDraftRating,
                    onTextChange    = reviewViewModel::setDraftText,
                    onEditClick     = reviewViewModel::openEditor,
                    onCancelClick   = reviewViewModel::cancelEditor,
                    onSaveClick     = {
                        reviewViewModel.saveReview(
                            item.id, currentUserId,
                            currentUserEmail ?: currentUserId
                        )
                    },
                    onDeleteClick   = { reviewViewModel.deleteReview(item.id, currentUserId) },
                    onClearError    = reviewViewModel::clearError
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ReviewSection
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ReviewSection(
    movieId: Int,
    userId: String,
    userEmail: String,
    userReview: Review?,
    allReviews: List<Review>,
    isEditing: Boolean,
    draftRating: Int,
    draftText: String,
    isSaving: Boolean,
    errorMessage: String?,
    onRatingChange: (Int) -> Unit,
    onTextChange: (String) -> Unit,
    onEditClick: () -> Unit,
    onCancelClick: () -> Unit,
    onSaveClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onClearError: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {

        // Section header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(20.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.primary)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Ratings & Reviews",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = White
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Your review block ─────────────────────────────────────────────
        AnimatedVisibility(
            visible = isEditing,
            enter   = expandVertically(animationSpec = tween(300)) + fadeIn(tween(200)),
            exit    = shrinkVertically(animationSpec = tween(250)) + fadeOut(tween(150))
        ) {
            ReviewEditor(
                draftRating    = draftRating,
                draftText      = draftText,
                isSaving       = isSaving,
                errorMessage   = errorMessage,
                isExisting     = userReview != null,
                onRatingChange = onRatingChange,
                onTextChange   = onTextChange,
                onSave         = onSaveClick,
                onCancel       = onCancelClick,
                onClearError   = onClearError
            )
        }

        if (!isEditing) {
            if (userReview != null) {
                OwnReviewCard(
                    review   = userReview,
                    onEdit   = onEditClick,
                    onDelete = onDeleteClick
                )
            } else {
                WriteReviewPrompt(onClick = onEditClick)
            }
        }

        // ── Community reviews ─────────────────────────────────────────────
        val others = allReviews.filter { it.userId != userId }
        if (others.isNotEmpty()) {
            Spacer(modifier = Modifier.height(22.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text  = "Community",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text  = "${others.size}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            others.forEach { review ->
                CommunityReviewCard(review = review)
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ReviewEditor
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ReviewEditor(
    draftRating: Int,
    draftText: String,
    isSaving: Boolean,
    errorMessage: String?,
    isExisting: Boolean,
    onRatingChange: (Int) -> Unit,
    onTextChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    onClearError: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CardBg)
            .border(1.dp, DividerColor, RoundedCornerShape(14.dp))
            .padding(18.dp)
    ) {
        Text(
            text  = if (isExisting) "Edit Your Review" else "Write a Review",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = White
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Star picker label
        Text(
            text  = "Your Rating",
            style = MaterialTheme.typography.labelLarge,
            color = SubtleText,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(10.dp))

        // Interactive star row with animated scale
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            for (star in 1..5) {
                val selected = star <= draftRating
                val scale by animateFloatAsState(
                    targetValue = if (selected) 1.15f else 1f,
                    animationSpec = spring(stiffness = 500f),
                    label = "starScale$star"
                )
                val tint by animateColorAsState(
                    targetValue = if (selected) StarGold else StarEmpty,
                    animationSpec = tween(150),
                    label = "starTint$star"
                )
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = "$star stars",
                    tint = tint,
                    modifier = Modifier
                        .size(38.dp)
                        .scale(scale)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { onRatingChange(star) }
                )
            }
        }

        // Rating label text
        if (draftRating > 0) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text  = ratingLabel(draftRating),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Review text field
        OutlinedTextField(
            value         = draftText,
            onValueChange = {
                onTextChange(it)
                if (errorMessage != null) onClearError()
            },
            placeholder   = {
                Text(
                    "Share your thoughts about this title…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SubtleText
                )
            },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 7,
            textStyle = LocalTextStyle.current.copy(color = White),
            colors    = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = DividerColor,
                cursorColor          = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(10.dp)
        )

        // Error
        AnimatedVisibility(visible = errorMessage != null) {
            Text(
                text      = errorMessage ?: "",
                color     = MaterialTheme.colorScheme.error,
                style     = MaterialTheme.typography.bodySmall,
                modifier  = Modifier.padding(top = 6.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Actions
        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth()
        ) {
            TextButton(
                onClick = onCancel,
                enabled = !isSaving
            ) {
                Text("Cancel", color = SubtleText)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onSave,
                enabled = !isSaving,
                shape   = RoundedCornerShape(10.dp),
                colors  = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.height(40.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color       = White
                    )
                } else {
                    Text(
                        if (isExisting) "Update Review" else "Submit Review",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// OwnReviewCard
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun OwnReviewCard(
    review: Review,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(OwnCardBg)
            .border(1.dp, OwnCardBorder, RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Avatar circle
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), CircleShape)
            ) {
                Text(
                    text  = review.userEmail.firstOrNull()?.uppercaseChar()?.toString() ?: "U",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = "Your Review",
                    style = MaterialTheme.typography.labelLarge,
                    color = White,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text  = formatDate(review.updatedAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = SubtleText
                )
            }

            // Edit
            SmallFloatingActionButton(
                onClick            = onEdit,
                containerColor     = Color(0xFF2A2A2A),
                contentColor       = White,
                modifier           = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Edit review",
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            // Delete
            SmallFloatingActionButton(
                onClick        = onDelete,
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                contentColor   = MaterialTheme.colorScheme.primary,
                modifier       = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete review",
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Stars + label
        Row(verticalAlignment = Alignment.CenterVertically) {
            StarDisplay(rating = review.rating, maxStars = 5)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text  = ratingLabel(review.rating),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (review.reviewText.isNotBlank()) {
            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = DividerColor, thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text  = review.reviewText,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFDDDDDD),
                lineHeight = 20.sp
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// CommunityReviewCard
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CommunityReviewCard(review: Review) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardBg)
            .border(1.dp, DividerColor, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Avatar
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2A2A2A))
            ) {
                Text(
                    text  = review.userEmail.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                    style = MaterialTheme.typography.labelLarge,
                    color = SubtleText,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = review.userEmail,
                    style = MaterialTheme.typography.labelMedium,
                    color = White,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text  = formatDate(review.updatedAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = SubtleText
                )
            }
            StarDisplay(rating = review.rating, maxStars = 5)
        }

        if (review.reviewText.isNotBlank()) {
            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = DividerColor, thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text  = review.reviewText,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFBBBBBB),
                lineHeight = 18.sp
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// WriteReviewPrompt
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun WriteReviewPrompt(onClick: () -> Unit) {
    Row(
        verticalAlignment   = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CardBg)
            .border(
                1.dp,
                Brush.horizontalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                        Color.Transparent
                    )
                ),
                RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 16.dp)
    ) {
        Column {
            Text(
                text  = "Rate this title",
                style = MaterialTheme.typography.titleSmall,
                color = White,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text  = "Tap to give a star rating and write a review",
                style = MaterialTheme.typography.labelSmall,
                color = SubtleText
            )
        }
        // Greyed-out star row as teaser
        Row {
            repeat(5) {
                Icon(
                    imageVector = Icons.Outlined.Star,
                    contentDescription = null,
                    tint = Color(0xFF444444),
                    modifier = Modifier
                        .size(24.dp)
                        .padding(1.dp)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// StarDisplay — compact read-only row
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun StarDisplay(rating: Int, maxStars: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        for (star in 1..maxStars) {
            Icon(
                imageVector = if (star <= rating) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = null,
                tint = if (star <= rating) StarGold else StarEmpty,
                modifier = Modifier
                    .size(16.dp)
                    .padding(1.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Helpers
// ─────────────────────────────────────────────────────────────────────────────

private fun ratingLabel(rating: Int): String = when (rating) {
    1    -> "Poor"
    2    -> "Fair"
    3    -> "Good"
    4    -> "Great"
    5    -> "Excellent"
    else -> ""
}

private fun formatDate(epochMillis: Long): String {
    if (epochMillis == 0L) return ""
    return SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(epochMillis))
}
