# Applinks automation POC

This script was created to automate the process of adding links to an android app.

Sometimes we were facing a situation when multiple links needed to be added. It took a lot of repetetive work and effort to check whether given path already exists or not, and adding links in specific places, alongside their versions with traing slash and resolution.

Since this is a proof of concept, for AndroidManifest.xml and applinks_array.xml files, that do not require particular link to be added - just a numbered string - this script generates the logs to copy and paste.

For applinks.xml, where most complex changes are required, this script checks whether a given applink already exists, and in that case updates its resolution, or - if it doesn't exist yet - it's added the the bottom of the list for given app flavor.

### How to use

To make use of this script, provide the links array from the Jira ticket in a comma delimited csv file (complete, with titles, as the first line will be skipped). This file should be uploaded to src -> main -> resources directory.

Then upload the applinks.xml file you wish to update. 

Fill in the arguments with paths, names and numbers needed in fun main() and run.
