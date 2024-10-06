package com.exoline.lang.test

import java.io.File

fun getFile(path: String): File = File(InterpreterTest::class.java.classLoader.getResource(path).file)