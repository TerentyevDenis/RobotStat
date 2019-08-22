package ru.terentev.Model.Settings

import javafx.beans.property.SimpleDoubleProperty
import ru.terentev.Controllers.mean
import ru.terentev.Controllers.variance
import ru.terentev.Model.AssertSettings
import ru.terentev.Model.Status
import ru.terentev.Model.StatusTime
import tornadofx.*
import java.math.RoundingMode
import java.text.DecimalFormat

class VarianceAssertSettings(active: Boolean = true, part: Double = 0.2 ) : AssertSettings(active){

    override fun check(list: ArrayList<StatusTime>):String {
        var result:String=""
        val df = DecimalFormat("#.####")
        df.setRoundingMode(RoundingMode.CEILING);
        if (variance(list)>mean(list)*part){
            result=result.plus("Variance  exceed ${df.format(partProperty.value)} mean value, ")
        }
        if (variance(list.filter{ it.status==Status.PASS })>mean(list.filter{ it.status==Status.PASS })*part){
            result=result.plus("Variance of passed tests exceed ${df.format(partProperty.value)} mean value, ")
        }
        if (variance(list.filter{ it.status==Status.FAIL })>mean(list.filter{ it.status==Status.FAIL })*part){
            result=result.plus("Variance of failed tests exceed ${df.format(partProperty.value)} mean value, ")
        }
        return result
    }

    val partProperty = SimpleDoubleProperty(this,"part",part)
    var part by partProperty

}