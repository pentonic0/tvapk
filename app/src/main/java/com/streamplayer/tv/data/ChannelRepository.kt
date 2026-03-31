package com.streamplayer.tv.data

import com.google.gson.Gson
import com.streamplayer.tv.model.Channel
import com.streamplayer.tv.model.ChannelListResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object ChannelRepository {

    private const val JSON_URL =
        "https://raw.githubusercontent.com/pentonic0/mpd/main/link.json"

    private val gson = Gson()

    suspend fun fetchChannels(): Result<List<Channel>> = withContext(Dispatchers.IO) {
        try {
            val url = URL(JSON_URL)
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 10_000
            connection.readTimeout = 15_000
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/json")

            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                return@withContext Result.failure(Exception("HTTP $responseCode"))
            }

            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            val sb = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                sb.append(line)
            }
            reader.close()
            connection.disconnect()

            val response = gson.fromJson(sb.toString(), ChannelListResponse::class.java)
            if (response.channels.isEmpty()) {
                Result.failure(Exception("No channels found"))
            } else {
                Result.success(response.channels)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
