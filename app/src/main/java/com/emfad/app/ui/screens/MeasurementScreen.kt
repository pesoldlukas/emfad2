package com.emfad.app.ui.screens

import android.bluetooth.BluetoothDevice
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.emfad.app.Models.MeasurementMode
import com.emfad.app.Models.MeasurementSettings
import com.emfad.app.Utils.GraphUtils
import com.emfad.app.Utils.UIUtils
import com.emfad.app.ViewModels.BluetoothViewModel
import com.emfad.app.ViewModels.MeasurementViewModel

@Composable
fun MeasurementScreen(
    bluetoothViewModel: BluetoothViewModel = hiltViewModel(),
    measurementViewModel: MeasurementViewModel = hiltViewModel(),
    connectedDevice: BluetoothDevice?
) {
    val connectionState by bluetoothViewModel.connectionState.collectAsState()
    val latestMeasurement by bluetoothViewModel.measurementData.collectAsState()
    val currentSession by measurementViewModel.currentSession.collectAsState()
    val sessionReport by measurementViewModel.sessionReport.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Section: Connection Info and Controls
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Device: ${connectedDevice?.name ?: connectedDevice?.address ?: "N/A"}",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Status: $connectionState",
                style = MaterialTheme.typography.bodyMedium,
                color = if (connectionState.contains("Connected")) Color.Green else Color.Red
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { bluetoothViewModel.disconnectDevice() }) {
                Text("Disconnect")
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Session Controls
            if (currentSession == null) {
                Button(onClick = {
                    // Example: Start a new session with dummy settings
                    val dummySettings = MeasurementSettings(
                        frequencyRange = "100Hz-10kHz",
                        antennaConfiguration = "Vertical",
                        depthResolution = "1m",
                        maxDepth = 100.0,
                        powerLevel = "High",
                        gainSetting = "Auto",
                        filterSettings = "None",
                        temperatureCompensation = true,
                        environmentalCorrection = true,
                        calibrationDate = "2023-01-01",
                        lastServiceDate = "2023-01-01"
                    )
                    measurementViewModel.startNewSession(MeasurementMode.BA_VERTICAL, dummySettings, notes = "Test Session")
                }) {
                    Text("Start New Session")
                }
            } else {
                Text("Session Active: ${currentSession?.mode?.displayName}", style = MaterialTheme.typography.titleSmall)
                Text("Measurements: ${currentSession?.measurements?.size}", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    Button(onClick = { measurementViewModel.finalizeSession() }) {
                        Text("Finalize Session")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { measurementViewModel.clearSession() }) {
                        Text("Clear Session")
                    }
                }
            }
        }

        // Middle Section: Live Measurement Data & Graph
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
            latestMeasurement?.let {
                Text(
                    text = "Latest: ${UIUtils.formatValue(it.value)} ${it.unit} @ ${UIUtils.formatFrequency(it.frequency)}",
                    style = MaterialTheme.typography.headlineMedium
                )
            } ?: Text("Waiting for data...")

            Spacer(modifier = Modifier.height(16.dp))

            // Graph Placeholder
            val measurementsForGraph = currentSession?.measurements ?: emptyList()
            if (measurementsForGraph.isNotEmpty()) {
                val valueRange = GraphUtils.calculateValueRange(measurementsForGraph)
                val frequencyRange = GraphUtils.calculateFrequencyRange(measurementsForGraph)

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    val path = GraphUtils.calculateLinePath(
                        measurements = measurementsForGraph,
                        width = size.width,
                        height = size.height,
                        valueRange = valueRange,
                        frequencyRange = frequencyRange
                    )
                    drawPath(path, color = Color.Blue, style = Stroke(width = 3f))

                    // Draw Y-axis labels
                    GraphUtils.getYAxisLabels(valueRange).forEach { (value, label) ->
                        val y = (1 - ((value - valueRange.first) / (valueRange.second - valueRange.first))).toFloat() * size.height
                        drawContext.canvas.nativeCanvas.drawText(
                            label,
                            10f, // X position
                            y, // Y position
                            android.graphics.Paint().apply { color = android.graphics.Color.BLACK; textSize = 30f }
                        )
                    }

                    // Draw X-axis labels
                    GraphUtils.getXAxisLabels(frequencyRange).forEach { (freq, label) ->
                        val x = ((freq - frequencyRange.first) / (frequencyRange.second - frequencyRange.first)).toFloat() * size.width
                        drawContext.canvas.nativeCanvas.drawText(
                            label,
                            x, // X position
                            size.height - 10f, // Y position
                            android.graphics.Paint().apply { color = android.graphics.Color.BLACK; textSize = 30f }
                        )
                    }
                }
            } else {
                Text("No measurements to display graph.")
            }
        }

        // Bottom Section: Session Report
        sessionReport?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Session Report:",
                style = MaterialTheme.typography.titleSmall
            )
            Text(text = it, style = MaterialTheme.typography.bodySmall)
        }
    }
}