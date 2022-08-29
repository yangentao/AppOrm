package dev.entao.app

import android.os.Bundle
import android.util.Size
import androidx.appcompat.app.AppCompatActivity
import dev.entao.app.log.*

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //default use logcat output
        logd(1, 2, 3, "abc", "this message will write to logcat")

        //add a DirPrinter.
        //or, We can do this: AppLog.addPrinter(DirPrinter(dirFile))
        AppLog.addDefaultDirPrinter(this, "myLogDir")
        logd("hello file", "this message will write to cache file and logcat")

        //remove printer
        AppLog.printer.remove(LogcatPrinter)
        logd("now, messages will only write to cache file")

        //add console printer
        AppLog.addPrinter(ConsolePrinter)
        logd("Console Printer added")

        AppLog.addFormatter(SizeLogFormatter())
        logd("size: ", Size(100, 50))

        //change default logcat tag
        LogcatPrinter.tagName = "myTagName"

        //disable log
        AppLog.level = LogLevel.DISABLE

        //error message only
        AppLog.level = LogLevel.ERROR
    }
}

class SizeLogFormatter : LogValueFormatter {
    override fun accept(value: Any): Boolean {
        return value is Size
    }

    override fun format(value: Any): String {
        value as Size
        return "Size{ ${value.width}, ${value.height} }"
    }
}