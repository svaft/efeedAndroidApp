package com.example.myapplication

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

enum class Direction { Left, Right, Forward, Backward }

//data class EfeedUiState(
//    var feedDirectionV: Direction = Direction.Left,
//    var outD: Float = 0f,
//    var pauseProgramFlag: Boolean = false,
//    var jogLP: Boolean = false,
//    var threadMode: Boolean = false,
//    var feedDirection: Direction = Direction.Left
//
//)

class MyViewModel(private val state: SavedStateHandle) : ViewModel() {
    // Expose screen UI state
    private val savedStateHandle = state
    val currentFeed: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    val internalCut = MutableLiveData<Int>(0)// 0 - outside turning, 1 - inside turning
    val mMetricUnit = MutableLiveData<Boolean>(true)
    val threadMode = MutableLiveData<Boolean>(false)

    val mDOC = MutableLiveData<String>("0.1")

    val mG90G91 = MutableLiveData<Int>(0) // 0 - absolute distance mode, 1 - incremental distance mode
    val mG94G95 = MutableLiveData<Int>(1) // 0 - unit per min, 1 - unit per rev

    val feedDirection = MutableLiveData<Direction>(Direction.Left)

    // Keep the key as a constant
    companion object {
        private val USER_KEY = "userId"
    }

    fun saveCurrentState(userId: String) {
        // Sets a new value for the object associated to the key.
        savedStateHandle.set(USER_KEY, userId)
    }

    fun getCurrentState(): String {
        // Gets the current value of the user id from the saved state handle
        return savedStateHandle.get(USER_KEY)?: ""
    }
    // Handle business logic
    fun getDOC(): String? {
        return mDOC.value
    }
}