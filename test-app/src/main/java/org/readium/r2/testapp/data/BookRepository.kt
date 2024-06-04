/*
 * Copyright 2021 Readium Foundation. All rights reserved.
 * Use of this source code is governed by the BSD-style license
 * available in the top-level LICENSE file of the project.
 */

package org.readium.r2.testapp.data

import androidx.annotation.ColorInt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import java.io.File
import java.util.Locale
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.forEach
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.joda.time.DateTime
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.publication.indexOfFirstWithHref
import org.readium.r2.shared.util.Url
import org.readium.r2.shared.util.mediatype.MediaType
import org.readium.r2.testapp.data.db.BooksDao
import org.readium.r2.testapp.data.model.Book
import org.readium.r2.testapp.data.model.Bookmark
import org.readium.r2.testapp.data.model.Highlight
import org.readium.r2.testapp.utils.extensions.readium.authorName
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
class BookRepository(
    private val booksDao: BooksDao
) {
    fun books(): Flow<List<Book>> = booksDao.getAllBooks()


//         fun books(): Flow<List<Book>> {
//          val appLangCode= Locale.getDefault().getLanguage()
//            val flowBooks:Flow<List<Book>> = booksDao.getAllBooks()
//             return booksDao.getAllBooks()
//
//      }




    suspend fun get(id: Long) = booksDao.get(id)

    suspend fun saveProgression(locator: Locator, bookId: Long) =
        booksDao.saveProgression(locator.toJSON().toString(), bookId)

    suspend fun insertBookmark(bookId: Long, publication: Publication, locator: Locator): Long {
        val resource = publication.readingOrder.indexOfFirstWithHref(locator.href)!!
        val bookmark = Bookmark(
            creation = DateTime().toDate().time,
            bookId = bookId,
            resourceIndex = resource.toLong(),
            resourceHref = locator.href.toString(),
            resourceType = locator.mediaType.toString(),
            resourceTitle = locator.title.orEmpty(),
            location = locator.locations.toJSON().toString(),
            locatorText = Locator.Text().toJSON().toString()
        )

        return booksDao.insertBookmark(bookmark)
    }

    fun bookmarksForBook(bookId: Long): Flow<List<Bookmark>> =
        booksDao.getBookmarksForBook(bookId)

    suspend fun deleteBookmark(bookmarkId: Long) = booksDao.deleteBookmark(bookmarkId)

    suspend fun highlightById(id: Long): Highlight? =
        booksDao.getHighlightById(id)

    fun highlightsForBook(bookId: Long): Flow<List<Highlight>> =
        booksDao.getHighlightsForBook(bookId)

    suspend fun addHighlight(
        bookId: Long,
        style: Highlight.Style,
        @ColorInt tint: Int,
        locator: Locator,
        annotation: String
    ): Long =
        booksDao.insertHighlight(Highlight(bookId, style, tint, locator, annotation))

    suspend fun deleteHighlight(id: Long) = booksDao.deleteHighlight(id)

    suspend fun updateHighlightAnnotation(id: Long, annotation: String) {
        booksDao.updateHighlightAnnotation(id, annotation)
    }

    suspend fun updateHighlightStyle(id: Long, style: Highlight.Style, @ColorInt tint: Int) {
        booksDao.updateHighlightStyle(id, style, tint)
    }

    suspend fun insertBook(
        url: Url,
        mediaType: MediaType,
        publication: Publication,
        cover: File
    ): Long {
        val book = Book(
            creation = DateTime().toDate().time,
            title = publication.metadata.title ?: url.filename,
            author = publication.metadata.authorName,
            href = url.toString(),
            identifier = publication.metadata.identifier ?: "",
            mediaType = mediaType,
            progression = "{}",
            cover = cover.path ,
            langCode = publication.metadata.language?.code


        )
        return booksDao.insertBook(book)
    }

    suspend fun deleteBook(id: Long) =
        booksDao.deleteBook(id)
}
