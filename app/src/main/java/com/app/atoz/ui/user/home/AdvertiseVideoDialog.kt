package com.app.atoz.ui.user.home

import android.net.Uri
import android.view.View
import com.app.atoz.R
import com.app.atoz.common.extentions.snack
import com.app.atoz.databinding.DialogAdvertiseVideoBinding
import com.app.atoz.shareddata.base.BaseDialogFragment
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.Util


class AdvertiseVideoDialog(private val videoUrl: String) : BaseDialogFragment() {
    companion object {
        const val ADVERTISE_VIDEO_DIALOG_TAG = "AdvertiseVideoDialog"
        fun newInstance(url: String): AdvertiseVideoDialog {
            return AdvertiseVideoDialog(url)
        }
    }

    private lateinit var mBinding: DialogAdvertiseVideoBinding
    private var mPlayer: SimpleExoPlayer? = null
    private var mComponentListener: ComponentListener? = null
    private var playWhenReady = true
    private var playbackPosition: Long = 0L
    private var currentWindow: Int = 0

    override fun getResource() = R.layout.dialog_advertise_video

    override fun initViewModel() {
        // no need to implement
    }

    override fun postInit() {
        mBinding = getBinding()
        initializePlayer()
    }

    override fun handleListener() {

    }

    override fun displayMessage(message: String) {
        mBinding.root.snack(message)
    }

    private fun initializePlayer() {
        mPlayer = ExoPlayerFactory.newSimpleInstance(
            activity, DefaultRenderersFactory(activity),
            DefaultTrackSelector(), DefaultLoadControl()
        )
        mComponentListener = ComponentListener()
        mPlayer?.addListener(mComponentListener)
        mBinding.playerView.player = mPlayer

        mPlayer?.playWhenReady = true
        mPlayer?.seekTo(currentWindow, playbackPosition)

        val uri = Uri.parse(videoUrl)
        val mediaSource = buildMediaSource(uri)
        mPlayer?.prepare(mediaSource, true, false)
    }

    private fun buildMediaSource(uri: Uri): MediaSource {
        return  ProgressiveMediaSource.Factory(
            DefaultHttpDataSourceFactory("exoplayer-codelab")
        ).createMediaSource(uri)
    }

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT > 23) {
            initializePlayer()
        }
    }

    override fun onResume() {
        super.onResume()
        if (Util.SDK_INT <= 23 || mPlayer == null) {
            initializePlayer()
        }
    }

    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT <= 23) {
            releasePlayer()
        }
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) {
            releasePlayer()
        }
    }

    private fun releasePlayer() {
        if (mPlayer != null) {
            playbackPosition = mPlayer!!.currentPosition
            currentWindow = mPlayer!!.currentWindowIndex
            playWhenReady = mPlayer!!.playWhenReady
            mPlayer!!.removeListener(mComponentListener)
            mPlayer!!.release()
            mPlayer = null
        }
    }

    inner class ComponentListener : Player.DefaultEventListener() {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            super.onPlayerStateChanged(playWhenReady, playbackState)
            when (playbackState) {
                ExoPlayer.STATE_BUFFERING -> {
                    mBinding.includeProgress.visibility = View.VISIBLE
                }
                else -> {
                    mBinding.includeProgress.visibility = View.GONE
                }
            }
        }
    }
}