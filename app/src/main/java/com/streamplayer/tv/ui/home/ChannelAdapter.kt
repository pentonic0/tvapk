package com.streamplayer.tv.ui.home

import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.streamplayer.tv.R
import com.streamplayer.tv.databinding.ItemChannelCardBinding
import com.streamplayer.tv.model.Channel

class ChannelAdapter(
    private val onChannelSelected: (Channel) -> Unit
) : ListAdapter<Channel, ChannelAdapter.ChannelViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Channel>() {
            override fun areItemsTheSame(old: Channel, new: Channel) = old.stream == new.stream
            override fun areContentsTheSame(old: Channel, new: Channel) = old == new
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelViewHolder {
        val binding = ItemChannelCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ChannelViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChannelViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ChannelViewHolder(
        private val binding: ItemChannelCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(channel: Channel) {
            binding.tvChannelName.text = channel.name

            // Load thumbnail
            if (channel.thumbUrl.isNotEmpty()) {
                Glide.with(binding.ivThumbnail.context)
                    .load(channel.thumbUrl)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .placeholder(R.drawable.bg_thumb_placeholder)
                    .error(R.drawable.bg_thumb_placeholder)
                    .into(binding.ivThumbnail)
            } else {
                binding.ivThumbnail.setImageResource(R.drawable.bg_thumb_placeholder)
            }

            binding.tvChannelNameBg.text = channel.name.uppercase()

            // Focus / click handling
            binding.root.isFocusable = true
            binding.root.isFocusableInTouchMode = true

            binding.root.setOnClickListener {
                onChannelSelected(channel)
            }

            binding.root.setOnKeyListener { _, keyCode, event ->
                if (event.action == KeyEvent.ACTION_DOWN &&
                    (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER)
                ) {
                    onChannelSelected(channel)
                    true
                } else false
            }

            binding.root.setOnFocusChangeListener { _, hasFocus ->
                binding.root.isSelected = hasFocus
                binding.playOverlay.alpha = if (hasFocus) 1f else 0f
                if (hasFocus) {
                    binding.root.animate().scaleX(1.06f).scaleY(1.06f).setDuration(150).start()
                    binding.tvChannelName.setTextColor(
                        binding.root.context.getColor(R.color.accent)
                    )
                } else {
                    binding.root.animate().scaleX(1f).scaleY(1f).setDuration(150).start()
                    binding.tvChannelName.setTextColor(
                        binding.root.context.getColor(R.color.text_primary)
                    )
                }
            }
        }
    }
}
