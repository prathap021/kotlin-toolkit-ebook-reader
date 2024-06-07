/*
 * Copyright 2021 Readium Foundation. All rights reserved.
 * Use of this source code is governed by the BSD-style license
 * available in the top-level LICENSE file of the project.
 */

package org.readium.r2.testapp.reader

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.appcompat.widget.SearchView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.BundleCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.FragmentResultListener
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.fragment.app.commitNow
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import org.readium.r2.navigator.DecorableNavigator
import org.readium.r2.navigator.Decoration
import org.readium.r2.navigator.epub.*
import org.readium.r2.navigator.epub.css.FontStyle
import org.readium.r2.navigator.html.HtmlDecorationTemplate
import org.readium.r2.navigator.html.toCss
import org.readium.r2.navigator.preferences.FontFamily
import org.readium.r2.shared.ExperimentalReadiumApi
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.publication.epub.pageList
import org.readium.r2.testapp.Application
import org.readium.r2.testapp.LITERATA
import org.readium.r2.testapp.R
import org.readium.r2.testapp.bookshelf.BookshelfViewModel
import org.readium.r2.testapp.data.BookRepository
import org.readium.r2.testapp.data.model.Book
import org.readium.r2.testapp.databinding.FragmentBookshelfBinding
import org.readium.r2.testapp.reader.preferences.UserPreferencesViewModel
import org.readium.r2.testapp.search.SearchFragment
import org.readium.r2.testapp.utils.EventChannel
import org.readium.r2.testapp.utils.viewLifecycle
import timber.log.Timber

@OptIn(ExperimentalReadiumApi::class)
class EpubReaderFragment : VisualReaderFragment() {

    override lateinit var navigator: EpubNavigatorFragment

    private lateinit var menuSearch: MenuItem
    lateinit var menuSearchView: SearchView

    private var isSearchViewIconified = true

    private val scope = CoroutineScope(newSingleThreadContext("name"))

    lateinit var viewModel: ReaderViewModel

    override fun onCreate(savedInstanceState: Bundle?) {

        ViewModelProvider(requireActivity())[ReaderViewModel::class.java].let {
            viewModel = it
        }

        if (savedInstanceState != null) {
            isSearchViewIconified = savedInstanceState.getBoolean(IS_SEARCH_VIEW_ICONIFIED)
        }

        val readerData = model.readerInitData as? EpubReaderInitData ?: run {
            // We provide a dummy fragment factory  if the ReaderActivity is restored after the
            // app process was killed because the ReaderRepository is empty. In that case, finish
            // the activity as soon as possible and go back to the previous one.
            childFragmentManager.fragmentFactory = EpubNavigatorFragment.createDummyFactory()
            super.onCreate(savedInstanceState)
            requireActivity().finish()
            return
        }

        childFragmentManager.fragmentFactory =
            readerData.navigatorFactory.createFragmentFactory(
                initialLocator = readerData.initialLocation,
                initialPreferences = readerData.preferencesManager.preferences.value,
                listener = model,
                configuration = EpubNavigatorFragment.Configuration {
                    // To customize the text selection menu.
                    selectionActionModeCallback = customSelectionActionModeCallback

                    // App assets which will be accessible from the EPUB resources.
                    // You can use simple glob patterns, such as "images/.*" to allow several
                    // assets in one go.
                    servedAssets = listOf(
                        // For the custom font Literata.
                        "fonts/.*",
                        // Icon for the annotation side mark, see [annotationMarkTemplate].
                        "annotation-icon.svg"
                    )

                    // Register the HTML templates for our custom decoration styles.
                    decorationTemplates[DecorationStyleAnnotationMark::class] =
                        annotationMarkTemplate()
                    decorationTemplates[DecorationStylePageNumber::class] = pageNumberTemplate()

                    // Declare a custom font family for reflowable EPUBs.
                    addFontFamilyDeclaration(FontFamily.LITERATA) {
                        addFontFace {
                            addSource("fonts/Literata-VariableFont_opsz,wght.ttf")
                            setFontStyle(FontStyle.NORMAL)
                            // Literata is a variable font family, so we can provide a font weight range.
                            setFontWeight(200..900)
                        }
                        addFontFace {
                            addSource("fonts/Literata-Italic-VariableFont_opsz,wght.ttf")
                            setFontStyle(FontStyle.ITALIC)
                            setFontWeight(200..900)
                        }
                    }
                }
            )

        childFragmentManager.setFragmentResultListener(
            SearchFragment::class.java.name,
            this,
            FragmentResultListener { _, result ->
                menuSearch.collapseActionView()
                BundleCompat.getParcelable(
                    result,
                    SearchFragment::class.java.name,
                    Locator::class.java
                )?.let {
                    navigator.go(it)
                }
            }
        )

        super.onCreate(savedInstanceState)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        if (savedInstanceState == null) {
            childFragmentManager.commitNow {
                add(
                    R.id.fragment_reader_container,
                    EpubNavigatorFragment::class.java,
                    Bundle(),
                    NAVIGATOR_FRAGMENT_TAG
                )
            }
        }
        navigator =
            childFragmentManager.findFragmentByTag(NAVIGATOR_FRAGMENT_TAG) as EpubNavigatorFragment

        return view
    }


    @OptIn(ExperimentalMaterialApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        viewModel.channel.receive(viewLifecycleOwner) { handleEvent(it) }


        @Suppress("Unchecked_cast")
        (model.settings as UserPreferencesViewModel<EpubSettings, EpubPreferences>)
            .bind(navigator, viewLifecycleOwner)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Display page number labels if the book contains a `page-list` navigation document.
                (navigator as? DecorableNavigator)?.applyPageNumberDecorations()
            }
        }

        val composeView: ComposeView = requireActivity().findViewById(R.id.overlay)
        composeView.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

        val booksList: MutableList<Book> = mutableListOf<Book>()

        lifecycleScope.launch {

            repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.readerInitData.publication.metadata.identifier?.let {
                    bookRepository.getBooksWithIdentifier(
                        it
                    )
                }?.collectLatest {
                    booksList.clear()
                    booksList.addAll(
                        it.distinctBy { it.langCode }
                    )
                }
            }
        }


        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(
            object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuSearch = menu.findItem(R.id.search).apply {
                        isVisible = true
                        menuSearchView = actionView as SearchView
                    }

                    connectSearch()
                    if (!isSearchViewIconified) menuSearch.expandActionView()
                }


                @SuppressLint("CoroutineCreationDuringComposition")
                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    when (menuItem.itemId) {

                        R.id.search -> {
                            return true
                        }

                        android.R.id.home -> {
                            menuSearch.collapseActionView()
                            return true
                        }

                        R.id.switch_language -> {
                            var isInit = true

                            composeView.visibility = View.VISIBLE
                            composeView.setContent {
                                val bottomSheetState = rememberModalBottomSheetState(
                                    ModalBottomSheetValue.Hidden
                                )
                                val coroutineScope = rememberCoroutineScope()
                                if (isInit) {
                                    if (!bottomSheetState.isVisible) {
                                        coroutineScope.launch {
                                            bottomSheetState.show()

                                            composeView.visibility = View.VISIBLE
                                            isInit = false
                                        }
                                    }
                                }


                                MyModalBottomSheet(
                                    bottomSheetState,
                                    booksList = booksList,
                                    modifier = Modifier.systemBarsPadding()
                                ) {
                                    coroutineScope.launch {
                                        bottomSheetState.hide()

                                        composeView.visibility = View.GONE
                                    }
                                }
                            }


                        }
                    }
                    return false
                }
            },
            viewLifecycleOwner
        )
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
                activity?.finish()
                startActivity(intent)
            }
        }
    }

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    fun MyModalBottomSheet(
        bottomSheetState: ModalBottomSheetState,
        booksList: MutableList<Book>,
        modifier: Modifier,
        bookshelfViewModel: BookshelfViewModel = viewModel(),
        onDismiss: () -> Unit,

        ) {


        ModalBottomSheetLayout(
            modifier = modifier,
            sheetState = bottomSheetState,
            sheetContent = {
                Column(
                    modifier = Modifier,
                ) {
                    Spacer(modifier = Modifier.size(8.dp))
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Choose Language", style = MaterialTheme.typography.h6)
                        IconButton(onClick = { onDismiss.invoke() }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    booksList.forEach {
                        Text(text = it.langCode?.let { it1 -> Locale(it1).displayLanguage } ?: "Unknown Language",
                            fontSize = 16.sp,
                            lineHeight = 20.sp,

                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {

                                    //activity?.finish()

                                    // it.id?.let { it1 -> bookshelfViewModel.openPublication(it1) }


                                    val bookId: Long? = it.id

                                    if (bookId != null) {

                                        viewModel.openPublication(bookId)


//                                        val argument = viewModel.openBook(requireContext(), bookId)
//                                        Log.e("intent", argument.toString())
//
//
//                                        val intent = ReaderActivityContract().createIntent(
//                                            requireContext(),
//                                            argument!!
//                                        )
//                                        startActivity(intent)
//
//                                        Log.e("intent", intent.toString())
                                    }


                                }
                                .padding(vertical = 12.dp, horizontal = 24.dp)

                        )
                        Spacer(modifier = Modifier.size(8.dp))

                    }

                }
            }
        ) {
            // Empty content as we're using this Composable only for the BottomSheet
        }
    }


    /**
     * Will display margin labels next to page numbers in an EPUB publication with a `page-list`
     * navigation document.
     *
     * See http://kb.daisy.org/publishing/docs/navigation/pagelist.html
     */
    private suspend fun DecorableNavigator.applyPageNumberDecorations() {
        val decorations = publication.pageList
            .mapIndexedNotNull { index, link ->
                val label = link.title ?: return@mapIndexedNotNull null
                val locator = publication.locatorFromLink(link) ?: return@mapIndexedNotNull null

                Decoration(
                    id = "page-$index",
                    locator = locator,
                    style = DecorationStylePageNumber(label = label)
                )
            }

        applyDecorations(decorations, "pageNumbers")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(IS_SEARCH_VIEW_ICONIFIED, isSearchViewIconified)
    }

    private fun connectSearch() {
        menuSearch.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {

            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                if (isSearchViewIconified) { // It is not a state restoration.
                    showSearchFragment()
                }

                isSearchViewIconified = false
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                isSearchViewIconified = true
                childFragmentManager.popBackStack()
                menuSearchView.clearFocus()

                return true
            }
        })

        menuSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String): Boolean {
                model.search(query)
                menuSearchView.clearFocus()

                return false
            }

            override fun onQueryTextChange(s: String): Boolean {
                return false
            }
        })

        menuSearchView.findViewById<ImageView>(androidx.appcompat.R.id.search_close_btn)
            .setOnClickListener {
                menuSearchView.requestFocus()
                model.cancelSearch()
                menuSearchView.setQuery("", false)

                (activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.showSoftInput(
                    this.view,
                    0
                )
            }
    }

    private fun showSearchFragment() {
        childFragmentManager.commit {
            childFragmentManager.findFragmentByTag(SEARCH_FRAGMENT_TAG)?.let { remove(it) }
            add(
                R.id.fragment_reader_container,
                SearchFragment::class.java,
                Bundle(),
                SEARCH_FRAGMENT_TAG
            )
            hide(navigator)
            addToBackStack(SEARCH_FRAGMENT_TAG)
        }
    }

    companion object {
        private const val SEARCH_FRAGMENT_TAG = "search"
        private const val NAVIGATOR_FRAGMENT_TAG = "navigator"
        private const val IS_SEARCH_VIEW_ICONIFIED = "isSearchViewIconified"
    }
}

// Examples of HTML templates for custom Decoration Styles.

/**
 * This Decorator Style will display a tinted "pen" icon in the page margin to show that a highlight
 * has an associated note.
 *
 * Note that the icon is served from the app assets folder.
 */
private fun annotationMarkTemplate(@ColorInt defaultTint: Int = Color.YELLOW): HtmlDecorationTemplate {
    val className = "testapp-annotation-mark"
    val iconUrl = checkNotNull(EpubNavigatorFragment.assetUrl("annotation-icon.svg"))
    return HtmlDecorationTemplate(
        layout = HtmlDecorationTemplate.Layout.BOUNDS,
        width = HtmlDecorationTemplate.Width.PAGE,
        element = { decoration ->
            val style = decoration.style as? DecorationStyleAnnotationMark
            val tint = style?.tint ?: defaultTint
            // Using `data-activable=1` prevents the whole decoration container from being
            // clickable. Only the icon will respond to activation events.
            """
            <div><div data-activable="1" class="$className" style="background-color: ${tint.toCss()} !important"/></div>"
            """
        },
        stylesheet = """
            .$className {
                float: left;
                margin-left: 8px;
                width: 30px;
                height: 30px;
                border-radius: 50%;
                background: url('$iconUrl') no-repeat center;
                background-size: auto 50%;
                opacity: 0.8;
            }
            """
    )
}

/**
 * This Decoration Style is used to display the page number labels in the margins, when a book
 * provides a `page-list`. The label is stored in the [DecorationStylePageNumber] itself.
 *
 * See http://kb.daisy.org/publishing/docs/navigation/pagelist.html
 */
private fun pageNumberTemplate(): HtmlDecorationTemplate {
    val className = "testapp-page-number"
    return HtmlDecorationTemplate(
        layout = HtmlDecorationTemplate.Layout.BOUNDS,
        width = HtmlDecorationTemplate.Width.PAGE,
        element = { decoration ->
            val style = decoration.style as? DecorationStylePageNumber

            // Using `var(--RS__backgroundColor)` is a trick to use the same background color as
            // the Readium theme. If we don't set it directly inline in the HTML, it might be
            // forced transparent by Readium CSS.
            """
            <div><span class="$className" style="background-color: var(--RS__backgroundColor) !important">${style?.label}</span></div>"
            """
        },
        stylesheet = """
            .$className {
                float: left;
                margin-left: 8px;
                padding: 0px 4px 0px 4px;
                border: 1px solid;
                border-radius: 20%;
                box-shadow: rgba(50, 50, 93, 0.25) 0px 2px 5px -1px, rgba(0, 0, 0, 0.3) 0px 1px 3px -1px;
                opacity: 0.8;
            }
            """
    )
}
