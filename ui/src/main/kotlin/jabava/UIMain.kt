package jabava

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.application
import jabava.ui.AppState
import jabava.ui.AppWindow
import jabava.ui.Tray

fun main() {
	val appInstanceController = AppInstanceController()

	val appRegistered = appInstanceController.registerInstance()

	if (appRegistered) {
		startApp(appInstanceController)
	} else {
		уебатьДругойЭкземпляр()
	}
}

fun startApp(appInstanceController: AppInstanceController) {
	application {
		val appState = remember { mutableStateOf<AppState>(AppState.Open) }

		if (appState.value == AppState.Open)
			AppWindow(appState)
		else
			Tray(appState)

		onExit {
			appInstanceController.unregisterInstance()
		}
	}
}

context(ApplicationScope)
@Composable
private fun onExit(onExit: () -> Unit) {
	DisposableEffect(true) {
		onDispose {
			onExit()
		}
	}
}

fun уебатьДругойЭкземпляр() {

}

