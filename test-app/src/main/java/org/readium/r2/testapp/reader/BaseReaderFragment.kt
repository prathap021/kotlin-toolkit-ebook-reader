/*
 * Copyright 2021 Readium Foundation. All rights reserved.
 * Use of this source code is governed by the BSD-style license
 * available in the top-level LICENSE file of the project.
 */

package org.readium.r2.testapp.reader

import android.net.Uri

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import org.readium.r2.lcp.lcpLicense
import org.readium.r2.navigator.Navigator
import org.readium.r2.navigator.preferences.Configurable
import org.readium.r2.shared.ExperimentalReadiumApi
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.publication.Publication
import org.readium.r2.testapp.R
import org.readium.r2.testapp.reader.preferences.MainPreferencesBottomSheetDialogFragment
import org.readium.r2.testapp.utils.UserError
import org.readium.r2.testapp.utils.launchWebBrowser
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.Observer
import org.readium.r2.testapp.chat.ChatActivity
import org.readium.r2.testapp.data.model.Book

//import android.app.AlertDialog


/*
 * Base reader fragment class
 *
 * Provides common menu items and saves last location on stop.
 */
@OptIn(ExperimentalReadiumApi::class)
abstract class BaseReaderFragment : Fragment() {

    var buyUrl: String = "https://www.amazon.in/s?k=metropolis"


    val model: ReaderViewModel by activityViewModels()
    protected val publication: Publication get() = model.publication

    protected abstract val navigator: Navigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        model.fragmentChannel.receive(this) { event ->
            fun toast(id: Int) {
                Toast.makeText(requireContext(), getString(id), Toast.LENGTH_SHORT).show()
            }

            when (event) {
                is ReaderViewModel.FragmentFeedback.BookmarkFailed -> toast(
                    R.string.bookmark_exists
                )

                is ReaderViewModel.FragmentFeedback.BookmarkSuccessfullyAdded -> toast(
                    R.string.bookmark_added
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        model.book.observe(viewLifecycleOwner, Observer { book ->
            book?.let {
//                Toast.makeText(context, book.toString(), Toast.LENGTH_LONG).show()
                if (
                    book.identifier == "60" || book.identifier == "59"
                ) {
                    context?.startActivity(
                        Intent(context, ChatActivity::class.java).apply {
                            putExtra("bookId", book.id.toString())
                        }
                    )
                } else {
                    Toast.makeText(context, "Chat not available", Toast.LENGTH_SHORT).show()
                }
            }
        })


        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(
            object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.menu_reader, menu)

                    menu.findItem(R.id.settings).isVisible =
                        navigator is Configurable<*, *>

                    menu.findItem(R.id.drm).isVisible =
                        model.publication.lcpLicense != null
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    when (menuItem.itemId) {
                        R.id.toc -> {
                            model.activityChannel.send(
                                ReaderViewModel.ActivityCommand.OpenOutlineRequested
                            )
                            return true
                        }

                        R.id.bookmark -> {
                            model.insertBookmark(navigator.currentLocator.value)
                            return true
                        }
                        // prathap added buy button 13-03-2024
                        R.id.purchase -> {
                            val context: Context = requireContext()

                            context?.let { value -> launchWebBrowser(context, Uri.parse(buyUrl)) }
                            return true
                        }

                        R.id.chatAi -> {
                            // chat api working block

                            val bookId = model.readerInitData.bookId

                            model.getBook(requireContext(), bookId)


//                            model.book.observe(viewLifecycleOwner, Observer { book ->
//                                Toast.makeText(context, book.toString(), Toast.LENGTH_LONG).show()
//
//                                if(book.title == "Indus Valley Civilization – A Land of the ancient Dravidians"){
//                                    context?.startActivity(
//                                        Intent(context, ChatActivity::class.java).apply {
//                                            putExtra("bookId", bookId.toString())
//                                        }
//                                    )
//                                }else{
//                                    Toast.makeText(context, "Chat not available", Toast.LENGTH_SHORT).show()
//                                }
//
//
//                            })


                            return true
                        }
//                        R.id.infoBook -> {
//                            println("book details is --> "+ model.publication
//                            +model.readerInitData)
//                            val context: Context = requireContext()
//                            showAlert(context,"book info","")
//                            return true
//                        }
                        R.id.settings -> {
                            MainPreferencesBottomSheetDialogFragment()
                                .show(childFragmentManager, "Settings")
                            return true
                        }

                        R.id.drm -> {
                            model.activityChannel.send(
                                ReaderViewModel.ActivityCommand.OpenDrmManagementRequested
                            )
                            return true
                        }


                    }
                    return false
                }
            },
            viewLifecycleOwner
        )
    }


    fun showAlert(context: Context, title: String, message: String) {
        // Create a builder for the alert dialog
        val builder = AlertDialog.Builder(context)

        // Set the dialog title and message
        builder.setTitle(title)
            .setMessage(message)

        // Set a button for the positive action (e.g., OK)
        builder.setPositiveButton("OK") { dialog, which ->
            // Do something when the positive button is clicked
            // For example, dismiss the dialog
            dialog.dismiss()
        }

        // Create and show the alert dialog
        val dialog = builder.create()
        dialog.show()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        setMenuVisibility(!hidden)
        requireActivity().invalidateOptionsMenu()
    }

    open fun go(locator: Locator, animated: Boolean) {
        navigator.go(locator, animated)
    }

    protected fun showError(error: UserError) {
        val activity = activity ?: return
        error.show(activity)
    }
}
