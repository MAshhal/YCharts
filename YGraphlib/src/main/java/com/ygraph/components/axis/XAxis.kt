package com.ygraph.components.axis

import android.graphics.Paint
import android.text.TextPaint
import android.text.TextUtils
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ygraph.components.common.extensions.getTextHeight
import com.ygraph.components.common.extensions.getTextWidth
import com.ygraph.components.common.model.Point
import kotlin.math.ceil

/**
 *
 * XAxis compose method used for drawing xAxis in any given graph.
 * @param axisData : All data needed to draw Yaxis
 * @see com.ygraph.components.axis.AxisData Data class to save all params related to axis
 * @param modifier : All modifier related property.
 * @param xStart : Start position of xAxis Points.
 * @param xLineStart : Start position of xAxis Line.
 * @param scrollOffset : Offset of delta scrolled position.
 * @param zoomScale : Scale at which zoom transformation being applied.
 * @param chartData : List of data points used in the graph.
 */
@Composable
fun XAxis(
    axisData: AxisData,
    modifier: Modifier,
    xStart: Float,
    xLineStart: Float,
    scrollOffset: Float,
    zoomScale: Float,
    chartData: List<Point>
) {
    with(axisData) {
        var xAxisHeight by remember { mutableStateOf(0.dp) }
        Row(modifier = modifier.clipToBounds()) {
            Canvas(
                modifier = modifier
                    .fillMaxWidth()
                    .height(xAxisHeight)
                    .background(backgroundColor)
            ) {
                val (xMin, xMax, xAxisScale) = getXAxisScale(chartData, xAxisSteps)
                var xPos = xStart - scrollOffset

                // used in the case of barchart
                if (xLineStart != xStart) {
                    drawLine(
                        axisLineColor,
                        Offset(xLineStart, 0f),
                        Offset(xPos, 0f),
                        strokeWidth = axisLineThickness.toPx()
                    )
                }
                
                
                for (index in 0..xAxisSteps) {
                    xAxisHeight = drawXAxisLabel(
                        axisData,
                        index,
                        xAxisScale,
                        xPos
                    )
                    drawAxisLineWithPointers(xPos, axisData, zoomScale, xAxisScale)
                    xPos += ((xAxisStepSize.toPx() * (zoomScale * xAxisScale)))
                }
            }
        }
    }
}

private fun DrawScope.drawAxisLineWithPointers(
    xPos: Float,
    axisData: AxisData,
    zoomScale: Float,
    xAxisScale: Float
) {
    with(axisData) {
        if (axisConfig.isAxisLineRequired) {
            drawLine(
                axisLineColor,
                Offset(xPos, 0f),
                Offset(xPos + ((xAxisStepSize.toPx() * (zoomScale * xAxisScale))), 0f),
                strokeWidth = axisLineThickness.toPx()
            )
            drawLine(
                axisLineColor,
                Offset(xPos, 0f),
                Offset(xPos, indicatorLineWidth.toPx()),
                strokeWidth = axisLineThickness.toPx()
            )
        }
    }
}

private fun DrawScope.drawXAxisLabel(
    axisData: AxisData,
    index: Int,
    xAxisScale: Float,
    xPos: Float
): Dp = with(axisData) {
    val calculatedXAxisHeight: Dp
    val xAxisTextPaint = TextPaint().apply {
        textSize = axisLabelFontSize.toPx()
        color = axisLabelColor.toArgb()
        textAlign = Paint.Align.LEFT
        typeface = axisData.typeface
    }
    val xLabel = xLabelData((index * xAxisScale).toInt())
    val labelHeight = xLabel.getTextHeight(xAxisTextPaint)
    val labelWidth = xLabel.getTextWidth(xAxisTextPaint)
    calculatedXAxisHeight =
        if (axisConfig.isAxisLineRequired) {
            labelHeight.toDp() + axisLineThickness +
                    indicatorLineWidth + xLabelAndAxisLinePadding
        } else labelHeight.toDp() + xLabelAndAxisLinePadding
    val ellipsizedText = TextUtils.ellipsize(
        xLabel,
        xAxisTextPaint,
        xAxisStepSize.toPx(),
        axisConfig.ellipsizeAt
    )
    drawContext.canvas.nativeCanvas.apply {
        drawText(
            if (axisConfig.shouldEllipsizeAxisLabel) ellipsizedText.toString() else xLabel,
            xPos - (labelWidth / 2),
            labelHeight / 2 + indicatorLineWidth.toPx() + xLabelAndAxisLinePadding.toPx(),
            xAxisTextPaint
        )
    }
    calculatedXAxisHeight
}

fun getXAxisScale(
    points: List<Point>,
    steps: Int,
): Triple<Float, Float, Float> {
    val xMin = points.takeIf { it.isNotEmpty() }?.minOf { it.x } ?: 0f
    val xMax = points.takeIf { it.isNotEmpty() }?.maxOf { it.x } ?: 0f
    val totalSteps = (xMax - xMin)
    val temp = totalSteps / steps
    val scale = ceil(temp)
    return Triple(xMin, xMax, scale)
}

@Preview(showBackground = true)
@Composable
private fun XAxisPreview() {
    val axisData = AxisData.Builder()
        .xLabelAndAxisLinePadding(10.dp)
        .xAxisPos(Gravity.BOTTOM)
        .axisLabelFontSize(14.sp)
        .xLabelData { index -> index.toString() }
        .build()
    XAxis(
        modifier = Modifier.height(40.dp),
        axisData = axisData,
        xStart = 0f,
        xLineStart = 0f,
        scrollOffset = 0f,
        zoomScale = 1f,
        chartData = listOf()
    )
}