package com.burgardt.german;


import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.getcapacitor.JSObject;
import com.getcapacitor.NativePlugin;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

@NativePlugin()
public class CustomNativePlugin extends Plugin {

  @PluginMethod()
  public void customCall(PluginCall call, Context myContext) {
    // Parametros??
    String message = call.getString("value");


    /* PROBAR PASAR LOS PATH ASÍ
    fun selectMediaToPlay(source: Source): Uri {
        return when (source) {
            Source.local_audio -> Uri.parse("asset:///audio/file.mp3")
            Source.local_video -> Uri.parse("asset:///video/file.mp4")
            Source.http_audio -> Uri.parse("http://site.../file.mp3")
            Source.http_video -> Uri.parse("http://site.../file.mp4")
        }
    }

    You can load files from assets in the following ways (you can create nested folders under the assets folder):

    Uri.parse(“file:///android_asset/video/video.mp4”)
    Uri.parse(“asset:///video/video.mp4”)
    Please note that:

    You can use RawResourceDataSource to build a Uri that points to a resource id in the res folder, eg: RawResourceDataSource.buildRawResourceUri(R.raw.my_media_file).

    * */

    JSObject muxResponse = muxingVideoWithAudio(myContext, "test", "test");
    call.success(muxResponse);
  }

    private JSObject muxingVideoWithAudio(
        Context myContext,
        String audioFilePath,
        String pathVideoAssets
    ) {

        String outputFile = "";

        try {
            // Crea el archivo
            File file = new File(Environment.getExternalStorageDirectory() + File.separator + "resultadoVideo.mp4");
            file.createNewFile();
            outputFile = file.getAbsolutePath();

            MediaExtractor videoExtractor = new MediaExtractor();

            AssetFileDescriptor afdd = myContext.getAssets().openFd(pathVideoAssets);
            videoExtractor.setDataSource(afdd.getFileDescriptor() ,afdd.getStartOffset(),afdd.getLength());

            MediaExtractor audioExtractor = new MediaExtractor();
            audioExtractor.setDataSource(audioFilePath);

            Log.d("TagMenssage: ", "Video Extractor Track Count " + videoExtractor.getTrackCount() );
            Log.d("TagMenssage: ", "Audio Extractor Track Count " + audioExtractor.getTrackCount() );

            MediaMuxer muxer = new MediaMuxer(outputFile, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

            videoExtractor.selectTrack(0);
            MediaFormat videoFormat = videoExtractor.getTrackFormat(0);
            int videoTrack = muxer.addTrack(videoFormat);

            audioExtractor.selectTrack(0);
            MediaFormat audioFormat = audioExtractor.getTrackFormat(0);
            int audioTrack = muxer.addTrack(audioFormat);

            Log.d("TagMenssage: ", "Video Format " + videoFormat.toString() );
            Log.d("TagMenssage: ", "Audio Format " + audioFormat.toString() );

            boolean sawEOS = false;
            int frameCount = 0;
            int offset = 100;
            int sampleSize = 256 * 1024;
            ByteBuffer videoBuf = ByteBuffer.allocate(sampleSize);
            ByteBuffer audioBuf = ByteBuffer.allocate(sampleSize);
            MediaCodec.BufferInfo videoBufferInfo = new MediaCodec.BufferInfo();
            MediaCodec.BufferInfo audioBufferInfo = new MediaCodec.BufferInfo();


            videoExtractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
            audioExtractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC);

            muxer.start();

            while (!sawEOS)
            {
                videoBufferInfo.offset = offset;
                videoBufferInfo.size = videoExtractor.readSampleData(videoBuf, offset);


                if (videoBufferInfo.size < 0 || audioBufferInfo.size < 0)
                {
                    Log.d("TagMenssage: ", "saw input EOS.");
                    sawEOS = true;
                    videoBufferInfo.size = 0;

                }
                else
                {
                    videoBufferInfo.presentationTimeUs = videoExtractor.getSampleTime();
                    videoBufferInfo.flags = videoExtractor.getSampleFlags();
                    muxer.writeSampleData(videoTrack, videoBuf, videoBufferInfo);
                    videoExtractor.advance();


                    frameCount++;
                    Log.d("TagMenssage: ", "Frame (" + frameCount + ") Video PresentationTimeUs:" + videoBufferInfo.presentationTimeUs +" Flags:" + videoBufferInfo.flags +" Size(KB) " + videoBufferInfo.size / 1024);
                    Log.d("TagMenssage: ", "Frame (" + frameCount + ") Audio PresentationTimeUs:" + audioBufferInfo.presentationTimeUs +" Flags:" + audioBufferInfo.flags +" Size(KB) " + audioBufferInfo.size / 1024);

                }
            }


//            Toast.makeText(getApplicationContext() , "frame:" + frameCount , Toast.LENGTH_SHORT).show();


            boolean sawEOS2 = false;
            int frameCount2 =0;
            while (!sawEOS2)
            {
                frameCount2++;

                audioBufferInfo.offset = offset;
                audioBufferInfo.size = audioExtractor.readSampleData(audioBuf, offset);

                if (videoBufferInfo.size < 0 || audioBufferInfo.size < 0)
                {
                    Log.d("TagMenssage: ", "saw input EOS.");
                    sawEOS2 = true;
                    audioBufferInfo.size = 0;
                }
                else
                {
                    audioBufferInfo.presentationTimeUs = audioExtractor.getSampleTime();
                    audioBufferInfo.flags = audioExtractor.getSampleFlags();
                    muxer.writeSampleData(audioTrack, audioBuf, audioBufferInfo);
                    audioExtractor.advance();

                    Log.d("TagMenssage: ", "Frame (" + frameCount + ") Video PresentationTimeUs:" + videoBufferInfo.presentationTimeUs +" Flags:" + videoBufferInfo.flags +" Size(KB) " + videoBufferInfo.size / 1024);
                    Log.d("TagMenssage: ", "Frame (" + frameCount + ") Audio PresentationTimeUs:" + audioBufferInfo.presentationTimeUs +" Flags:" + audioBufferInfo.flags +" Size(KB) " + audioBufferInfo.size / 1024);

                }
            }

//            Toast.makeText(getApplicationContext() , "frame:" + frameCount2 , Toast.LENGTH_SHORT).show();

            muxer.stop();
            muxer.release();

            // Creo respuesta
            JSObject jsonResponse = new JSObject();
            jsonResponse.put("estado", "Video creado");
            jsonResponse.put("outputFile", outputFile);

            return jsonResponse;

        } catch (IOException e) {
            Log.d("TagMenssage: ", "Mixer Error 1 " + e.getMessage());
            return null;
        } catch (Exception e) {
            Log.d("TagMenssage: ", "Mixer Error 2 " + e.getMessage());
            return null;
        }
    }

}
