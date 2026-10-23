package com.example.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class LocationHelper(private val context: Context) {
    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Location? {
        return suspendCancellableCoroutine { continuation ->
            try {
                fusedClient.lastLocation.addOnSuccessListener { lastLoc ->
                    if (lastLoc != null) {
                        continuation.resume(lastLoc)
                    } else {
                        // Request single update
                        val req = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000)
                            .setMaxUpdates(1)
                            .build()
                        val callback = object : LocationCallback() {
                            override fun onLocationResult(result: LocationResult) {
                                fusedClient.removeLocationUpdates(this)
                                continuation.resume(result.lastLocation)
                            }
                        }
                        fusedClient.requestLocationUpdates(req, callback, Looper.getMainLooper())
                            .addOnFailureListener {
                                continuation.resume(null)
                            }
                    }
                }.addOnFailureListener {
                    Log.w("LocationHelper", "Failed to get location", it)
                    continuation.resume(null)
                }
            } catch (e: SecurityException) {
                Log.e("LocationHelper", "SecurityException requesting location", e)
                continuation.resume(null)
            } catch (e: Exception) {
                Log.e("LocationHelper", "Exception requesting location", e)
                continuation.resume(null)
            }
        }
    }
}
