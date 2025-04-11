package org.example

import java.io.File
import java.io.FileNotFoundException

/**
 * This function logs the required data for applinks_array and AndroidManifest to copy - paste from the terminal.
 * @appFlavor is a string for the required app flavor needing the update, it should be in the same format as in the applinks strings
 * @firstNewLinkNum is the number next to last existing applink
 * @numberOfLinks is the number of links we need to add
 * */
fun printApplinksArrayAndAndroidManifest(
    appFlavor: String,
    firstNewLinkNum: Int,
    numberOfLinks: Int,
) {
    LoggingHelper().logApplinksArray(appFlavor, firstNewLinkNum, numberOfLinks)
    LoggingHelper().logAndroidManifest(appFlavor, firstNewLinkNum, numberOfLinks)
}

/**
 * This one reads the csv file with links to add and loops over the xml to update.
 * @appFlavor is a string for the required app flavor needing the update, it should be in the same format as in the applinks strings
 * @csvFileName is the name of csv file with applinks we need to add. This file should be in src -> main -> resources dir
 * @xmlFilePath is a path to xml file we're looping over (applinks.xml)
 * */
fun uploadAndParseCSV(
    appFlavor: String,
    csvFileName: String,
    xmlFilePath: String,
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
                filePath = xmlFilePath,
                appFlavor = appFlavor,
            )
        } else {
            println("Skipping malformed line: $line")
        }
    }
}

/**
 * Checks whether the link in question already exists in the xml file - if yes updates the resolution to the new one,
 * if not - invokes the insertNewApplink function
 * */
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

    var applinkNumberFound: String? = null

    // Find the first matching applink line and extract the number
    for (line in lines) {
        val regex = """<string .*name="applink_${appFlavor}_(\d+)".*?>(.*?)</string>""".toRegex()
        val match = regex.find(line)
        if (match != null) {
            val number = match.groupValues[1]
            val value = match.groupValues[2]
            if (value == url) {
                applinkNumberFound = number
                println("Exact match found: applink_${appFlavor}_$number for URL '$url'")
                break
            }
        }
    }

    if (applinkNumberFound == null) {
        println("No matching applink line found.")
        val updatedLines = insertNewApplink(
            lines = lines,
            appFlavor = appFlavor,
            newBasePath = url,
            newResolutionLink = newResolution,
        )
        file.writeText(updatedLines.joinToString(System.lineSeparator()))
        return
    }

    // Find and update the resolution line
    val resolutionRegex = ("""(.*name="applink_${appFlavor}_${applinkNumberFound}_resolution".*?>)(.*?)(</string>)""").toRegex()

    var modified = false
    for (i in lines.indices) {
        val line = lines[i]
        if (line.contains("applink_${appFlavor}_${applinkNumberFound}_resolution") && resolutionRegex.containsMatchIn(line)) {
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

/**
 * In a situation when a given applink does not exist in the file - adds it to the list
 * */
fun insertNewApplink(
    lines: MutableList<String>,
    appFlavor: String,
    newBasePath: String,
    newResolutionLink: String,
): List<String> {
    val resolutionPattern = """name="applink_${appFlavor}_(\d+)_resolution"""".toRegex()
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
    val newLinks = listOf(
        """    <string formatted="false" name="applink_${appFlavor}_${newNumber}">$newBasePath</string>""",
        """    <string formatted="false" name="applink_${appFlavor}_${newNumber}_trailing">$trailingPath</string>""",
        """    <string formatted="false" name="applink_${appFlavor}_${newNumber}_resolution">$newResolutionLink</string>""",
    )

    if (lastResolutionIndex >= 0) {
        lines.addAll(lastResolutionIndex + 1, newLinks)
        println("Inserted new applink_${appFlavor}_${newNumber}")
    } else {
        // If no resolution index found at all, append to end?
        lines.addAll(newLinks)
        println("Inserted new applink_${appFlavor}_${newNumber} at end (no prior entries found)")
    }
    return lines
}

fun main(
) {
    uploadAndParseCSV(
        appFlavor = "", // put app flavor here - in a format seen in the applinks.xml
        csvFileName = "", // put your csv file name here
        xmlFilePath = "", // put the full path to the xml file
    )

    printApplinksArrayAndAndroidManifest(
        appFlavor = "", // put app flavor here - in a format seen in the applinks.xml
        firstNewLinkNum = 0, // put first number you need to get results for
        numberOfLinks = 0, // put number of links to add
    )
}
