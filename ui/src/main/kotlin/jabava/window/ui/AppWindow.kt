package jabava.window.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowScope
import androidx.compose.ui.window.rememberWindowState
import jabava.theme.AsciiTheme
import jabava.theme.Close
import jabava.theme.Expand
import jabava.theme.RollUp
import jabava.theme.TextMain

const val APP_NAME = "asciffy-doki"

@Composable
fun AppWindow(
	appState: MutableState<AppState>,
	exitApplication: () -> Unit,
) {
	val windowState = rememberWindowState(position = WindowPosition(Alignment.Center))

	Window(
		state = windowState,
		// TODO добавить иконку
		//icon = painterResource("ic_app.svg"),
		title = APP_NAME,
		resizable = false,
		undecorated = true,
		onCloseRequest = { },
	) {
		AsciiTheme {
			Column(
				modifier = Modifier
					.fillMaxSize()
					.background(color = MaterialTheme.colors.primary)
			) {
				AppTapBar(
					onRollUp = {

					},
					onExpandClick = {
						// тут надо добавить анимации разворачивания
						if (windowState.placement == WindowPlacement.Floating) {
							windowState.placement = WindowPlacement.Fullscreen
						} else {
							windowState.placement = WindowPlacement.Floating
						}
					},
					onCloseClick = { exitApplication() /*appState.value = AppState.InTray*/ } //Уводить в трей если идет загрузка
				)
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
fun WindowScope.AppTapBar(
	onRollUp: () -> Unit,
	onExpandClick: () -> Unit,
	onCloseClick: () -> Unit,
) {
	WindowDraggableArea(
		modifier = Modifier.height(20.dp)
	) {
		TopAppBar(
			backgroundColor = MaterialTheme.colors.primary, // Поменять цвет
			modifier = Modifier
		) {
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.End,
			) {
				TopBarActionButton(
					icon = painterResource("topbar/top_bar_roll_up_icon.svg"), backgroundColor = RollUp,
					modifier = Modifier.padding(end = 5.dp),
					onButtonClick = onRollUp
				)

				TopBarActionButton(
					icon = painterResource("topbar/top_bar_extend_icon.svg"), backgroundColor = Expand,
					modifier = Modifier.padding(start = 5.dp, end = 5.dp),
					onButtonClick = onExpandClick
				)

				TopBarActionButton(
					icon = painterResource("topbar/top_bar_close_icon.svg"), backgroundColor = Close,
					modifier = Modifier.padding(start = 5.dp, end = 5.dp),
					onButtonClick = onCloseClick
				)
			}
		}
	}
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TopBarActionButton(
	icon: Painter,
	backgroundColor: Color,
	modifier: Modifier,
	onButtonClick: () -> Unit
) {
	var buttonActive by remember { mutableStateOf(false) }

	Icon(
		modifier = modifier
			.size(12.dp)
			.drawBehind { drawCircle(color = backgroundColor, radius = 7.5f) }
			.clickable { onButtonClick() }
			.onPointerEvent(PointerEventType.Enter) { buttonActive = true }
			.onPointerEvent(PointerEventType.Exit) { buttonActive = false },
		painter = if (buttonActive) {
			icon
		} else {
			painterResource("topbar/top_bar_empty_icon.svg")
		},
		tint = TextMain, // Заменить цвет
		contentDescription = null,
	)
}

//@Composable
//private fun AppBarMenu(appState: MutableState<AppState>) {
//	Row(
//		modifier = Modifier.fillMaxSize(),
//		horizontalArrangement = Arrangement.End
//	) {
//		Icon(
//			imageVector = Icons.Default.Close,
//			contentDescription = null,
//			tint = MaterialTheme.colors.background,
//			modifier = Modifier.clickable { appState.value = AppState.InTray },
//		)
//	}
//}