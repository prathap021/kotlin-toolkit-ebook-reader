/*
 * Copyright 2021 Readium Foundation. All rights reserved.
 * Use of this source code is governed by the BSD-style license
 * available in the top-level LICENSE file of the project.
 */

package org.readium.r2.testapp.bookshelf

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.Locale
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.readium.r2.shared.util.AbsoluteUrl
import org.readium.r2.testapp.Application
import org.readium.r2.testapp.R
import org.readium.r2.testapp.data.model.Book
import org.readium.r2.testapp.databinding.FragmentBookshelfBinding
import org.readium.r2.testapp.opds.GridAutoFitLayoutManager
import org.readium.r2.testapp.reader.ReaderActivityContract
import org.readium.r2.testapp.utils.viewLifecycle


class BookshelfFragment : Fragment() {

    private inner class OnViewAttachedListener : View.OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(view: View) {
            app.readium.onLcpDialogAuthenticationParentAttached(view)
        }

        override fun onViewDetachedFromWindow(view: View) {
            app.readium.onLcpDialogAuthenticationParentDetached()
        }
    }

    private val bookshelfViewModel: BookshelfViewModel by activityViewModels()
    private lateinit var bookshelfAdapter: BookshelfAdapter
    private lateinit var appStoragePickerLauncher: ActivityResultLauncher<String>
    private lateinit var sharedStoragePickerLauncher: ActivityResultLauncher<Array<String>>
    private var binding: FragmentBookshelfBinding by viewLifecycle()
    private var onViewAttachedListener: OnViewAttachedListener = OnViewAttachedListener()

    private val app: Application
        get() = requireContext().applicationContext as Application

//    val locale = requireContext().applicationContext.getCurrentLocale()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBookshelfBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.addOnAttachStateChangeListener(onViewAttachedListener)

        bookshelfViewModel.channel.receive(viewLifecycleOwner) { handleEvent(it) }

        bookshelfAdapter = BookshelfAdapter(
            onBookClick = { book ->
                book.id?.let {
//                    val locale = applicationContext.getCurrentLocale()
                    Toast.makeText(context, "Lang details: ${book.langCode}", Toast.LENGTH_LONG).show()
                   println("book details is " +book.langCode)
//                    println("App language is --->"+locale)


                    bookshelfViewModel.openPublication(it)
                }
            },
            onBookLongClick = { book -> confirmDeleteBook(book) }
        )

        appStoragePickerLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                uri?.let {
                    bookshelfViewModel.importPublicationFromStorage(it)
                }
            }

        sharedStoragePickerLauncher =
            registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
                uri?.let {
                    val takeFlags: Int = Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    requireContext().contentResolver.takePersistableUriPermission(uri, takeFlags)
                    bookshelfViewModel.addPublicationFromStorage(it)
                }
            }

        binding.bookshelfBookList.apply {
            setHasFixedSize(true)
            layoutManager = GridAutoFitLayoutManager(requireContext(), 120)
            adapter = bookshelfAdapter
            addItemDecoration(
                VerticalSpaceItemDecoration(
                    10
                )
            )
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                bookshelfViewModel.books.collectLatest {
                    bookshelfAdapter.submitList(it)
                }
            }
        }

        binding.bookshelfAddBookFab.setOnClickListener {
            var selected = 0
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.add_book))
                .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                    dialog.cancel()
                }
                .setPositiveButton(getString(R.string.ok)) { _, _ ->
                    when (selected) {
                        0 -> appStoragePickerLauncher.launch("*/*")
                        1 -> sharedStoragePickerLauncher.launch(arrayOf("*/*"))
                        else -> askForRemoteUrl()
                    }
                }
                .setSingleChoiceItems(R.array.documentSelectorArray, 0) { _, which ->
                    selected = which
                }
                .show()
        }
    }

//    @TargetApi(Build.VERSION_CODES.M)
//    private fun getCurrentDefaultLocaleStr(context: Context): String {
//        val locale: Locale = context.getResources().getConfiguration().locale
//        return locale.getDefault().get
//    }

//    fun Context.getCurrentLocale(): Locale {
//        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
//            this.resources.configuration.locales.get(0);
//        } else{
//            this.resources.configuration.locale;
//        }
//    }


    private fun askForRemoteUrl() {
        val urlEditText = EditText(requireContext())
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.add_book))
            .setMessage(R.string.enter_url)
            .setView(urlEditText)
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.cancel()
            }
            .setPositiveButton(getString(R.string.ok)) { _, _ ->
                val url = AbsoluteUrl(urlEditText.text.toString())
                if (url == null || !URLUtil.isValidUrl(urlEditText.text.toString())) {
                    urlEditText.error = getString(R.string.invalid_url)
                    return@setPositiveButton
                }

                bookshelfViewModel.addPublicationFromWeb(url)
            }
            .show()
    }

    private fun handleEvent(event: BookshelfViewModel.Event) {
        when (event) {
            is BookshelfViewModel.Event.OpenPublicationError -> {
                event.error.toUserError().show(requireActivity())
            }

            is BookshelfViewModel.Event.LaunchReader -> {
                val intent = ReaderActivityContract().createIntent(
                    requireContext(),
                    event.arguments
                )
                startActivity(intent)
            }
        }
    }

    class VerticalSpaceItemDecoration(private val verticalSpaceHeight: Int) :
        RecyclerView.ItemDecoration() {

        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            outRect.bottom = verticalSpaceHeight
        }
    }

    private fun deleteBook(book: Book) {
        bookshelfViewModel.deletePublication(book)
    }

    private fun confirmDeleteBook(book: Book) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.confirm_delete_book_title))
            .setMessage(getString(R.string.confirm_delete_book_text))
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.cancel()
            }
            .setPositiveButton(getString(R.string.delete)) { dialog, _ ->
                deleteBook(book)
                dialog.dismiss()
            }
            .show()
    }
}
