package com.lifeflow

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

private const val LifeFlowInputMaxLength = 96
private val LifeFlowInputHorizontalPadding = 20.dp
private val LifeFlowInputVerticalPadding = 16.dp
private val LifeFlowTextInputMinHeight = 76.dp
private val LifeFlowTextInputShape = RoundedCornerShape(26.dp)

private val LifeFlowTextInputMaxWidth = LifeFlowHomeButtonMaxWidth * 2 + 40.dp

@Composable
internal fun LifeFlowSoftTextInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    maxLength: Int = LifeFlowInputMaxLength
) {
    BasicTextField(
        value = value,
        onValueChange = { incoming ->
            onValueChange(
                incoming
                    .replace("\r", " ")
                    .replace("\n", " ")
                    .take(maxLength)
            )
        },
        singleLine = true,
        textStyle = lifeFlowInputTextStyle(),
        cursorBrush = SolidColor(LifeFlowButtonInteractiveText),
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = LifeFlowButtonOuterHorizontalPadding,
                vertical = LifeFlowButtonOuterVerticalPadding
            ),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .widthIn(max = LifeFlowTextInputMaxWidth)
                        .fillMaxWidth()
                        .heightIn(min = LifeFlowTextInputMinHeight)
                        .lifeFlowRaisedPanelChrome(LifeFlowTextInputShape)
                        .padding(
                            horizontal = LifeFlowInputHorizontalPadding,
                            vertical = LifeFlowInputVerticalPadding
                        ),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (value.isBlank()) {
                        Text(
                            text = placeholder,
                            style = lifeFlowInputPlaceholderStyle(),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    innerTextField()
                }
            }
        }
    )
}

@Composable
private fun lifeFlowInputTextStyle(): TextStyle =
    lifeFlowButtonTextStyle().copy(
        color = MaterialTheme.colorScheme.onSurface
    )

@Composable
private fun lifeFlowInputPlaceholderStyle(): TextStyle =
    lifeFlowButtonTextStyle().copy(
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
