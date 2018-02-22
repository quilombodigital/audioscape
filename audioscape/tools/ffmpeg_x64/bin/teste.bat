cd F:\mostradearte\audioscape\tools\ffmpeg_x64\bin
ffmpeg -y -v error -f concat -safe 0 -noautorotate -i input.txt -strict experimental -vcodec libx264 -vf hflip,scale=1280:720 texto.mp4