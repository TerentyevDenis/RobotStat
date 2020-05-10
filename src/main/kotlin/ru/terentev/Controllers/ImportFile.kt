package ru.terentev.Controllers

import java.io.File
import java.io.InputStream
import org.xmlpull.v1.*
import ru.terentev.Model.*
import ru.terentev.view.statusBar
import ru.terentev.view.updateTable
import tornadofx.*
import java.io.FileWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.ArrayList


fun import(dirs: List<File>){
    runAsync(statusBar){
        for (dir in dirs) {
            updateMessage("Uploading ${dir.name}...")
            var list = xmlparser(dir.inputStream())
            var helper = DBHelper()
            helper.progress.addListener(ChangeListener { observable, oldValue, newValue -> updateProgress(newValue.toDouble(),list.first.size.toDouble()+list.second.size.toDouble()) })
            helper.putListInDB(list.first,list.second)
        }
        updateMessage("Updating table...")
        updateProgress(0.4, 1.0)
        updateTable()
        assert()
    }
}

fun export (dir: List<File>){
    runAsync(statusBar){
        updateMessage("Creating ${dir[0].name}...")
        if (selectedRow.value!=-1) {
            var list = DBHelper().getTimeSeries(selectedRow.value)
            FileWriter(dir[0].absolutePath)
                    .use {
                        for (element in list) {
                            it.write(element.time.toString() + "\n")
                        }
                    }
        }
    }
}

fun xmlparser(str: InputStream):Pair<ArrayList<Test>,ArrayList<KeyWord>>{
    var tests:ArrayList<Test> = ArrayList<Test>()
    var keyWords:ArrayList<KeyWord> = ArrayList<KeyWord>()
    var processingKW:ArrayList<KeyWord> = ArrayList<KeyWord>()
    var test: Test? = null
    var suiteName:String?=null

    val factory = XmlPullParserFactory.newInstance()
    factory.isNamespaceAware = true
    val parser = factory.newPullParser()
    parser.setInput(str, null)

    var eventType = parser.eventType
    while (parser.getEventType() != XmlPullParser.END_DOCUMENT) {
        val tagname = parser.name
        when (eventType) {
            XmlPullParser.START_TAG -> if (tagname.equals("suite")){
                suiteName=parser.getAttributeValue(null, "name")
            }else if (tagname.equals("test")){
                test= Test(parser.getAttributeValue(null, "name"))
                test.suiteName=suiteName
            }else if (tagname.equals("status")&&parser.getAttributeValue(null,"critical")!=null){
                if  (parser.getAttributeValue(null,"status").equals("PASS")) test?.status= Status.PASS
                if  (parser.getAttributeValue(null,"status").equals("FAIL")) test?.status= Status.FAIL
                test?.end=(gettime(parser.getAttributeValue(null,"endtime").toString()))
                test?.start=(gettime(parser.getAttributeValue(null,"starttime").toString()))
                test?.counttime()
            }else if (tagname.equals("kw")){
                var library = parser.getAttributeValue(null, "library")
                if (library==null) library=""
                processingKW.add(KeyWord(parser.getAttributeValue(null, "name"), library))
            }else if (tagname.equals("status")&&parser.getAttributeValue(null,"critical")==null){
                if (!processingKW.isEmpty()) {
                    if (parser.getAttributeValue(null, "status").equals("PASS")) processingKW.last()?.status = Status.PASS
                    if (parser.getAttributeValue(null, "status").equals("FAIL")) processingKW.last()?.status = Status.FAIL
                    processingKW.last()?.end = (gettime(parser.getAttributeValue(null, "endtime").toString()))
                    processingKW.last()?.start = (gettime(parser.getAttributeValue(null, "starttime").toString()))
                    processingKW.last()?.counttime()
                }
            }
            XmlPullParser.END_TAG -> if (tagname.equals("test")){
                if (test!=null) {
                    tests.add(test)
                }
            }else if (tagname.equals("kw")) {
                if (!processingKW.isEmpty()) {
                    keyWords.add(processingKW.last())
                    processingKW.remove(processingKW.last())
                }
            }

        }
        eventType = parser.next()
    }
    return Pair(tests,keyWords)
}

fun gettime(s:String): LocalDateTime {
    val formatter = DateTimeFormatter.ofPattern("uuuuMMdd HH:mm:ss.SSS")
    val parsedDate = LocalDateTime.parse(s,formatter)
    return parsedDate
}