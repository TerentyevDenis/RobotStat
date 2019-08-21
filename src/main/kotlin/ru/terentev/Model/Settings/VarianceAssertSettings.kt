package ru.terentev.Model.Settings

import javafx.beans.property.SimpleDoubleProperty
import ru.terentev.Model.AssertSettings
import tornadofx.*

class VarianceAssertSettings(active: Boolean = true, part: Double = 0.2 ) : AssertSettings(active){
    val partProperty = SimpleDoubleProperty(this,"active",part)
    var part by partProperty
}