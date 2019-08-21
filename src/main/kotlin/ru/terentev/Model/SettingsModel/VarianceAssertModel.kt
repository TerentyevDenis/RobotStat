package ru.terentev.Model.SettingsModel

import ru.terentev.Model.Settings.VarianceAssertSettings
import tornadofx.*

class VarianceAssertModel(orig:VarianceAssertSettings):ItemViewModel<VarianceAssertSettings>(orig){
    val active=bind(VarianceAssertSettings::activeProperty)
    val part=bind(VarianceAssertSettings::partProperty)
}