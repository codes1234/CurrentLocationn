package com.skyway.currentlocationn

import android.content.Context
import android.content.IntentSender.SendIntentException
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.skyway.currentlocationn.databinding.ActivityMapsBinding

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    lateinit var currentlocation: Location
    private lateinit var client: FusedLocationProviderClient
    private val permissionCode = 101
    private val REQUEST_CHECK_SETTINGS = 214

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        client = LocationServices.getFusedLocationProviderClient(this);
//       getGPSpermission()
        getCurrentLocationUser()
    }

    private fun getCurrentLocationUser() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                permissionCode
            )
            return
        } else {

            val getLocation = client.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    currentlocation = location
                    val latitude: Double = currentlocation.latitude
                    val longitude: Double = currentlocation.longitude
                    Toast.makeText(this, "$latitude and $longitude", Toast.LENGTH_SHORT).show()
                    // Obtain the SupportMapFragment and get notified when the map is ready to be used.
                    val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
                    mapFragment.getMapAsync(this)
                } else {
                    Toast.makeText(this, "Please on Loction", Toast.LENGTH_SHORT).show()

                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            permissionCode -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                getCurrentLocationUser()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val latLng = LatLng(currentlocation.latitude, currentlocation.longitude)
        mMap.addMarker(MarkerOptions().position(latLng).title("Marker in Sydney")
            .icon(BitmapDescriptorFactory.fromBitmap(generateSmallIcon(applicationContext))))

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))

    }
    fun generateSmallIcon(context: Context): Bitmap {
        val height = 150
        val width = 100
        val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.markerimg)
        return Bitmap.createScaledBitmap(bitmap, width, height, false)
    }

    private fun getGPSpermission() {
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(LocationRequest().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY))
        builder.setAlwaysShow(true)
        val mLocationSettingsRequest = builder.build()
        val mSettingsClient = LocationServices.getSettingsClient(this@MapsActivity)
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
            .addOnSuccessListener { //Success Perform Task Here

            }
            .addOnFailureListener { e ->
                val statusCode = (e as ApiException).statusCode
                when (statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                        val rae = e as ResolvableApiException
                        rae.startResolutionForResult(
                            this@MapsActivity, REQUEST_CHECK_SETTINGS
                        )
                    } catch (sie: SendIntentException) {
                        Log.e("GPS", "Unable to execute request.")
                    }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> Log.e(
                        "GPS",
                        "Location settings are inadequate, and cannot be fixed here. Fix in Settings."
                    )
                }
            }
            .addOnCanceledListener { Log.e("GPS", "checkLocationSettings -> onCanceled") }
    }

}