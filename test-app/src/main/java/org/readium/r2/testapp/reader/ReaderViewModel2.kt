package org.readium.r2.testapp.reader

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch



class ReaderViewModel2(application: android.app.Application) : AndroidViewModel(application){
    private val app get() = getApplication<org.readium.r2.testapp.Application>()
    fun openBook(context: Context, bookId: Long, ):Intent{
        var intent = Intent()
        viewModelScope.launch {
            app.readerRepository
                .open(bookId!!)
                .onFailure {
                    Toast.makeText(context,"Book open failed", Toast.LENGTH_SHORT).show()
                }
                .onSuccess {
                    val arguments = ReaderActivityContract.Arguments(bookId)
                     intent= ReaderActivityContract().createIntent(
                        context,
                        arguments
                    )

                }
        }
        return intent
    }

}