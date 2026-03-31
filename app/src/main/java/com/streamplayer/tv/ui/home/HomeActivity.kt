package com.streamplayer.tv.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.streamplayer.tv.databinding.ActivityHomeBinding
import com.streamplayer.tv.model.Channel
import com.streamplayer.tv.ui.player.PlayerActivity

class HomeActivity : FragmentActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val viewModel: HomeViewModel by viewModels()
    private val adapter = ChannelAdapter(::onChannelSelected)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        observeViewModel()

        binding.btnRetry.setOnClickListener { viewModel.loadChannels() }
    }

    private fun setupRecyclerView() {
        val columns = 5
        binding.rvChannels.layoutManager = GridLayoutManager(this, columns)
        binding.rvChannels.adapter = adapter
        binding.rvChannels.setHasFixedSize(true)
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(this) { state ->
            when (state) {
                is HomeUiState.Loading -> showLoading()
                is HomeUiState.Success -> showChannels(state.channels)
                is HomeUiState.Error   -> showError(state.message)
            }
        }
    }

    private fun showLoading() {
        binding.progressBar.visibility  = View.VISIBLE
        binding.rvChannels.visibility   = View.GONE
        binding.layoutError.visibility  = View.GONE
    }

    private fun showChannels(channels: List<Channel>) {
        binding.progressBar.visibility = View.GONE
        binding.rvChannels.visibility  = View.VISIBLE
        binding.layoutError.visibility = View.GONE

        binding.tvChannelCount.text = channels.size.toString()
        adapter.submitList(channels)

        // Focus first card automatically (TV remote UX)
        binding.rvChannels.post {
            binding.rvChannels.getChildAt(0)?.requestFocus()
        }
    }

    private fun showError(message: String) {
        binding.progressBar.visibility = View.GONE
        binding.rvChannels.visibility  = View.GONE
        binding.layoutError.visibility = View.VISIBLE
        binding.tvErrorMsg.text        = message
    }

    private fun onChannelSelected(channel: Channel) {
        val intent = Intent(this, PlayerActivity::class.java).apply {
            putExtra(PlayerActivity.EXTRA_STREAM_URL,  channel.stream)
            putExtra(PlayerActivity.EXTRA_CHANNEL_NAME, channel.name)
            putExtra(PlayerActivity.EXTRA_DRM_KID, channel.drm?.kid)
            putExtra(PlayerActivity.EXTRA_DRM_KEY, channel.drm?.key)
        }
        startActivity(intent)
    }
}
