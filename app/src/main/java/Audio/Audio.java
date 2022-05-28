package Audio;

import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import communications.R;
import controller.ControllerActivity;

public class Audio extends AppCompatActivity {
    // Declare a MediaPlayer object reference
    MediaPlayer mediaPlayer;

    public Audio(ControllerActivity context, int audio) {


        Log.d("test", "onclick " + audio);
        if(mediaPlayer == null){
            mediaPlayer = MediaPlayer.create(context, audio);
        }
        // Then, register OnCompletionListener that calls a user supplied callback method onCompletion() when
        // looping mode was set to false to indicate playback is completed.
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                // Here, call a method to release the MediaPlayer object and to set it to null.
                stopMusic();
            }
        });
        // Next, call start() method on mediaPlayer to start playing the music.
        mediaPlayer.start();

    }

    public void stop() {
        if(mediaPlayer != null){
            // Here, call stop() method on mediaPlayer to stop the music.
            mediaPlayer.stop();
            // Call stopMusic() method
            stopMusic();
        }
    }

    public void pause() {
        if(mediaPlayer != null) {
            // Here, call pause() method on mediaPlayer to pause the music.
            mediaPlayer.pause();
        }
    }

    private void stopMusic() {
        mediaPlayer.release();
        mediaPlayer = null;
    }

    // Call stopMusic() in onStop() overridden method as well.
    @Override
    protected void onStop() {
        super.onStop();
        stopMusic();
    }
}