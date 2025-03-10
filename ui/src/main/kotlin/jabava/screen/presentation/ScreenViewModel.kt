package jabava.screen.presentation

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf

class ScreenViewModel {

	private val _state = mutableStateOf(
		ScreenState(
			ratio = 0,
			fontSize = 0,
			colored = false,
			scale = false
		)
	)
	val state: State<ScreenState> = _state

	fun handleRatio(ratio: Int) {
		_state.value = _state.value.copy(ratio = ratio)
	}

	fun handleFontSize(fontSize: Int) {
		_state.value = _state.value.copy(ratio = fontSize)
	}

	fun handleColored(colored: Boolean) {
		_state.value = _state.value.copy(colored = colored)
	}

	fun handleScale(scale: Boolean) {
		_state.value = _state.value.copy(scale = scale)
	}


}