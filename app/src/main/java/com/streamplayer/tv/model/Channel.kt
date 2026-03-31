package com.streamplayer.tv.model

import com.google.gson.annotations.SerializedName

data class ChannelListResponse(
    @SerializedName("channels") val channels: List<Channel> = emptyList()
)

data class Channel(
    @SerializedName("name")      val name: String = "",
    @SerializedName("stream")    val stream: String = "",
    @SerializedName("thumbnail") val thumbnail: String? = null,
    @SerializedName("logo")      val logo: String? = null,
    @SerializedName("image")     val image: String? = null,
    @SerializedName("drm")       val drm: DrmInfo? = null
) {
    val thumbUrl: String get() = thumbnail ?: logo ?: image ?: ""
}

data class DrmInfo(
    @SerializedName("kid") val kid: String? = null,
    @SerializedName("key") val key: String? = null
)
