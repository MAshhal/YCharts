package co.yml.kmm.charts

import androidx.compose.runtime.Composable

@Composable
internal fun StartScreenIOS(chartType: Int) {
    when (chartType) {
        1 -> BarChartScreen()
        2 -> ChartScreen()
        3 -> LineChartScreen()
    }
}