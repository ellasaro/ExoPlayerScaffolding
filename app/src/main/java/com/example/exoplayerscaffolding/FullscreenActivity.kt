package com.example.exoplayerscaffolding

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.*
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.activity_fullscreen.*
import kotlinx.android.synthetic.main.layout_exo_player_controls.*

class FullscreenActivity : AppCompatActivity(), Player.EventListener {

    private var mExoPlayer: SimpleExoPlayer? = null
    private var mExoPlayerCurrentPosition: Long = 0
    private var mCurrentUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fullscreen)

        header_tv.text = getString(R.string.video_header)

        secondary_action_im.setOnClickListener {
            finish()
        }

        initializePlayer()
        checkInternetPermission()
    }

    // PERMISSIONS
    private fun checkInternetPermission() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.INTERNET), 101)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_DENIED) {
            checkInternetPermission()
        }
    }

    // PLAYER CALLBACKS
    override fun onTimelineChanged(timeline: Timeline, manifest: Any?, reason: Int) {}
    override fun onTracksChanged(trackGroups: TrackGroupArray, trackSelections: TrackSelectionArray) {}
    override fun onLoadingChanged(isLoading: Boolean) {}
    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {}

    override fun onRepeatModeChanged(repeatMode: Int) {}
    override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {}
    override fun onPlayerError(error: ExoPlaybackException) {
        Toast.makeText(this, R.string.error_playing_video_message, Toast.LENGTH_LONG).show()
    }

    override fun onPositionDiscontinuity(reason: Int) {}
    override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {}
    override fun onSeekProcessed() {}

    // PLAYER
    private fun initializePlayer() {
        if (mExoPlayer == null) {
            // Create an instance of the ExoPlayer.
            val videoTrackSelectionFactory: TrackSelection.Factory = AdaptiveTrackSelection.Factory()
            val trackSelector: TrackSelector = DefaultTrackSelector(this, videoTrackSelectionFactory)
            mExoPlayer = SimpleExoPlayer.Builder(this).setTrackSelector(trackSelector).build()
            mExoPlayer?.addListener(this)
            activity_fullscreen_playerview.player = mExoPlayer
            loadUriIntoPlayer(Uri.parse(getString(R.string.test_url)))
        }
    }

    private fun loadUriIntoPlayer(mediaUri: Uri) {
        if (mExoPlayer == null) {
            initializePlayer()
        }
        val userAgent = Util.getUserAgent(this, getString(R.string.app_name))
        val dataSourceFactory = DefaultDataSourceFactory(this, userAgent, DefaultBandwidthMeter.Builder(this).build())
        val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mediaUri)

        if(mediaUri.toString().contains(".mp3", true)) {
            activity_fullscreen_playerview.defaultArtwork = ContextCompat.getDrawable(this, R.drawable.background_artwork)
            activity_fullscreen_playerview.useArtwork = true
        }

        mExoPlayer?.prepare(mediaSource)
        mExoPlayer?.playWhenReady = true
        mCurrentUri = mediaUri
    }

    private fun releasePlayer() {
        if (mExoPlayer != null) {
            mExoPlayerCurrentPosition = mExoPlayer!!.currentPosition
            mExoPlayer!!.stop()
            mExoPlayer!!.release()
            mExoPlayer = null
        }
    }

    public override fun onResume() {
        super.onResume()
        hideSystemUiFullScreen()
        if (mCurrentUri != null) {
            initializePlayer()
            loadUriIntoPlayer(mCurrentUri!!)
            if (mExoPlayerCurrentPosition > 0) mExoPlayer!!.seekTo(mExoPlayerCurrentPosition)
        }
    }

    public override fun onPause() {
        super.onPause()
        releasePlayer()
        hideSystemUi()
    }

    private fun hideSystemUiFullScreen() {
        activity_fullscreen_playerview.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
    }

    private fun hideSystemUi() {
        activity_fullscreen_playerview.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
    }
}