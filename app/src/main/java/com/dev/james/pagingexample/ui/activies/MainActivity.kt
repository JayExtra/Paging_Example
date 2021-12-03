package com.dev.james.pagingexample.ui.activies

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import com.dev.james.pagingexample.adapters.ArticlesRecyclerAdapter
import com.dev.james.pagingexample.model.Article
import com.dev.james.pagingexample.ui.activies.databinding.ActivityMainBinding
import com.dev.james.pagingexample.viewmodel.MainActivityViewModel
import com.dev.james.pagingexample.viewmodel.UiAction
import com.dev.james.pagingexample.viewmodel.UiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @ExperimentalPagingApi
    private val mMainActivityViewModel : MainActivityViewModel by viewModels()

    private val articlesRecyclerAdapter : ArticlesRecyclerAdapter = ArticlesRecyclerAdapter()

    private var searchState : Boolean? = null

    private var hasFetchedNewsForFirstTime = false

    private var hasUserSearched = false


    private lateinit var binding : ActivityMainBinding
    @ExperimentalPagingApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        listenToToolBarState()

        binding.bindState(
            uiState = mMainActivityViewModel.uiState,
            pagingData = mMainActivityViewModel.pagingDataFlow,
            uiActions = mMainActivityViewModel.accept
        )
    }

    private fun ActivityMainBinding.bindState(
        uiState : StateFlow<UiState>,
        pagingData: Flow<PagingData<Article>>,
        uiActions: (UiAction) -> Unit
    ){
        recyclerView.adapter = articlesRecyclerAdapter
        recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)

        bindSearch(
            uiState = uiState,
            onQueryChanged = uiActions
        )

        bindList(
            uiState = uiState,
            pagingData = pagingData
        )

    }

    private fun ActivityMainBinding.bindSearch(
        uiState: StateFlow<UiState>,
        onQueryChanged: (UiAction.Search) -> Unit
    ){
        searchInput.setOnEditorActionListener { _, actionId, _ ->
            if(actionId == EditorInfo.IME_ACTION_GO) {
                updateRepoListFromInput(onQueryChanged)
                hideSoftKeyboard()
                true
            }else{
                false
            }
        }

        searchInput.setOnKeyListener { _, keyCode, event ->
            if(event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER){
                updateRepoListFromInput(onQueryChanged)
                hideSoftKeyboard()
                true
            }else{
                false
            }
        }

        searchInput.addTextChangedListener {
            if(it.toString().isEmpty()){
                updateRepoListFromInput(onQueryChanged)
                hideSoftKeyboard()
            }
        }

        lifecycleScope.launch {
            uiState
                .map { it.query }
                .distinctUntilChanged()
                .collect(searchInput::setText)
        }

    }

    private fun ActivityMainBinding.bindList(
        uiState : StateFlow<UiState> ,
        pagingData : Flow<PagingData<Article>>
    ){

        lifecycleScope.launch {
            pagingData.collectLatest(articlesRecyclerAdapter::submitData)
        }

        lifecycleScope.launch {
            articlesRecyclerAdapter.loadStateFlow.collect { loadState ->

                val isEmpty = loadState.refresh is LoadState.NotLoading && articlesRecyclerAdapter.itemCount == 0
                searchNotFoundTxt.isVisible = isEmpty
                searchNotFoundTxt.isInvisible = !isEmpty

                mainActivityProgressBar.isVisible = loadState.mediator?.refresh is LoadState.Loading
                btnRetry.isVisible = loadState.mediator?.refresh is LoadState.Error && articlesRecyclerAdapter.itemCount == 0

                btnRetry.setOnClickListener {
                    articlesRecyclerAdapter.retry()
                }

                val errorState = loadState.source.append as? LoadState.Error
                    ?: loadState.source.prepend as? LoadState.Error
                    ?: loadState.append as? LoadState.Error
                    ?: loadState.prepend as? LoadState.Error

                errorState?.let {
                    Toast.makeText(this@MainActivity,
                        "Woops: ${it.error}",
                        Toast.LENGTH_SHORT)
                        .show()
                }

            }
        }
    }

    private fun ActivityMainBinding.updateRepoListFromInput(onQueryChanged: (UiAction.Search) -> Unit){
        searchInput.text?.trim().let {
            if (it != null) {
                if(it.isNotEmpty()){
                    recyclerView.scrollToPosition(0)
                    onQueryChanged(UiAction.Search(query = it.toString()))

                }
            }
        }
    }

    @ExperimentalPagingApi
    private fun listenToToolBarState() {
        lifecycleScope.launchWhenStarted {
            mMainActivityViewModel.toolBarStateFlow.collectLatest {
                //Toast.makeText(this@MainActivity, "search mode on? $it", Toast.LENGTH_SHORT).show()
                searchState = it
                toggleSearchAndFilterViews(it)
            }
        }
    }

    private fun toggleSearchAndFilterViews(state: Boolean) {
        if(state){
            binding.apply {
                searchInputLayout.isVisible = true
                searchInputLayout.startAnimation(AnimationUtils.loadAnimation(this@MainActivity , R.anim.slide_down))
                filterSearch.isVisible = true
                filterSearch.startAnimation(AnimationUtils.loadAnimation(this@MainActivity , R.anim.slide_down))
            }
        }else{
            binding.apply {
                searchInputLayout.isGone = true
                    searchInputLayout.startAnimation(AnimationUtils.loadAnimation(this@MainActivity , R.anim.slide_up))
                filterSearch.isGone = true
                    filterSearch.startAnimation(AnimationUtils.loadAnimation(this@MainActivity , R.anim.slide_up))
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater : MenuInflater = menuInflater
        inflater.inflate(R.menu.toolbar_menu , menu)
        return true

    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        super.onPrepareOptionsMenu(menu)
        val searchToggle = menu?.findItem(R.id.searchToggle)
        val cancelToggle = menu?.findItem(R.id.cancelToggle)

        if (searchState == true){
            searchToggle?.isVisible = false
            cancelToggle?.isVisible = true
        }else{
            searchToggle?.isVisible = true
            cancelToggle?.isVisible = false
        }
        return true
    }

    @ExperimentalPagingApi
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.searchToggle -> {
                //update ui state to search mode
                changeToolBarState(true)
                invalidateOptionsMenu()
                true
            }
            R.id.cancelToggle ->{
                //update ui state back to normal mode
                changeToolBarState(false)
                invalidateOptionsMenu()
                binding.searchInput.text?.clear()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    @ExperimentalPagingApi
    private fun changeToolBarState(b: Boolean) {
      mMainActivityViewModel.changeToolBarState(b)
    }


    private fun hideSoftKeyboard() {
        val view = this.currentFocus

        view?.let {
            val imm =
                this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }


    }


}