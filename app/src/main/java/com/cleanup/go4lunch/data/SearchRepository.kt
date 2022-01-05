package com.cleanup.go4lunch.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchRepository @Inject constructor() {

    private val searchMutableStateFlow = MutableStateFlow<String?>(null)
    val searchStateFlow: StateFlow<String?> = searchMutableStateFlow.asStateFlow()

    fun setSearchTerms(terms: String?) {
        // we could make terms smallCase, reduce UTF to ascii, split to words, strip blanks...
        searchMutableStateFlow.value = terms
    }

}


