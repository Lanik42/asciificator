package jabava.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import jabava.theme.AsciiTheme

const val APP_NAME = "asciffy-doki"

@Composable
fun AppWindow(
	appState: MutableState<AppState>
) {
	Window(
		state = WindowState(position = WindowPosition(Alignment.Center)),
		// TODO добавить иконку
		//icon = painterResource("ic_app.svg"),
		title = APP_NAME,
		resizable = false,
		undecorated = true,
		onCloseRequest = { },
	) {
		AsciiTheme {
			Column {
				WindowDraggableArea(
					modifier = Modifier.height(20.dp).fillMaxWidth()
				) {
					TopAppBar {
						AppBarMenu(appState)
					}
				}
			}
		}
	}
}

@Composable
fun ApplicationScope.Tray(
	appState: MutableState<AppState>
) {
	Tray(
		tooltip = APP_NAME,
		icon = painterResource("ic_tray_app.svg"),
		onAction = { appState.value = AppState.Open },
		menu = {
			Item(
				"Exit",
				onClick = {
					exitApplication()
				}
			)
		}
	)
}

@Composable
private fun AppBarMenu(appState: MutableState<AppState>) {
	Row(
		modifier = Modifier.fillMaxSize(),
		horizontalArrangement = Arrangement.End
	) {
		Icon(
			imageVector = Icons.Default.Close,
			contentDescription = null,
			tint = MaterialTheme.colors.background,
			modifier = Modifier.clickable { appState.value = AppState.InTray },
		)
	}
}