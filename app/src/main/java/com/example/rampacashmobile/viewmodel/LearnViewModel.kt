package com.example.rampacashmobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rampacashmobile.data.LearnModule
import com.example.rampacashmobile.data.LearnModulesData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LearnViewModel @Inject constructor() : ViewModel() {

    private val _modules = MutableStateFlow<List<LearnModule>>(LearnModulesData.modules)
    val modules: StateFlow<List<LearnModule>> = _modules.asStateFlow()

    private val _totalBonksEarned = MutableStateFlow(0)
    val totalBonksEarned: StateFlow<Int> = _totalBonksEarned.asStateFlow()

    private val _currentLessonIndex = MutableStateFlow(0)
    val currentLessonIndex: StateFlow<Int> = _currentLessonIndex.asStateFlow()

    init {
        calculateTotalBonks()
    }

    fun completeSubmodule(moduleId: String, submoduleId: String) {
        viewModelScope.launch {
            val updatedModules = _modules.value.map { module ->
                if (module.id == moduleId) {
                    val newCompletedSet = module.completedSubmodules + submoduleId
                    module.copy(completedSubmodules = newCompletedSet)
                } else {
                    module
                }
            }
            _modules.value = updatedModules
            calculateTotalBonks()
        }
    }

    fun setCurrentLessonIndex(index: Int) {
        _currentLessonIndex.value = index
    }

    fun nextLesson() {
        _currentLessonIndex.value += 1
    }

    fun previousLesson() {
        if (_currentLessonIndex.value > 0) {
            _currentLessonIndex.value -= 1
        }
    }

    fun resetLessonIndex() {
        _currentLessonIndex.value = 0
    }

    private fun calculateTotalBonks() {
        val total = _modules.value.sumOf { module ->
            if (module.isCompleted) module.bonkReward else 0
        }
        _totalBonksEarned.value = total
    }

    fun getModule(moduleId: String): LearnModule? {
        return _modules.value.find { it.id == moduleId }
    }

    fun isSubmoduleCompleted(moduleId: String, submoduleId: String): Boolean {
        return _modules.value
            .find { it.id == moduleId }
            ?.completedSubmodules
            ?.contains(submoduleId) ?: false
    }
}

