package com.example.jiffydeliveryclient.ui.ui.home

import android.annotation.SuppressLint
import android.content.res.Resources
import android.location.LocationRequest
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.jiffydeliveryclient.R
import com.example.jiffydeliveryclient.databinding.FragmentHomeBinding
import com.example.jiffydeliveryclient.utils.Constants
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener

class HomeFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentHomeBinding? = null
    private lateinit var mMap: GoogleMap

    // This property is only valid between onCreateView and
    // onDestroyView.
    private lateinit var mapFragment :SupportMapFragment
    private lateinit var locationRequest : LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    //online system
    private lateinit var onlineRef : DatabaseReference
    private lateinit var currentUserRef : DatabaseReference
    private lateinit var clientsLocationRef : DatabaseReference
    private lateinit var geoFire : GeoFire
    private val onlineValueEventListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            currentUserRef.onDisconnect().removeValue()
        }

        override fun onCancelled(error: DatabaseError) {
            Snackbar.make(mapFragment.requireView(),error.message, Snackbar.LENGTH_LONG).show()
        }

    }
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this)[HomeViewModel::class.java]

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        init()

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        return root

    }

    @SuppressLint("MissingPermission")
    private fun init() {
        onlineRef = FirebaseDatabase.getInstance().reference.child(".info/connected")
        clientsLocationRef = FirebaseDatabase.getInstance().getReference(Constants.CLIENT_LOCATION_REFERENCE)
        currentUserRef = FirebaseDatabase.getInstance().getReference(Constants.CLIENT_LOCATION_REFERENCE)
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
        geoFire = GeoFire(clientsLocationRef)
        registerOnlineSystem()

        val locationRequest = com.google.android.gms.location.LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 5000
        ).build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)

                val newPos = LatLng(
                    locationResult.lastLocation?.latitude!!,
                    locationResult.lastLocation?.longitude!!
                )
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newPos, 10f))
                geoFire.setLocation(
                    FirebaseAuth.getInstance().currentUser!!.uid,
                    GeoLocation(locationResult.lastLocation?.latitude!!,
                        locationResult.lastLocation?.longitude!!)
                ){key:String?,error: DatabaseError?->
                    if (error != null){
                        Snackbar.make(mapFragment.requireView(),error.message, Snackbar.LENGTH_LONG).show()

                    }else{
                        Snackbar.make(mapFragment.requireView(),"You are online!", Snackbar.LENGTH_LONG).show()

                    }
                }
            }
        }


        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.myLooper()
        )
    }
    private fun registerOnlineSystem(){
        onlineRef.addValueEventListener(onlineValueEventListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        //geoFire.removeLocation(FirebaseAuth.getInstance().currentUser!!.uid)
        //onlineRef.removeEventListener(onlineValueEventListener)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        //request permission

        Dexter.withContext(context)
            .withPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
            .withListener(object : PermissionListener {
                @SuppressLint("MissingPermission")
                override fun onPermissionGranted(permissions: PermissionGrantedResponse?) {
                    mMap.isMyLocationEnabled = true
                    mMap.uiSettings.isMyLocationButtonEnabled = true

                    mMap.setOnMyLocationButtonClickListener() {
                        fusedLocationProviderClient.lastLocation
                            .addOnFailureListener {
                                Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
                            }.addOnSuccessListener { location ->
                                val userLatLng = LatLng(location.latitude, location.longitude)
                                mMap.animateCamera(
                                    CameraUpdateFactory.newLatLngZoom(
                                        userLatLng,
                                        10f
                                    )
                                )
                            }
                        true
                    }

                    val view = mapFragment.view?.findViewById<View>("1".toInt())?.parent as View
                    val locationButton = view.findViewById<View>("2".toInt())
                    val params = locationButton.layoutParams as RelativeLayout.LayoutParams
                    params.addRule(RelativeLayout.ALIGN_TOP, 0)
                    params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
                    params.bottomMargin = 250
                }

                override fun onPermissionDenied(permissions: PermissionDeniedResponse?) {
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: PermissionRequest?,
                    p1: PermissionToken?
                ) {
                }
            }).check()


        try {
            val success =  googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(),R.raw.jiffy_delivery_maps_style))
            if (!success){
                Log.d("Google Map","error")
            }
        }catch (e: Resources.NotFoundException){
            e.printStackTrace()
        }

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }
}