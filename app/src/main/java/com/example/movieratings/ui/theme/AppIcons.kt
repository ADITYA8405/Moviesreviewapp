package com.example.movieratings.ui.theme

import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Lightweight Material icons to avoid pulling in the heavy material-icons-extended dependency.
 */
object AppIcons {
    val Bookmark: ImageVector
        get() {
            if (_bookmark != null) return _bookmark!!
            _bookmark = materialIcon(name = "Filled.Bookmark") {
                materialPath {
                    moveTo(17.0f, 3.0f)
                    horizontalLineTo(7.0f)
                    curveToRelative(-1.1f, 0.0f, -1.99f, 0.9f, -1.99f, 2.0f)
                    lineTo(5.0f, 21.0f)
                    lineToRelative(7.0f, -3.0f)
                    lineToRelative(7.0f, 3.0f)
                    verticalLineTo(5.0f)
                    curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                    close()
                }
            }
            return _bookmark!!
        }
    private var _bookmark: ImageVector? = null

    val BookmarkBorder: ImageVector
        get() {
            if (_bookmarkBorder != null) return _bookmarkBorder!!
            _bookmarkBorder = materialIcon(name = "Outlined.BookmarkBorder") {
                materialPath {
                    moveTo(17.0f, 3.0f)
                    horizontalLineTo(7.0f)
                    curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                    verticalLineToRelative(16.0f)
                    lineToRelative(7.0f, -3.0f)
                    lineToRelative(7.0f, 3.0f)
                    verticalLineTo(5.0f)
                    curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                    close()
                    moveTo(17.0f, 18.0f)
                    lineToRelative(-5.0f, -2.18f)
                    lineTo(7.0f, 18.0f)
                    verticalLineTo(5.0f)
                    horizontalLineToRelative(10.0f)
                    verticalLineToRelative(13.0f)
                    close()
                }
            }
            return _bookmarkBorder!!
        }
    private var _bookmarkBorder: ImageVector? = null

    val VideoLibrary: ImageVector
        get() {
            if (_videoLibrary != null) return _videoLibrary!!
            _videoLibrary = materialIcon(name = "Filled.VideoLibrary") {
                materialPath {
                    moveTo(4.0f, 6.0f)
                    horizontalLineTo(2.0f)
                    verticalLineToRelative(14.0f)
                    curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                    horizontalLineToRelative(14.0f)
                    verticalLineToRelative(-2.0f)
                    horizontalLineTo(4.0f)
                    verticalLineTo(6.0f)
                    close()
                    moveTo(20.0f, 2.0f)
                    horizontalLineTo(8.0f)
                    curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                    verticalLineToRelative(12.0f)
                    curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                    horizontalLineToRelative(12.0f)
                    curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                    verticalLineTo(4.0f)
                    curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                    close()
                    moveTo(12.0f, 14.5f)
                    verticalLineToRelative(-9.0f)
                    lineToRelative(6.0f, 4.5f)
                    lineToRelative(-6.0f, 4.5f)
                    close()
                }
            }
            return _videoLibrary!!
        }
    private var _videoLibrary: ImageVector? = null

    val RateReview: ImageVector
        get() {
            if (_rateReview != null) return _rateReview!!
            _rateReview = materialIcon(name = "Filled.RateReview") {
                materialPath {
                    moveTo(20.0f, 2.0f)
                    horizontalLineTo(4.0f)
                    curveToRelative(-1.1f, 0.0f, -1.99f, 0.9f, -1.99f, 2.0f)
                    lineTo(2.0f, 22.0f)
                    lineToRelative(4.0f, -4.0f)
                    horizontalLineToRelative(14.0f)
                    curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                    verticalLineTo(4.0f)
                    curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                    close()
                    moveTo(6.0f, 14.0f)
                    verticalLineToRelative(-2.47f)
                    lineToRelative(6.88f, -6.88f)
                    curveToRelative(0.2f, -0.2f, 0.51f, -0.2f, 0.71f, 0.0f)
                    lineToRelative(1.77f, 1.77f)
                    curveToRelative(0.2f, 0.2f, 0.2f, 0.51f, 0.0f, 0.71f)
                    lineTo(8.47f, 14.0f)
                    horizontalLineTo(6.0f)
                    close()
                    moveTo(18.0f, 14.0f)
                    horizontalLineToRelative(-7.5f)
                    lineToRelative(2.0f, -2.0f)
                    horizontalLineTo(18.0f)
                    verticalLineToRelative(2.0f)
                    close()
                }
            }
            return _rateReview!!
        }
    private var _rateReview: ImageVector? = null

    val Chat: ImageVector
        get() {
            if (_chat != null) return _chat!!
            _chat = materialIcon(name = "Filled.Chat") {
                materialPath {
                    moveTo(20.0f, 2.0f)
                    horizontalLineTo(4.0f)
                    curveToRelative(-1.1f, 0.0f, -1.99f, 0.9f, -1.99f, 2.0f)
                    lineTo(2.0f, 22.0f)
                    lineToRelative(4.0f, -4.0f)
                    horizontalLineToRelative(14.0f)
                    curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                    verticalLineTo(4.0f)
                    curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                    close()
                    moveTo(6.0f, 9.0f)
                    horizontalLineToRelative(12.0f)
                    verticalLineToRelative(2.0f)
                    horizontalLineTo(6.0f)
                    verticalLineTo(9.0f)
                    close()
                    moveTo(14.0f, 14.0f)
                    horizontalLineTo(6.0f)
                    verticalLineToRelative(-2.0f)
                    horizontalLineToRelative(8.0f)
                    verticalLineToRelative(2.0f)
                    close()
                    moveTo(18.0f, 8.0f)
                    horizontalLineTo(6.0f)
                    verticalLineTo(6.0f)
                    horizontalLineToRelative(12.0f)
                    verticalLineToRelative(2.0f)
                    close()
                }
            }
            return _chat!!
        }
    private var _chat: ImageVector? = null

    val Send: ImageVector
        get() {
            if (_send != null) return _send!!
            _send = materialIcon(name = "Filled.Send") {
                materialPath {
                    moveTo(2.01f, 21.0f)
                    lineTo(23.0f, 12.0f)
                    lineTo(2.01f, 3.0f)
                    lineTo(2.0f, 10.0f)
                    lineToRelative(15.0f, 2.0f)
                    lineToRelative(-15.0f, 2.0f)
                    close()
                }
            }
            return _send!!
        }
    private var _send: ImageVector? = null
}
