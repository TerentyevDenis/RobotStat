package ru.terentev.Controllers

import javafx.scene.chart.XYChart
import ru.terentev.Model.*
import tornadofx.*
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.ArrayList


fun makeDataForTimeChart() {
    runLater {
        var list = DBHelper().getTimeSeries(selectedRow.value)
        datafortime.remove(0, datafortime.size)
        for ((i, point) in list.withIndex()) {
            datafortime.add(XYChart.Series<Number, Number>().data(i, point.time, op = {
                runLater {
                    this.node.tooltip("${point.date},duration: ${point.time} s").style(append = true) { fontSize = 12.px }
                    when (point.status) {
                        Status.PASS -> this.node.style(append = true) { backgroundColor += c("#1aaf10") }
                        Status.FAIL -> this.node.style(append = true) { backgroundColor += c("#ef131c", 0.94) }
                    }
                }
            }))
        }
    }
    runLater {
        var list = DBHelper().getTimeSeries(selectedRow.value)
        dataforhist.remove(0, dataforhist.size)
        var t: Int
        when {
            list.size <= 40 -> t = 7
            list.size <= 50 && list.size > 40 -> t = 9
            else -> t = 11
        }

        if (list.isNotEmpty()) {
            var max = Math.ceil(list.maxBy { it.time }!!.time)
            var min = (list.minBy { it.time }!!.time)
            var delta: Double = (max - min) / t

            val df = DecimalFormat("#.####")
            df.setRoundingMode(RoundingMode.CEILING);

            if (delta < 0.01) {
                delta = 0.01
            }
            val result = Array<Int>(t) { i -> 0 }
            for (element in list) {
                result[((element.time - min) / delta).toInt()] += 1
            }
            for (i in result.indices.reversed()) {
                if (result[i] == 0 && result[i - 1] == 0) {
                    result[i] = -1
                } else {
                    break
                }
            }
            for (i in result.indices) {
                if (result[i] != -1) {
                    dataforhist.add(XYChart.Series<String, Number>().data(df.format(i * delta + delta / 2 + min), result[i]))
                }
            }
        }

    }

}
