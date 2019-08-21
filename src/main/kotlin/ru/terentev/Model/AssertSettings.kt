package ru.terentev.Model
import tornadofx.*
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty

open class AssertSettings(active:Boolean=false) {
    val activeProperty = SimpleBooleanProperty(this,"active",active)
    var active by activeProperty
}