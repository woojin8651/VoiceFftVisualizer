package com.example.live_voice_pitch_representer.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    // TODO: Implement the ViewModel
     var _currentValue: MutableLiveData<Int> = MutableLiveData();
     val currentValue :LiveData<Int>
     get() = _currentValue

    init{
        _currentValue.value = 0
    }
    fun updateValue(input:Int){
    }
}