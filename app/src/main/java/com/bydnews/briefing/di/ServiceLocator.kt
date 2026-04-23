package com.bydnews.briefing.di

import android.content.Context
import com.bydnews.briefing.data.prefs.SecurePrefs
import com.bydnews.briefing.data.repo.BriefingRepository
import com.bydnews.briefing.data.storage.BriefingStore
import com.bydnews.briefing.net.AnthropicClient
import com.bydnews.briefing.net.GoogleTtsClient
import com.bydnews.briefing.net.Http

object ServiceLocator {
    lateinit var appContext: Context
        private set
    lateinit var prefs: SecurePrefs
        private set
    lateinit var anthropic: AnthropicClient
        private set
    lateinit var tts: GoogleTtsClient
        private set
    lateinit var repo: BriefingRepository
        private set

    fun init(context: Context) {
        appContext = context.applicationContext
        prefs = SecurePrefs(appContext)
        val http = Http.client
        anthropic = AnthropicClient(http)
        tts = GoogleTtsClient(http)
        repo = BriefingRepository(BriefingStore(appContext))
    }
}
