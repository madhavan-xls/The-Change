import android.content.Context
import android.net.Uri
import android.media.MediaPlayer

object MediaPlayerHelper {
    private var mediaPlayer: MediaPlayer? = null
    
    fun play(context: Context, soundUri: Uri) {
        stop()
        mediaPlayer = MediaPlayer.create(context, soundUri).apply {
            isLooping = true
            start()
        }
    }

    fun stop() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
        }
        mediaPlayer = null
    }
} 