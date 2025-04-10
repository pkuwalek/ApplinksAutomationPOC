package org.example

import java.io.File
import java.io.FileNotFoundException

fun printApplinksArrayAndAndroidManifest(
    appFlavor: String,
    firstNum: Int,
    numberOfLinks: Int,
) {
    LoggingHelper().logApplinksArray(appFlavor, firstNum, numberOfLinks)
    LoggingHelper().logAndroidManifest(appFlavor, firstNum, numberOfLinks)
}

fun uploadAndParseCSV(
    appFlavor: String,
    csvFileName: String,
) {
    val inputStream = {}::class.java.getResourceAsStream("/${csvFileName}.csv")
        ?: throw FileNotFoundException("Resource not found")

    val lines = inputStream.bufferedReader().use { it.readLines() }

    for (line in lines.drop(1)) {
        val columns = line.split(",")

        if (columns.size >= 3) {
            val link = columns[1].trim()
            val resolution = columns[2].trim()

            updateXmlWithNewResolutionLink(
                url = link.removeUrlPrefix(),
                newResolution = resolution,
                filePath = "updated_applinks.xml",
                appFlavor = appFlavor,
            )
        } else {
            println("Skipping malformed line: $line")
        }
    }
}

fun updateXmlWithNewResolutionLink(
    url: String,
    newResolution: String,
    filePath: String,
    appFlavor: String,
) {
    val file = File(filePath)
    if (!file.exists()) {
        throw FileNotFoundException("File not found: $filePath")
    }
    val lines = file.readLines().toMutableList()

    var numberFound: String? = null

    // Find the first matching applink line and extract the number
    for (line in lines) {
        val regex = """<string .*name="applink_${appFlavor}_(\d+)".*?>(.*?)</string>""".toRegex()
        val match = regex.find(line)
        if (match != null) {
            val number = match.groupValues[1]
            val value = match.groupValues[2]
            if (value == url) {
                numberFound = number
                println("Exact match found: applink_${appFlavor}_$number for URL '$url'")
                break
            }
        }
    }

    if (numberFound == null) {
        println("No matching applink line found.")
        val updatedLines = insertNewApplink(
            lines = lines,
            group = appFlavor,
            newBasePath = url,
            newResolutionLink = newResolution,
        )
        file.writeText(updatedLines.joinToString(System.lineSeparator()))
        return
    }

    // Find and update the resolution line
    val resolutionRegex = ("""(.*name="applink_${appFlavor}_${numberFound}_resolution".*?>)(.*?)(</string>)""").toRegex()

    var modified = false
    for (i in lines.indices) {
        val line = lines[i]
        if (line.contains("applink_${appFlavor}_${numberFound}_resolution") && resolutionRegex.containsMatchIn(line)) {
            val newLine = resolutionRegex.replace(line) {
                val prefix = it.groupValues[1]
                val suffix = it.groupValues[3]
                "$prefix$newResolution$suffix"
            }
            lines[i] = newLine
            println("Updated line: $newLine")
            modified = true
            break
        }
    }

    if (!modified) {
        println("No resolution line found to update.")
        return
    }

    // Write back to a file
    file.writeText(lines.joinToString(System.lineSeparator()))
    println("Updated resolution in file: $filePath")
}

fun insertNewApplink(
    lines: MutableList<String>,
    group: String,
    newBasePath: String,
    newResolutionLink: String,
): List<String> {
    val resolutionPattern = """name="applink_${group}_(\d+)_resolution"""".toRegex()
    var maxNumber = -1
    var lastResolutionIndex = -1

    // Find the highest number for the group
    for ((i, line) in lines.withIndex()) {
        val match = resolutionPattern.find(line)
        if (match != null) {
            val number = match.groupValues[1].toInt()
            if (number > maxNumber) {
                maxNumber = number
                lastResolutionIndex = i
            }
        }
    }

    val newNumber = maxNumber + 1
    val trailingPath = "$newBasePath/"
    val newLines = listOf(
        """    <string formatted="false" name="applink_${group}_${newNumber}">$newBasePath</string>""",
        """    <string formatted="false" name="applink_${group}_${newNumber}_trailing">$trailingPath</string>""",
        """    <string formatted="false" name="applink_${group}_${newNumber}_resolution">$newResolutionLink</string>""",
    )

    if (lastResolutionIndex >= 0) {
        lines.addAll(lastResolutionIndex + 1, newLines)
        println("Inserted new applink_${group}_${newNumber}")
    } else {
        // If no resolution index found at all, append to end?
        lines.addAll(newLines)
        println("Inserted new applink_${group}_${newNumber} at end (no prior entries found)")
    }
    return lines
}

fun main(
) {
    uploadAndParseCSV(
        appFlavor = "BE_JET",
        csvFileName = "Arkusz1"
    )

    printApplinksArrayAndAndroidManifest(
        appFlavor = "BE_JET",
        firstNum = 99,
        numberOfLinks = 116,
    )
}
