package com.emfad.app.ViewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.emfad.app.Models.MaterialProperties
import com.emfad.app.Models.MeasurementData
import com.emfad.app.Models.MeasurementMode
import com.emfad.app.Models.MeasurementSession
import com.emfad.app.Models.MeasurementSettings
import com.emfad.app.Utils.DataProcessor
import com.emfad.app.Utils.LocationUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MeasurementViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val _currentSession = MutableStateFlow<MeasurementSession?>(null)
    val currentSession: StateFlow<MeasurementSession?> = _currentSession

    private val _sessionReport = MutableStateFlow<String?>(null)
    val sessionReport: StateFlow<String?> = _sessionReport

    fun startNewSession(
        mode: MeasurementMode,
        settings: MeasurementSettings,
        materialProperties: MaterialProperties? = null,
        notes: String? = null
    ) {
        viewModelScope.launch {
            LocationUtils.requestCurrentLocation(getApplication()).collect {
                val locationData = it?.let { loc ->
                    com.emfad.app.Models.LocationData(loc.latitude, loc.longitude, loc.altitude, loc.accuracy)
                }
                _currentSession.value = DataProcessor.createMeasurementSession(
                    mode = mode,
                    settings = settings,
                    materialProperties = materialProperties,
                    latitude = locationData?.latitude,
                    longitude = locationData?.longitude,
                    altitude = locationData?.altitude,
                    accuracy = locationData?.accuracy,
                    notes = notes
                )
            }
        }
    }

    fun addMeasurement(measurement: MeasurementData) {
        _currentSession.value?.let { session ->
            DataProcessor.addMeasurementToSession(session, measurement)
            _currentSession.value = session // Trigger recomposition/update
        }
    }

    fun finalizeSession() {
        _currentSession.value?.let { session ->
            DataProcessor.finalizeMeasurementSession(session)
            _sessionReport.value = DataProcessor.generateSessionReport(session)
            // Optionally, save the session to a database here
        }
    }

    fun clearSession() {
        _currentSession.value = null
        _sessionReport.value = null
    }

    fun analyzeCurrentSessionMeasurements(): Map<String, Double>? {
        return _currentSession.value?.let { session ->
            if (session.measurements.isNotEmpty()) {
                DataProcessor.analyzeMeasurements(session.measurements)
            } else {
                null
            }
        }
    }
}