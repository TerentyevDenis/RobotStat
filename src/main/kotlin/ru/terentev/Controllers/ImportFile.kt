package ru.terentev.Controllers
import  ru.terentev.Model.*

import java.io.File
import java.io.InputStream
import org.xmlpull.v1.*
import ru.terentev.Model.Status
import ru.terentev.Model.Test
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.ArrayList


fun import(dir: List<File>){
        DBHelper().putListInDB(xmlparser(dir.first().inputStream()))
}

fun xmlparser(str: InputStream):ArrayList<Test>{
    var tests:ArrayList<Test> = ArrayList<Test>()
    var test: Test? = null

    val factory = XmlPullParserFactory.newInstance()
    factory.isNamespaceAware = true
    val parser = factory.newPullParser()
    parser.setInput(str, null)

    var eventType = parser.eventType
    while (parser.getEventType() != XmlPullParser.END_DOCUMENT) {
        val tagname = parser.name
        when (eventType) {
            XmlPullParser.START_TAG -> if (tagname.equals("test")){
                test= Test(parser.getAttributeValue(null, "name"))
            }else if (tagname.equals("status")&&parser.getAttributeValue(null,"critical")!=null){
                if  (parser.getAttributeValue(null,"status").equals("PASS")) test?.status= Status.PASS
                if  (parser.getAttributeValue(null,"status").equals("FAIL")) test?.status= Status.FAIL
                test?.end=(gettime(parser.getAttributeValue(null,"endtime").toString()))
                test?.start=(gettime(parser.getAttributeValue(null,"starttime").toString()))
                test?.counttime()
            }
            XmlPullParser.END_TAG -> if (tagname.equals("test")){
                if (test!=null) {
                    tests.add(test)
                }
            }

        }
        eventType = parser.next()
    }
    return tests
}

fun gettime(s:String): LocalDateTime {
    val formatter = DateTimeFormatter.ofPattern("uuuuMMdd HH:mm:ss.SSS")
    val parsedDate = LocalDateTime.parse(s,formatter)
    return parsedDate
}