package com.cleanup.go4lunch.ui.mates

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cleanup.go4lunch.data.users.UsersRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MatesViewModel @Inject constructor(
    private val usersRepository: UsersRepository,
) : ViewModel() {

    private val matesListMutableLiveData: MutableLiveData<List<MatesViewState>> = MutableLiveData()
    val matesListLiveData: LiveData<List<MatesViewState>> = matesListMutableLiveData

    fun refreshMatesList(){
        viewModelScope.launch(Dispatchers.IO) {
            val list = usersRepository.getUsers()
            withContext(Dispatchers.Main){
                matesListMutableLiveData.value = list.map {
                    MatesViewState(
                        id = it.id,
                        imageUrl = it.avatarUrl,
                        text = it.firstName
                    )
                }
            }
        }
    }
}
