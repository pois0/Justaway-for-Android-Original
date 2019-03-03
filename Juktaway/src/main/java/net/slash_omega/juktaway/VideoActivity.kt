package net.slash_omega.juktaway

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.Window
import jp.nephy.jsonkt.toJsonObject
import jp.nephy.penicillin.endpoints.statuses
import jp.nephy.penicillin.endpoints.statuses.show
import jp.nephy.penicillin.extensions.await
import jp.nephy.penicillin.models.Status
import kotlinx.android.synthetic.main.activity_video.*
import kotlinx.coroutines.*
import net.slash_omega.juktaway.twitter.currentClient
import net.slash_omega.juktaway.util.MessageUtil
import net.slash_omega.juktaway.util.parseWithClient
import org.jetbrains.anko.toast
import java.util.regex.Pattern

private val pattern = Pattern.compile("https?://twitter\\.com/\\w+/status/(\\d+)/video/(\\d+)/?.*")!!

class VideoActivity: DividedFragmentActivity() {
    private var musicWasPlaying = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)

        setContentView(R.layout.activity_video)

        val extra = intent.extras ?: run {
            toast("Missing Bundle in Intent")
            finish()
            return@onCreate
        }

        // TODO 画質を選べるように
        when (extra.getString("arg")) {
            "statusJson" -> setVideoURI(extra.getString("statusJson")!!.toJsonObject().parseWithClient<Status>())
            "statusUrl" -> pattern.matcher(extra.getString("statusUrl"))?.takeIf { it.find() }?.let { m ->
                launch {
                    runCatching {
                        currentClient.statuses.show(m.group(1).toLong()).await()
                    }.onSuccess {
                        setVideoURI(it.result)
                    }
                }
            }
            "videoUrl" -> setVideoURI(extra.getString("videoUrl")!!)
            else -> {
                MessageUtil.showToast("Missing videoUrl in Bundle")
                finish()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        finish()
    }

    override fun onDestroy() {
        if (musicWasPlaying)
            sendBroadcast(Intent("com.android.music.musicservicecommand")
                    .apply { putExtra("command", "play") })
        super.onDestroy()
    }

    private fun setVideoURI(status: Status) = status.extendedEntities?.let { entities ->
        entities.media.first { it.type == "video" }.videoInfo?.variants?.get(1)?.url?.let { setVideoURI(it) }
    }

    private fun setVideoURI(url: String) {
        musicWasPlaying = (getSystemService(Context.AUDIO_SERVICE) as AudioManager).isMusicActive

        guruguru.visibility = View.VISIBLE
        player.setOnTouchListener { _, _ ->
            finish()
            false
        }
        player.setOnPreparedListener { guruguru.visibility = View.GONE }
        player.setOnCompletionListener {
            player.seekTo(0)
            player.start()
        }
        player.setVideoURI(Uri.parse(url))
        player.start()
    }
}