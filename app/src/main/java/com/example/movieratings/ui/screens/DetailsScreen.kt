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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import com.example.movieratings.ui.theme.AppIcons
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
import com.example.movieratings.data.model.DiscussionComment
import com.example.movieratings.data.model.MediaItem
import com.example.movieratings.data.model.Review
import com.example.movieratings.data.repository.SavedMovie
import com.example.movieratings.ui.theme.White
import com.example.movieratings.viewmodel.DiscussionViewModel
import com.example.movieratings.viewmodel.ReviewViewModel
import com.example.movieratings.viewmodel.UserLibraryViewModel
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
private val FavRed       = Color(0xFFE53935)
private val WatchlistBlue = Color(0xFF42A5F5)

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
    reviewViewModel: ReviewViewModel = viewModel(),
    discussionViewModel: DiscussionViewModel = viewModel(),
    libraryViewModel: UserLibraryViewModel
) {
    LaunchedEffect(item?.id, currentUserId) {
        if (item != null && currentUserId != null) {
            reviewViewModel.init(
                movieId   = item.id,
                userId    = currentUserId,
                userEmail = currentUserEmail ?: currentUserId
            )
            discussionViewModel.init(item.id)
            libraryViewModel.initLibrary(currentUserId)
        }
    }

    val reviews     by reviewViewModel.reviews.collectAsState()
    val userReview  by reviewViewModel.userReview.collectAsState()
    val isEditing   by reviewViewModel.isEditing.collectAsState()
    val draftRating by reviewViewModel.draftRating.collectAsState()
    val draftText   by reviewViewModel.draftText.collectAsState()
    val isSaving    by reviewViewModel.isSaving.collectAsState()
    val errorMsg    by reviewViewModel.errorMessage.collectAsState()

    // Public discussion state
    val comments         by discussionViewModel.comments.collectAsState()
    val draftComment     by discussionViewModel.draftComment.collectAsState()
    val isPostingComment by discussionViewModel.isPosting.collectAsState()
    val editingCommentId by discussionViewModel.editingCommentId.collectAsState()
    val editingText      by discussionViewModel.editingText.collectAsState()
    val isSavingEdit     by discussionViewModel.isSavingEdit.collectAsState()
    val discussionError  by discussionViewModel.errorMessage.collectAsState()

    // Favourite / Watchlist state derived reactively from real-time library flows
    val favourites    by libraryViewModel.favourites.collectAsState()
    val watchlist     by libraryViewModel.watchlist.collectAsState()
    val isFavourite   = item != null && favourites.any { it.movieId == item.id }
    val isInWatchlist = item != null && watchlist.any { it.movieId == item.id }

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

            Spacer(modifier = Modifier.height(20.dp))

            // ── Favourite / Watchlist action row ──────────────────────────
            if (currentUserId != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Favourite button
                    val favBg by animateColorAsState(
                        targetValue = if (isFavourite) FavRed.copy(alpha = 0.15f) else CardBg,
                        animationSpec = tween(200),
                        label = "favBg"
                    )
                    val favBorder by animateColorAsState(
                        targetValue = if (isFavourite) FavRed.copy(alpha = 0.5f) else DividerColor,
                        animationSpec = tween(200),
                        label = "favBorder"
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(favBg)
                            .border(1.dp, favBorder, RoundedCornerShape(10.dp))
                            .clickable {
                                libraryViewModel.toggleFavourite(
                                    currentUserId,
                                    SavedMovie(
                                        movieId      = item.id,
                                        title        = item.displayTitle,
                                        posterPath   = item.posterPath,
                                        backdropPath = item.backdropPath,
                                        overview     = item.overview,
                                        voteAverage  = item.voteAverage,
                                        releaseDate  = item.displayDate
                                    )
                                )
                            }
                            .padding(vertical = 12.dp)
                    ) {
                        Icon(
                            imageVector = if (isFavourite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = if (isFavourite) "Remove from favourites" else "Add to favourites",
                            tint = if (isFavourite) FavRed else SubtleText,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isFavourite) "Favourited" else "Favourite",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isFavourite) FavRed else SubtleText,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Watchlist button
                    val wlBg by animateColorAsState(
                        targetValue = if (isInWatchlist) WatchlistBlue.copy(alpha = 0.15f) else CardBg,
                        animationSpec = tween(200),
                        label = "wlBg"
                    )
                    val wlBorder by animateColorAsState(
                        targetValue = if (isInWatchlist) WatchlistBlue.copy(alpha = 0.5f) else DividerColor,
                        animationSpec = tween(200),
                        label = "wlBorder"
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(wlBg)
                            .border(1.dp, wlBorder, RoundedCornerShape(10.dp))
                            .clickable {
                                libraryViewModel.toggleWatchlist(
                                    currentUserId,
                                    SavedMovie(
                                        movieId      = item.id,
                                        title        = item.displayTitle,
                                        posterPath   = item.posterPath,
                                        backdropPath = item.backdropPath,
                                        overview     = item.overview,
                                        voteAverage  = item.voteAverage,
                                        releaseDate  = item.displayDate
                                    )
                                )
                            }
                            .padding(vertical = 12.dp)
                    ) {
                        Icon(
                            imageVector = if (isInWatchlist) AppIcons.Bookmark else AppIcons.BookmarkBorder,
                            contentDescription = if (isInWatchlist) "Remove from watchlist" else "Add to watchlist",
                            tint = if (isInWatchlist) WatchlistBlue else SubtleText,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isInWatchlist) "In Watchlist" else "Watchlist",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isInWatchlist) WatchlistBlue else SubtleText,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
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

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = DividerColor, thickness = 1.dp)
            Spacer(modifier = Modifier.height(20.dp))

            // ── Public Discussion / Comments section ──────────────────────
            if (currentUserId != null) {
                DiscussionSection(
                    movieId          = item.id,
                    userId           = currentUserId,
                    userEmail        = currentUserEmail ?: currentUserId,
                    comments         = comments,
                    draftComment     = draftComment,
                    isPosting        = isPostingComment,
                    editingCommentId = editingCommentId,
                    editingText      = editingText,
                    isSavingEdit     = isSavingEdit,
                    errorMessage     = discussionError,
                    onDraftChange    = discussionViewModel::setDraftComment,
                    onPostComment    = {
                        discussionViewModel.postComment(
                            movieId   = item.id,
                            userId    = currentUserId,
                            userEmail = currentUserEmail ?: currentUserId
                        )
                    },
                    onStartEdit      = discussionViewModel::startEditing,
                    onEditTextChange = discussionViewModel::setEditingText,
                    onCancelEdit     = discussionViewModel::cancelEditing,
                    onSaveEdit       = { discussionViewModel.saveEditedComment(item.id) },
                    onDeleteComment  = { commentId ->
                        discussionViewModel.deleteComment(item.id, commentId)
                    },
                    onClearError     = discussionViewModel::clearError
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

// ─────────────────────────────────────────────────────────────────────────────
// DiscussionSection
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun DiscussionSection(
    movieId: Int,
    userId: String,
    userEmail: String,
    comments: List<DiscussionComment>,
    draftComment: String,
    isPosting: Boolean,
    editingCommentId: String?,
    editingText: String,
    isSavingEdit: Boolean,
    errorMessage: String?,
    onDraftChange: (String) -> Unit,
    onPostComment: () -> Unit,
    onStartEdit: (DiscussionComment) -> Unit,
    onEditTextChange: (String) -> Unit,
    onCancelEdit: () -> Unit,
    onSaveEdit: () -> Unit,
    onDeleteComment: (String) -> Unit,
    onClearError: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // ── Section Header ───────────────────────────────────────────────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = AppIcons.Chat,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Community Discussion",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = White
            )
            Spacer(modifier = Modifier.width(8.dp))
            // Count pill badge
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF282828))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "${comments.size}",
                    style = MaterialTheme.typography.labelSmall,
                    color = SubtleText,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Share your thoughts and discuss this title with other fans.",
            style = MaterialTheme.typography.bodySmall,
            color = SubtleText
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ── Post a comment box ───────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(CardBg)
                .border(1.dp, DividerColor, RoundedCornerShape(14.dp))
                .padding(12.dp)
        ) {
            OutlinedTextField(
                value = draftComment,
                onValueChange = {
                    onDraftChange(it)
                    if (errorMessage != null) onClearError()
                },
                placeholder = {
                    Text(
                        "Write a comment...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SubtleText
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                    unfocusedBorderColor = DividerColor,
                    focusedTextColor = White,
                    unfocusedTextColor = White,
                    cursorColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(10.dp),
                textStyle = MaterialTheme.typography.bodyMedium
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onPostComment,
                    enabled = draftComment.isNotBlank() && !isPosting,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = Color(0xFF2C2C2C),
                        disabledContentColor = SubtleText
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isPosting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Posting...", style = MaterialTheme.typography.labelMedium)
                    } else {
                        Icon(
                            imageVector = AppIcons.Send,
                            contentDescription = "Post",
                            modifier = Modifier.size(16.dp),
                            tint = if (draftComment.isNotBlank()) White else SubtleText
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Post Comment", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Comments List ────────────────────────────────────────────────
        if (comments.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardBg.copy(alpha = 0.5f))
                    .padding(vertical = 24.dp, horizontal = 16.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = AppIcons.Chat,
                        contentDescription = null,
                        tint = SubtleText.copy(alpha = 0.5f),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No comments yet.\nBe the first to start the discussion!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SubtleText,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                comments.forEach { comment ->
                    val isEditingThis = editingCommentId == comment.id
                    DiscussionCommentCard(
                        comment = comment,
                        isOwnComment = comment.userId == userId,
                        isEditing = isEditingThis,
                        editingText = if (isEditingThis) editingText else "",
                        isSavingEdit = if (isEditingThis) isSavingEdit else false,
                        onEditTextChange = onEditTextChange,
                        onStartEdit = { onStartEdit(comment) },
                        onCancelEdit = onCancelEdit,
                        onSaveEdit = onSaveEdit,
                        onDelete = { onDeleteComment(comment.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DiscussionCommentCard(
    comment: DiscussionComment,
    isOwnComment: Boolean,
    isEditing: Boolean,
    editingText: String,
    isSavingEdit: Boolean,
    onEditTextChange: (String) -> Unit,
    onStartEdit: () -> Unit,
    onCancelEdit: () -> Unit,
    onSaveEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val cardBackground = if (isOwnComment) Color(0xFF1E1E26) else CardBg
    val cardBorder = if (isOwnComment) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else DividerColor

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(cardBackground)
            .border(1.dp, cardBorder, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // User avatar
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        if (isOwnComment)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        else
                            Color(0xFF2E3238)
                    )
            ) {
                Text(
                    text = comment.displayName.firstOrNull()?.uppercaseChar()?.toString() ?: "U",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isOwnComment) MaterialTheme.colorScheme.primary else Color(0xFFCCCCCC),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = comment.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = White,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (isOwnComment) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "You",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = formatCommentDate(comment.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = SubtleText
                    )
                    if (comment.isEdited) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "(edited)",
                            style = MaterialTheme.typography.labelSmall,
                            color = SubtleText.copy(alpha = 0.7f),
                            fontStyle = FontStyle.Italic
                        )
                    }
                }
            }

            // Edit & Delete actions (Only visible to comment author)
            if (isOwnComment && !isEditing) {
                IconButton(
                    onClick = onStartEdit,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edit comment",
                        tint = SubtleText.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(2.dp))
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete comment",
                        tint = SubtleText.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (isEditing) {
            // Inline comment edit form
            OutlinedTextField(
                value = editingText,
                onValueChange = onEditTextChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = DividerColor,
                    focusedTextColor = White,
                    unfocusedTextColor = White,
                    cursorColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(8.dp),
                textStyle = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onCancelEdit) {
                    Text("Cancel", color = SubtleText)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onSaveEdit,
                    enabled = editingText.isNotBlank() && !isSavingEdit,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    if (isSavingEdit) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp,
                            color = White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Saving...", style = MaterialTheme.typography.labelSmall)
                    } else {
                        Text("Save", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            Text(
                text = comment.text,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFDDDDDD),
                lineHeight = 20.sp
            )
        }
    }
}

private fun formatCommentDate(epochMillis: Long): String {
    if (epochMillis == 0L) return ""
    val diff = System.currentTimeMillis() - epochMillis
    return when {
        diff < 60_000L -> "Just now"
        diff < 3600_000L -> "${diff / 60_000L}m ago"
        diff < 86400_000L -> "${diff / 3600_000L}h ago"
        else -> SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(epochMillis))
    }
}
