package com.bydnews.briefing.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.bydnews.briefing.data.model.Briefing
import com.bydnews.briefing.di.ServiceLocator
import com.bydnews.briefing.work.Scheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(app: Application) : AndroidViewModel(app) {

    private val _briefings = MutableStateFlow<List<Briefing>>(emptyList())
    val briefings: StateFlow<List<Briefing>> = _briefings.asStateFlow()

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    init {
        refresh()
        observeManualWork()
    }

    fun refresh() {
        viewModelScope.launch {
            _briefings.value = ServiceLocator.repo.list()
        }
    }

    fun generateNow() {
        Scheduler.enqueueManual(getApplication())
    }

    private fun observeManualWork() {
        val wm = WorkManager.getInstance(getApplication())
        wm.getWorkInfosForUniqueWorkLiveData(Scheduler.UNIQUE_MANUAL).observeForever { infos ->
            val running = infos.any { it.state == WorkInfo.State.RUNNING || it.state == WorkInfo.State.ENQUEUED }
            _busy.value = running
            if (!running) refresh()
        }
    }
}
