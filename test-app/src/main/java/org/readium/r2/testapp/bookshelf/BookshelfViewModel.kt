/*
 * Copyright 2021 Readium Foundation. All rights reserved.
 * Use of this source code is governed by the BSD-style license
 * available in the top-level LICENSE file of the project.
 */

package org.readium.r2.testapp.bookshelf

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.text.intl.Locale
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import org.readium.r2.shared.util.AbsoluteUrl
import org.readium.r2.shared.util.toUrl
import org.readium.r2.testapp.data.model.Book
import org.readium.r2.testapp.reader.OpeningError
import org.readium.r2.testapp.reader.ReaderActivityContract
import org.readium.r2.testapp.utils.EventChannel

class BookshelfViewModel(application: Application) : AndroidViewModel(application) {

    private val app get() = getApplication<org.readium.r2.testapp.Application>()

    val channel = EventChannel(Channel<Event>(Channel.BUFFERED), viewModelScope)

    //private var _books = MutableStateFlow(emptyList<Book>())
    //val books: StateFlow<List<Book>> get() = _books

    fun getBooks(langCode: String): Flow<List<Book>> {
        return app.bookRepository.books()
    }

    fun deletePublication(book: Book) =
        viewModelScope.launch {
            app.bookshelf.deleteBook(book)
        }

    fun importPublicationFromStorage(uri: Uri) {
        app.bookshelf.importPublicationFromStorage(uri)
    }

    fun addPublicationFromStorage(uri: Uri) {
        app.bookshelf.addPublicationFromStorage(uri.toUrl()!! as AbsoluteUrl)
    }

    fun addPublicationFromWeb(url: AbsoluteUrl) {
        app.bookshelf.addPublicationFromWeb(url)
    }

    fun openPublication(
        bookId: Long
    ) {

        Log.e("readersaba", bookId.toString())
        viewModelScope.launch {
            app.readerRepository
                .open(bookId)
                .onFailure {
                    channel.send(Event.OpenPublicationError(it))
                }
                .onSuccess {
                    val arguments = ReaderActivityContract.Arguments(bookId)
                    channel.send(Event.LaunchReader(arguments))
                }
        }
    }

    sealed class Event {

        class OpenPublicationError(
            val error: OpeningError
        ) : Event()

        class LaunchReader(
            val arguments: ReaderActivityContract.Arguments
        ) : Event()
    }
}
