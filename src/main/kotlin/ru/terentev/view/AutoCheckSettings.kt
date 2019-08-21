package ru.terentev.view

import javafx.geometry.Pos
import jdk.nashorn.internal.runtime.PropertyListeners.addListener
import ru.terentev.Model.SettingsModel.VarianceAssertModel
import ru.terentev.Model.varianceAssertSettings
import tornadofx.*
import java.math.RoundingMode
import java.text.DecimalFormat
import javax.swing.GroupLayout

class AutoCheckSettings : View("Assert settings") {
    val modelVariance = VarianceAssertModel(varianceAssertSettings)
    override val root = squeezebox {
        prefWidth = 700.0
        fillHeight = false
        fold("Variance assert",expanded = false) {
            vbox {
                hbox(20) {
                    checkbox("Use this assertion",modelVariance.active)
                    slider(0,1.0,modelVariance.part.value){
                        modelVariance.part.bindBidirectional(valueProperty())
                        isShowTickLabels=true
                        isShowTickMarks=true
                    }
                    label{
                        val df = DecimalFormat("#.####")
                        df.setRoundingMode(RoundingMode.CEILING);
                        text=df.format(modelVariance.part.value)
                        modelVariance.part.addListener(ChangeListener { observable, oldValue, newValue -> text=df.format(newValue) })
                    }
                    label("Max variance a part of mean value") {
                        alignment = Pos.TOP_CENTER
                    }
                }
                buttonbar {
                    button("Save").action {
                        modelVariance.commit()
                    }
                    button("Cancel").action{
                        modelVariance.rollback()
                    }
                }
            }

        }

    }
}

