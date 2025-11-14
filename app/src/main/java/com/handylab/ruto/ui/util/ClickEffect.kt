package com.handylab.ruto.ui.util

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * 클릭 시, 발생하는 애니메이션 이펙트를 구현한 파일
 */
enum class ButtonState { Pressed, Idle }

fun Modifier.bounceClick(
    onClick: () -> Unit,
    hapticOnPress: HapticFeedbackType = HapticFeedbackType.Confirm, // 진동 타입 설정
    hapticOnRelease: HapticFeedbackType? = null // 해제 시도 원하면 지정
) = composed {
    var buttonState by remember { mutableStateOf(ButtonState.Idle) }
    val scale by animateFloatAsState(
        targetValue = if (buttonState == ButtonState.Pressed) 0.70f else 1f,
        label = "bounce-scale"
    )
    val haptics = LocalHapticFeedback.current

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = { onClick.invoke() }
        )
        .pointerInput(buttonState) {
            awaitPointerEventScope {
                if (buttonState == ButtonState.Pressed) {
                    // 손가락 떼는 순간(업) 처리
                    waitForUpOrCancellation()
                    if (hapticOnRelease != null) {
                        haptics.performHapticFeedback(hapticOnRelease)
                    }
                    buttonState = ButtonState.Idle
                } else {
                    // 손가락 누른 순간(다운) 처리
                    awaitFirstDown(requireUnconsumed = false)
                    haptics.performHapticFeedback(hapticOnPress)
                    buttonState = ButtonState.Pressed
                }
            }
        }

}
