package jabava.window.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.loadSvgPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowScope
import androidx.compose.ui.window.rememberWindowState
import jabava.screen.ui.MainScreen
import jabava.theme.AsciiTheme
import jabava.widgets.ChibiItem

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
					.background(color = MaterialTheme.colors.secondary)
					.border(width = 2.dp, color = MaterialTheme.colors.primary)
			) {
				AppTapBar(
					onRollUp = {
						windowState.isMinimized = true
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

				MainScreen()
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
		modifier = Modifier.height(32.dp)
	) {
		TopAppBar(
			backgroundColor = MaterialTheme.colors.secondary,
			elevation = 0.dp,
			modifier = Modifier
		) {
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.End,
			) {
				TopBarActionButton(
					icon = painterResource("topbar/top_bar_minimize_icon.svg"),
					modifier = Modifier.padding(end = 2.dp),
					onButtonClick = onRollUp
				)

				TopBarActionButton(
					icon = painterResource("topbar/top_bar_restore_icon.svg"),
					modifier = Modifier.padding(start = 1.dp, end = 1.dp),
					onButtonClick = onExpandClick
				)

				TopBarActionButton(
					icon = painterResource("topbar/top_bar_close_icon.svg"),
					modifier = Modifier.padding(start = 1.dp, end = 2.dp),
					onButtonClick = onCloseClick
				)
			}
		}
	}
}

@Composable
fun TopBarActionButton(
	icon: Painter,
	modifier: Modifier,
	onButtonClick: () -> Unit
) {
	ChibiItem(
		backgroundColor = MaterialTheme.colors.secondary,
		borderWight = 3.dp,
		modifier = modifier
	) {
		Image(
			modifier = Modifier
				.padding(it)
				.clickable { onButtonClick() },
			painter = icon,
			contentDescription = null,
		)
	}
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