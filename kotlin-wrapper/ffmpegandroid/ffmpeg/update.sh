#!/bin/bash

# to reduce the size of bundle: x86_64 and arm64-v8a are copies of x86 and armeabi-v7a

rm -f ./armeabi-v7a/ffmpeg
rm -f ./armeabi-v7a/libopenh264.so
rm -f ./arm64-v8a/ffmpeg
rm -f ./arm64-v8a/libopenh264.so
rm -f ./x86/ffmpeg
rm -f ./x86/libopenh264.so
rm -f ./x86_64/ffmpeg
rm -f ./x86_64/libopenh264.so

cp -a -p ../../../build/armeabi-v7a/bin/ffmpeg ./armeabi-v7a/ffmpeg
cp -a -p ../../../build/armeabi-v7a/bin/libopenh264.so ./armeabi-v7a/libopenh264.so
cp -a -p ../../../build/armeabi-v7a/bin/ffmpeg ./arm64-v8a/ffmpeg
cp -a -p ../../../build/armeabi-v7a/bin/libopenh264.so ./arm64-v8a/libopenh264.so

cp -a -p ../../../build/x86/bin/ffmpeg ./x86/ffmpeg
cp -a -p ../../../build/x86/bin/libopenh264.so ./x86/libopenh264.so
cp -a -p ../../../build/x86/bin/ffmpeg ./x86_64/ffmpeg
cp -a -p ../../../build/x86/bin/libopenh264.so ./x86_64/libopenh264.so
