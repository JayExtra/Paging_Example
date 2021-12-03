package com.dev.james.pagingexample.viewmodel

import androidx.lifecycle.*
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.dev.james.pagingexample.model.Article
import com.dev.james.pagingexample.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalPagingApi
@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val repository : Repository,
    private val savedStateHandle : SavedStateHandle
) : ViewModel() {

    private val _toolBarStateFlow : MutableStateFlow<Boolean> = MutableStateFlow(false)
    val toolBarStateFlow get() = _toolBarStateFlow.asStateFlow()


   val uiState : StateFlow<UiState>

   val pagingDataFlow : Flow<PagingData<Article>>

   val accept: (UiAction) -> Unit

   init {
       val initialQuery: String = savedStateHandle.get<String>(LAST_SEARCH_QUERY) ?: DEFAULT_QUERY

       val actionStateFlow = MutableSharedFlow<UiAction>()

       val searches = actionStateFlow
           .filterIsInstance<UiAction.Search>()
           .distinctUntilChanged()
           .onStart { emit(UiAction.Search(query = initialQuery))}

       pagingDataFlow = searches
           .flatMapLatest { searchRepo(queryString = it.query) }
           .cachedIn(viewModelScope)

        uiState = searches.map { search ->
            UiState(
                query = search.query
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000 ),
            initialValue = UiState()
        )

       accept = { action ->
           viewModelScope.launch { actionStateFlow.emit(action)}
       }

   }

    fun changeToolBarState(state : Boolean) = viewModelScope.launch {
        _toolBarStateFlow.value = state
    }

    override fun onCleared() {
        savedStateHandle[LAST_SEARCH_QUERY] = uiState.value.query
        super.onCleared()
    }

    @ExperimentalPagingApi
    private fun searchRepo(queryString : String) : Flow<PagingData<Article>> =
            repository.getResultStream(queryString).cachedIn(viewModelScope)


}



sealed class UiAction {
    data class Search(val query : String) : UiAction()
}

data class UiState(
    val query : String = DEFAULT_QUERY,
    val pagingData : PagingData<Article> = PagingData.empty()
)

private const val LAST_SEARCH_QUERY = "last_search_query"
private const val  DEFAULT_QUERY = ""
