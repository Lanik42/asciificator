package jabava.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp
import jabava.theme.FlareColor
import jabava.theme.ShadowColor

@Composable
fun ChibiItem(
	borderWight: Dp,
	backgroundColor: Color,
	topLeftBorderColor: Color = FlareColor,
	bottomRightBorderColor: Color = ShadowColor,
	modifier: Modifier = Modifier,
	content: @Composable (padding: PaddingValues) -> Unit
) {
	val shadowPath = remember { Path() }
	val flarePath = remember { Path() }

	Column(
		verticalArrangement = Arrangement.Center,
		modifier = modifier
			.background(color = backgroundColor)
			.drawBehind {
				if (flarePath.isEmpty) {
					flarePath.moveTo(0f, size.height)
					flarePath.lineTo(borderWight.value, size.height - borderWight.value) // вправо на borderWight вверх на borderWight
					flarePath.lineTo(borderWight.value, borderWight.value)
					flarePath.lineTo(size.width - borderWight.value, borderWight.value)
					flarePath.lineTo(size.width, 0f) // правый верхний угол
					flarePath.lineTo(0f, 0f)
					flarePath.lineTo(0f, size.height)
				}

				drawPath(flarePath, topLeftBorderColor)

				if (shadowPath.isEmpty) {
					shadowPath.moveTo(0f, size.height)
					shadowPath.lineTo(borderWight.value, size.height - borderWight.value) // вправо на borderWight вверх на borderWight
					shadowPath.lineTo(size.width - borderWight.value, size.height - borderWight.value)
					shadowPath.lineTo(size.width - borderWight.value, borderWight.value)
					shadowPath.lineTo(size.width, 0f)
					shadowPath.lineTo(size.width, size.height)
					shadowPath.lineTo(0f, size.height)
				}

				drawPath(shadowPath, bottomRightBorderColor)
			}
	) {
		content(PaddingValues(borderWight))
	}
}