package com.example.myapplication

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlin.random.Random

enum class Direction { Left, Right, Forward, Backward }

data class EfeedUiState(

    val firstDieValue: Int? = null,
    val secondDieValue: Int? = null,
    val numberOfRolls: Int = 0,

    var feedDirectionV: Direction = Direction.Left,
    var outD: Float = 0f,
    var pauseProgramFlag: Boolean = false,
    var jogLP: Boolean = false,
    var threadMode: Boolean = false,
    var feedDirection: Direction = Direction.Left

)

class MyViewModel(private val state: SavedStateHandle) : ViewModel() {
    // Expose screen UI state
    private val _uiState = MutableStateFlow(EfeedUiState())
    val uiState: StateFlow<EfeedUiState> = _uiState.asStateFlow()

    // Keep the key as a constant
    companion object {
        private val USER_KEY = "userId"
    }

    private val savedStateHandle = state

    fun saveCurrentState(userId: String) {
        // Sets a new value for the object associated to the key.
        savedStateHandle.set(USER_KEY, userId)
    }

    fun getCurrentState(): String {
        // Gets the current value of the user id from the saved state handle
        return savedStateHandle.get(USER_KEY)?: ""
    }
    // Handle business logic
    fun rollDice() {
        _uiState.update { currentState ->
            currentState.copy(
                firstDieValue = Random.nextInt(from = 1, until = 7),
                secondDieValue = Random.nextInt(from = 1, until = 7),
                numberOfRolls = currentState.numberOfRolls + 1,
                feedDirectionV = Direction.Forward,
                feedDirection = Direction.Left,

                outD = 0f,
                pauseProgramFlag = false
            )
        }
    }

    fun testFromViewModel(){

    }
}