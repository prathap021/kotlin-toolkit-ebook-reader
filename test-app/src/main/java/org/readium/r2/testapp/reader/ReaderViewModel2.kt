package org.readium.r2.testapp.reader

import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import org.readium.r2.testapp.Application
import org.readium.r2.testapp.bookshelf.BookshelfViewModel
import org.readium.r2.testapp.utils.EventChannel

class ReaderViewModel2 (application: Application): AndroidViewModel(application){
    private val app get() = getApplication<org.readium.r2.testapp.Application>()
    val channel = EventChannel(Channel<ReaderViewModel2.Event>(Channel.BUFFERED), viewModelScope)

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