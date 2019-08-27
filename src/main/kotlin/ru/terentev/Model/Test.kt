package ru.terentev.Model

import java.time.LocalDateTime
import java.time.ZoneOffset

class Test(var name:String){
    var suiteName:String?=null
    var status: Status?= null
    var start:LocalDateTime?=null
    var end:LocalDateTime?=null
    var time:Double?=null
    fun counttime(){
        if (start!=null &&end!=null)
        time = (end!!.atZone(ZoneOffset.UTC).toInstant().toEpochMilli()-start!!.atZone(ZoneOffset.UTC).toInstant().toEpochMilli())/1000.0
    }
}

