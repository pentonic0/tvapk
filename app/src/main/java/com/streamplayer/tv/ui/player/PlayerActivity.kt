package com.streamplayer.tv.ui.player

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.dash.DashMediaSource
import androidx.media3.exoplayer.drm.ClearKeyUtil
import androidx.media3.exoplayer.drm.DefaultDrmSessionManager
import androidx.media3.exoplayer.drm.DrmSessionManagerProvider
import androidx.media3.exoplayer.drm.LocalMediaDrmCallback
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.streamplayer.tv.R
import com.streamplayer.tv.databinding.ActivityPlayerBinding

@UnstableApi
class PlayerActivity : FragmentActivity() {

    companion object {
        const val EXTRA_STREAM_URL   = "extra_stream_url"
        const val EXTRA_CHANNEL_NAME = "extra_channel_name"
        const val EXTRA_DRM_KID      = "extra_drm_kid"
        const val EXTRA_DRM_KEY      = "extra_drm_key"
    }

    private lateinit var binding: ActivityPlayerBinding
    private var player: ExoPlayer? = null
    private val hudHandler  = Handler(Looper.getMainLooper())
    private val hudHideRunnable = Runnable { hideHud() }
    private val HUD_TIMEOUT = 3000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val streamUrl   = intent.getStringExtra(EXTRA_STREAM_URL)   ?: run { finish(); return }
        val channelName = intent.getStringExtra(EXTRA_CHANNEL_NAME) ?: ""
        val drmKid      = intent.getStringExtra(EXTRA_DRM_KID)
        val drmKey      = intent.getStringExtra(EXTRA_DRM_KEY)

        binding.tvNowPlayingName.text = channelName
        binding.btnBack.setOnClickListener { finish() }

        initPlayer(streamUrl, drmKid, drmKey)
        showHudBriefly()
    }

    private fun initPlayer(url: String, drmKid: String?, drmKey: String?) {
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("StreamPlayerTV/1.0")
            .setConnectTimeoutMs(15_000)
            .setReadTimeoutMs(15_000)

        // Build ExoPlayer
        val exoBuilder = ExoPlayer.Builder(this)

        // ClearKey DRM
        if (drmKid != null && drmKey != null) {
            try {
                val keyMap = mapOf(
                    android.util.Base64.decode(drmKid, android.util.Base64.DEFAULT) to
                    android.util.Base64.decode(drmKey, android.util.Base64.DEFAULT)
                )
                val drmCallback = LocalMediaDrmCallback(
                    ClearKeyUtil.buildResponseJson(keyMap).toByteArray()
                )
                val drmSessionManager = DefaultDrmSessionManager.Builder()
                    .setUuidAndExoMediaDrmProvider(C.CLEARKEY_UUID, androidx.media3.exoplayer.drm.FrameworkMediaDrm.DEFAULT_PROVIDER)
                    .build(drmCallback)

                val mediaSourceFactory = DefaultMediaSourceFactory(this)
                    .setDrmSessionManagerProvider { drmSessionManager }
                    .setDataSourceFactory(httpDataSourceFactory)

                exoBuilder.setMediaSourceFactory(mediaSourceFactory)
            } catch (e: Exception) {
                // fallback without DRM
            }
        } else {
            exoBuilder.setMediaSourceFactory(
                DefaultMediaSourceFactory(this).setDataSourceFactory(httpDataSourceFactory)
            )
        }

        player = exoBuilder.build().also { exo ->
            binding.playerView.player = exo

            // Determine stream type
            val mediaItem = when {
                url.contains(".mpd",  ignoreCase = true) ->
                    MediaItem.Builder().setUri(Uri.parse(url))
                        .setMimeType(MimeTypes.APPLICATION_MPD).build()
                url.contains(".m3u8", ignoreCase = true) ->
                    MediaItem.Builder().setUri(Uri.parse(url))
                        .setMimeType(MimeTypes.APPLICATION_M3U8).build()
                else ->
                    MediaItem.fromUri(Uri.parse(url))
            }

            exo.setMediaItem(mediaItem)
            exo.prepare()
            exo.playWhenReady = true

            exo.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    when (state) {
                        Player.STATE_BUFFERING -> showLoading()
                        Player.STATE_READY     -> hideLoading()
                        Player.STATE_ENDED     -> finish()
                        else -> {}
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    showError(error.message ?: getString(R.string.error_stream))
                }
            })
        }
    }

    private fun showLoading() {
        binding.loadingOverlay.visibility = View.VISIBLE
        binding.tvErrorMsg.visibility      = View.GONE
    }

    private fun hideLoading() {
        binding.loadingOverlay.visibility = View.GONE
    }

    private fun showError(msg: String) {
        binding.loadingOverlay.visibility = View.GONE
        binding.tvErrorMsg.visibility      = View.VISIBLE
        binding.tvErrorMsg.text            = msg
    }

    // ── HUD ──
    private fun showHudBriefly() {
        binding.playerHud.visibility = View.VISIBLE
        hudHandler.removeCallbacks(hudHideRunnable)
        hudHandler.postDelayed(hudHideRunnable, HUD_TIMEOUT)
    }

    private fun hideHud() {
        binding.playerHud.animate().alpha(0f).setDuration(300).withEndAction {
            binding.playerHud.visibility = View.GONE
            binding.playerHud.alpha = 1f
        }.start()
    }

    // ── TV Remote Key Handling ──
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_BACK,
            KeyEvent.KEYCODE_ESCAPE -> {
                finish()
                true
            }
            KeyEvent.KEYCODE_DPAD_CENTER,
            KeyEvent.KEYCODE_ENTER,
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                player?.let {
                    if (it.isPlaying) it.pause() else it.play()
                    showHudBriefly()
                }
                true
            }
            KeyEvent.KEYCODE_MEDIA_PLAY  -> { player?.play();  showHudBriefly(); true }
            KeyEvent.KEYCODE_MEDIA_PAUSE -> { player?.pause(); showHudBriefly(); true }
            KeyEvent.KEYCODE_MEDIA_STOP  -> { finish(); true }
            KeyEvent.KEYCODE_DPAD_UP,
            KeyEvent.KEYCODE_DPAD_DOWN,
            KeyEvent.KEYCODE_DPAD_LEFT,
            KeyEvent.KEYCODE_DPAD_RIGHT  -> { showHudBriefly(); super.onKeyDown(keyCode, event) }
            else -> super.onKeyDown(keyCode, event)
        }
    }

    override fun onPause() {
        super.onPause()
        player?.pause()
    }

    override fun onResume() {
        super.onResume()
        player?.play()
    }

    override fun onDestroy() {
        super.onDestroy()
        hudHandler.removeCallbacksAndMessages(null)
        player?.release()
        player = null
    }
}
