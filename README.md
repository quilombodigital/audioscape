# AUDIOSCAPE
Generative Art project for an exhibition at [OSítio](https://ositio.com.br/#).

**This is a work in progress, it is not complete yet, but it will be in a few weeks. The core code is complete and functional. It is missing user interface.**

## Description ##
This project records the user telling a story, makes the transcription using Google speech API, extract text emotion using Microsoft Azure text analysis, grabs images connected to these words using google search, and creates a resulting video using javacv and ffmpeg, that mixes all this information.

## Requirements ##

This instructions will change soon, because currently I am executing it using the Intellij IDE, and I dont have yet an install package, but these are the minimum requirements:

1. HD webcam.
2. Good microphone (Shure)
2. Make sure you have at least Java JDK 8.
2. OpenCV 3.4.0. (at environment path).
3. [Visual C++ Redistributable for Visual Studio 2015](https://www.microsoft.com/en-us/download/details.aspx?id=48145).
4. Copy google credential file to keys/ directory, and use GOOGLE\_APPLICATION\_CREDENTIALS to point to it.
5. Create Microsoft Text analysis configuration at keys/text_analysis.json, with the contents:

    	{
    	  "accessKey": "<your access key here>",
    	  "host": "https://westcentralus.api.cognitive.microsoft.com",
    	  "sentimentPath": "/text/analytics/v2.0/sentiment",
    	  "keyPhrasePath": "/text/analytics/v2.0/keyPhrases"
    	}


## TODO ##

* Create minimum install package and .bat file
* Use videos with text as interface
* Delete sessions when there is an error
* Make interface to hardware button
* Make interface to user detection (arduino+distance)
* Better video mix, maybe overlay with user record beside slides
* Study to use two monitors

Any doubts just drop me a line, like I said, this is a work in progress...

Thanks,

Ricardo Andere de Mello (gandhi)
