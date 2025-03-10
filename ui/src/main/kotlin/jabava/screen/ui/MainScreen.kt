package jabava.screen.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import jabava.screen.presentation.ScreenViewModel

@Composable
fun MainScreen() {
	val viewModel = remember { ScreenViewModel() }

	Row(
		modifier = Modifier.fillMaxSize()
	) {
		SettingsMenu(
			onRatioChanged = viewModel::handleRatio,
			onFontSizeChanged = viewModel::handleFontSize
		) {

		}

		PreviewArea()

	}
}

@Composable
fun SettingsMenu(
	onRatioChanged: (Int) -> Unit,
	onFontSizeChanged: (Int) -> Unit,
	onGenerationStart: () -> Unit
) {
	Column(
		modifier = Modifier
			.width(200.dp)
			.padding(start = 10.dp, end = 5.dp)
	) {
		var ratioValue by remember { mutableStateOf(1f) }

		Text("ratio ${ratioValue.toInt()}", color = MaterialTheme.colors.primary)
		Slider(
			value = ratioValue,
			valueRange = 1f..10f,
			steps = 10,
			onValueChange = {
				ratioValue = it
			},
			onValueChangeFinished = {
				onRatioChanged(ratioValue.toInt())
			}
		)

		var fontSizeValue by remember { mutableStateOf(1f) }

		Text("fontSize: ${fontSizeValue.toInt()}", color = MaterialTheme.colors.primary)
		Slider(
			value = fontSizeValue,
			valueRange = 1f..50f,
			steps = 10,
			onValueChange = {
				fontSizeValue = it
			},
			onValueChangeFinished = {
				onFontSizeChanged(fontSizeValue.toInt())
			}
		)

		Button(
			onClick = {}
		) {

		}
	}
}

@Composable
fun PreviewArea() {

	Box(
		modifier = Modifier
			.fillMaxSize()
			.padding(start = 5.dp, end = 5.dp, bottom = 5.dp)
			.border(width = 2.dp, color = MaterialTheme.colors.primary)
			.background(color = Color.White)
	) {

		//Image
	}
}