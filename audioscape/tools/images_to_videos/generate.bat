..\ffmpeg_x64\bin\ffmpeg -y -v error -f concat -safe 0 -noautorotate -i input1.txt -strict experimental -vcodec libx264 -vf scale=1280:720 ..\..\videos\instructions.mp4
..\ffmpeg_x64\bin\ffmpeg -y -v error -f concat -safe 0 -noautorotate -i input2.txt -strict experimental -vcodec libx264 -vf scale=1280:720 ..\..\videos\recording.mp4
..\ffmpeg_x64\bin\ffmpeg -y -v error -f concat -safe 0 -noautorotate -i input3.txt -strict experimental -vcodec libx264 -vf scale=1280:720 ..\..\videos\processing.mp4
..\ffmpeg_x64\bin\ffmpeg -y -v error -f concat -safe 0 -noautorotate -i input4.txt -strict experimental -vcodec libx264 -vf scale=1280:720 ..\..\videos\minimumtime.mp4
..\ffmpeg_x64\bin\ffmpeg -y -v error -f concat -safe 0 -noautorotate -i input5.txt -strict experimental -vcodec libx264 -vf scale=1280:720 ..\..\videos\maxtime.mp4
..\ffmpeg_x64\bin\ffmpeg -y -v error -f concat -safe 0 -noautorotate -i input6.txt -strict experimental -vcodec libx264 -vf scale=1280:720 ..\..\videos\approve.mp4
..\ffmpeg_x64\bin\ffmpeg -y -v error -f concat -safe 0 -noautorotate -i input7.txt -strict experimental -vcodec libx264 -vf scale=1280:720 ..\..\videos\attention.mp4