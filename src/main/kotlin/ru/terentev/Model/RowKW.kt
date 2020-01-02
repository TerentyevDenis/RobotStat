package ru.terentev.Model

import java.math.RoundingMode
import java.text.DecimalFormat

class RowKW (var id:Int, var library:String, var name:String, var pass:Int, var fail:Int, variance:Double, mean:Double){
    var assertResult:String = ""
    var varianceform:String
    var meanform:String
    init{
        val df = DecimalFormat("#.####")
        df.setRoundingMode(RoundingMode.CEILING);
        varianceform = df.format(variance)
        meanform = df.format(mean)
    }
}