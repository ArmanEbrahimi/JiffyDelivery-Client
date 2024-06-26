package com.example.jiffydeliveryclient.ui

import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.jiffydeliveryclient.R
import com.example.jiffydeliveryclient.model.SelectedPlaceEvent
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Circle
import com.example.jiffydeliveryclient.databinding.ActivityRequestCourierBinding
import com.example.jiffydeliveryclient.model.AcceptRequestFromCourier
import com.example.jiffydeliveryclient.model.CourierGeoModel
import com.example.jiffydeliveryclient.model.DeclineRequestFromCourier
import com.example.jiffydeliveryclient.model.Order
import com.example.jiffydeliveryclient.remote.GoogleAPI
import com.example.jiffydeliveryclient.remote.RetrofitClient
import com.example.jiffydeliveryclient.utils.Constants
import com.example.jiffydeliveryclient.utils.UserUtils
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.SquareCap
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.maps.android.ui.IconGenerator
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject

class RequestCourierActivity : AppCompatActivity(), OnMapReadyCallback {

    private var lastCourierCall: CourierGeoModel? = null

    //Spinning animation
    private var animator: ValueAnimator? = null
    private val DESIRED_NUM_OF_SPINS = 5
    private val DESIRED_SECONDS_PER_ONE_FULL_360_SPIN = 40

    //Effect
    private var lastUserCircle: Circle? = null
    private val durationTime = 1000
    private var lastPulseAnimator: ValueAnimator? = null

    private lateinit var mMap: GoogleMap
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var binding: ActivityRequestCourierBinding

    private var selectedPlaceEvent: SelectedPlaceEvent? = null
    private lateinit var text_origin: TextView
    private lateinit var text_address_pickup: TextView

    //Routes
    private val compositeDisposable = CompositeDisposable()
    private lateinit var googleAPI: GoogleAPI
    private var blackPolyLine: Polyline? = null
    private var greyPolyLine: Polyline? = null
    private var polylineOptions: PolylineOptions? = null
    private var blackPolyLineOptions: PolylineOptions? = null
    private var polylineList: MutableList<LatLng>? = null
    private var originMarker: Marker? = null
    private var destinationMarker: Marker? = null

    //order info
    private lateinit var database: FirebaseDatabase
    private lateinit var orderWeight : Spinner
    private lateinit var orderSize: Spinner
    private lateinit var orderInfoRef: DatabaseReference
    private lateinit var order: Order
    private lateinit var duration: String

    //courier info
    private  var acceptRequestFromCourier: AcceptRequestFromCourier? = null
    private lateinit var imageAvatar : CircleImageView
    private lateinit var courierName : TextView
    private lateinit var expectingTime: TextView
    private lateinit var msgBody: TextView


    override fun onStart() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        super.onStart()
    }

    override fun onStop() {
        compositeDisposable.clear()
        if (EventBus.getDefault().hasSubscriberForEvent(SelectedPlaceEvent::class.java)) {
            EventBus.getDefault().removeStickyEvent(SelectedPlaceEvent::class.java)
            EventBus.getDefault().unregister(this)
        }
        if (EventBus.getDefault().hasSubscriberForEvent(DeclineRequestFromCourier::class.java)) {
            EventBus.getDefault().removeStickyEvent(DeclineRequestFromCourier::class.java)
            EventBus.getDefault().unregister(this)
        }
        super.onStop()
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onDeclineReceived(event: DeclineRequestFromCourier) {
        Log.d("decline method","I am called")
        if (lastCourierCall != null) {
            Constants.couriersFound.get(lastCourierCall!!.key)!!.isDeclined = true
            findNearbyCourier(selectedPlaceEvent!!.origin)
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onAcceptReceived(event: AcceptRequestFromCourier) {
        acceptRequestFromCourier = event
        Log.d("accept method","I am called")
        findViewById<View>(R.id.confirm_pickup_layout).visibility = View.VISIBLE
        imageAvatar = findViewById(R.id.profile_image)
        if(acceptRequestFromCourier!!.avatarImage != null && !TextUtils.isEmpty(acceptRequestFromCourier!!.avatarImage) ){
            Picasso.get().load(acceptRequestFromCourier!!.avatarImage)
                .into(imageAvatar)
        }

        courierName.setText(acceptRequestFromCourier!!.firtName+" "+acceptRequestFromCourier!!.lastName)
        expectingTime.setText("Will be there approximately in: "+ acceptRequestFromCourier!!.expectingTime)
        msgBody.setText(acceptRequestFromCourier!!.notificationBody)

    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onSelectedPlaceEvent(event: SelectedPlaceEvent) {
        selectedPlaceEvent = event
    }

    override fun onDestroy() {
        if (animator != null) animator!!.end()
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRequestCourierBinding.inflate(layoutInflater)
        setContentView(binding.root)

        orderWeight = findViewById(R.id.spinner_weight)
        orderSize = findViewById(R.id.spinner_size)

        courierName = findViewById(R.id.text_courier_name)
        expectingTime = findViewById(R.id.text_expecting_time)
        msgBody = findViewById(R.id.text_body)

        init()
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap


        drawPath(selectedPlaceEvent!!)

        try {
            val success = googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    this,
                    R.raw.jiffy_delivery_maps_style
                )
            )
            if (!success) {
                Snackbar.make(
                    mapFragment.requireView(),
                    "Error while loading the map style",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        } catch (e: Exception) {
            Snackbar.make(mapFragment.requireView(), e.message!!, Snackbar.LENGTH_LONG).show()
        }

    }

    private fun drawPath(selectedPlaceEvent: SelectedPlaceEvent) {
        compositeDisposable.add(googleAPI.getDirections(
            "driving",
            "less_driving",
            selectedPlaceEvent.originString, selectedPlaceEvent.destinationString,
            getString(R.string.api_key)
        )
        !!.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { returnResult ->
                Log.d("API_RETURN", returnResult)
                try {

                    val jsonObject = JSONObject(returnResult)
                    val jsonArray = jsonObject.getJSONArray("routes")
                    for (i in 0 until jsonArray.length()) {
                        val route = jsonArray.getJSONObject(i)
                        val poly = route.getJSONObject("overview_polyline")
                        val polyLine = poly.getString("points")
                        polylineList = Constants.decodePoly(polyLine)
                    }

                    polylineOptions = PolylineOptions()
                    polylineOptions!!.color(Color.GRAY)
                    polylineOptions!!.width(12f)
                    polylineOptions!!.startCap(SquareCap())
                    polylineOptions!!.jointType(JointType.ROUND)
                    polylineOptions!!.addAll(polylineList!!)
                    greyPolyLine = mMap.addPolyline(polylineOptions!!)

                    blackPolyLineOptions = PolylineOptions()
                    blackPolyLineOptions!!.color(Color.BLACK)
                    blackPolyLineOptions!!.width(5f)
                    blackPolyLineOptions!!.startCap(SquareCap())
                    blackPolyLineOptions!!.jointType(JointType.ROUND)
                    blackPolyLineOptions!!.addAll(polylineList!!)
                    blackPolyLine = mMap.addPolyline(blackPolyLineOptions!!)

                    //Animator
                    val valueAnimator = ValueAnimator.ofInt(0, 100)
                    valueAnimator.duration = 1100
                    valueAnimator.repeatCount = ValueAnimator.INFINITE
                    valueAnimator.interpolator = LinearInterpolator()
                    valueAnimator.addUpdateListener { value ->
                        val points = greyPolyLine!!.points
                        val percentValue = valueAnimator.animatedValue.toString().toInt()
                        val size = points.size
                        val newPoints = (size * (percentValue / 100f)).toInt()
                        val p = points.subList(0, newPoints)
                        blackPolyLine!!.points = p
                    }
                    valueAnimator.start()

                    val latLngBound = LatLngBounds.Builder().include(selectedPlaceEvent.origin)
                        .include(selectedPlaceEvent.destination)
                        .build()

                    val objects = jsonArray.getJSONObject(0)
                    val legs = objects.getJSONArray("legs")
                    val legsObject = legs.getJSONObject(0)

                    val time = legsObject.getJSONObject("duration")
                    duration = time.getString("text")

                    val start_address = legsObject.getString("start_address")
                    val end_address = legsObject.getString("end_address")

                    addOriginMarker(duration, start_address)
                    addDestinationMarker(end_address)

                    mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBound, 160))
                    mMap.moveCamera(CameraUpdateFactory.zoomTo(mMap.cameraPosition.zoom - 1))

                } catch (e: Exception) {
                    Snackbar.make(mapFragment.requireView(), e.message!!, Snackbar.LENGTH_LONG)
                        .show()
                }
            }
        )
    }

    private fun addDestinationMarker(endAddress: String) {
        val view = layoutInflater.inflate(R.layout.destination_info_window, null, false)

        val text_destination = view.findViewById<View>(R.id.text_destination) as TextView
        text_destination.text = Constants.formatAddress(endAddress)

        val generator = IconGenerator(this)
        generator.setContentView(view)
        generator.setBackground(ColorDrawable(Color.TRANSPARENT))
        val icon = generator.makeIcon()

        destinationMarker = mMap.addMarker(
            MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(icon))
                .position(selectedPlaceEvent!!.destination)
        )

    }

    private fun addOriginMarker(duration: String, startAddress: String) {

        val view = layoutInflater.inflate(R.layout.origin_info_window, null, false)
        val text_time = view.findViewById<View>(R.id.text_time) as TextView
        text_origin = view.findViewById<View>(R.id.text_origin) as TextView

        text_time.text = Constants.formatDuration(duration)
        text_origin.text = Constants.formatAddress(startAddress)

        val generator = IconGenerator(this)
        generator.setContentView(view)
        generator.setBackground(ColorDrawable(Color.TRANSPARENT))
        val icon = generator.makeIcon()

        originMarker = mMap.addMarker(
            MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(icon))
                .position(selectedPlaceEvent!!.origin)
        )

    }

    private fun init() {
        googleAPI = RetrofitClient.instance!!.create(GoogleAPI::class.java)

        database = FirebaseDatabase.getInstance()
        orderInfoRef = database.getReference(Constants.ORDER_INFO_REFERENCE)

        findViewById<Button>(R.id.button_confirm_order)
            .setOnClickListener {
                text_address_pickup = findViewById<View>(R.id.confirm_pickup_layout)
                    .findViewById<TextView>(R.id.text_view_address_pickup)
                findViewById<View>(R.id.confirm_order_layout).visibility = View.GONE

                 order = Order(orderWeight.selectedItem.toString(),
                    orderSize.selectedItem.toString(),
                    selectedPlaceEvent!!.destination.toString(),
                    selectedPlaceEvent!!.origin.toString())

                orderInfoRef.child(FirebaseAuth.getInstance().currentUser!!.uid)
                    .setValue(order)
                    .addOnFailureListener {
                        Toast.makeText(
                            this@RequestCourierActivity,
                            "${it.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }.addOnSuccessListener {
                        Toast.makeText(
                            this@RequestCourierActivity,
                            "Order Placed Successfully!",
                            Toast.LENGTH_SHORT
                        ).show()

                    }

                Log.d("order info",""+order.weight+" "+order.size+" "+
                order.destination+" "+order.origin+order.courier)

                findNearbyCourier(selectedPlaceEvent!!.origin)

                setDataPickup()
            }


        findViewById<Button>(R.id.btn_confirm_pickup)
            .setOnClickListener {
                if (mMap == null) return@setOnClickListener
                if (selectedPlaceEvent == null) return@setOnClickListener

                mMap.clear()

                //Tilt
                val cameraPos = CameraPosition.Builder().target(selectedPlaceEvent!!.origin)
                    .tilt(45f)
                    .zoom(16f)
                    .build()

                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPos))

                //Start animation
                addMarkerWithPulseAnimation()
            }
    }

    private fun addMarkerWithPulseAnimation() {
        findViewById<View>(R.id.confirm_pickup_layout).visibility = View.GONE
        findViewById<View>(R.id.fill_maps).visibility = View.VISIBLE
        findViewById<View>(R.id.fiding_your_courier_layout).visibility = View.VISIBLE

        originMarker = mMap.addMarker(
            MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker())
                .position(selectedPlaceEvent!!.origin)
        )

        addPulsatingEffect(selectedPlaceEvent!!.origin)

    }

    private fun addPulsatingEffect(origin: LatLng) {
        if (lastPulseAnimator != null) lastPulseAnimator!!.cancel()
        if (lastUserCircle != null) lastUserCircle!!.center = origin
        lastPulseAnimator = Constants.valueAnimate(durationTime.toLong()) { animation ->
            if (lastUserCircle != null) lastUserCircle!!.radius =
                animation.animatedValue.toString().toDouble() else {
                lastUserCircle = mMap.addCircle(
                    CircleOptions()
                        .center(origin)
                        .radius(animation.animatedValue.toString().toDouble())
                        .strokeColor(Color.WHITE)
                        .fillColor(
                            ContextCompat.getColor(
                                this@RequestCourierActivity,
                                R.color.map_darker
                            )
                        )
                )
            }
        }

        //Start rotating camera
        startMaoCameraSpinningAnimation(mMap.cameraPosition.target)

    }

    private fun startMaoCameraSpinningAnimation(target: LatLng) {
        if (animator != null) animator?.cancel()
        animator = ValueAnimator.ofFloat(0f, (DESIRED_NUM_OF_SPINS * 360.toFloat()))
        animator!!.duration = (DESIRED_SECONDS_PER_ONE_FULL_360_SPIN * 1000).toLong()
        animator!!.interpolator = LinearInterpolator()
        animator!!.startDelay = 100
        animator!!.addUpdateListener { valueAnimator ->
            val newBearingValue = valueAnimator.animatedValue as Float
            mMap.moveCamera(
                CameraUpdateFactory.newCameraPosition(
                    CameraPosition.Builder()
                        .target(target)
                        .zoom(16f)
                        .tilt(45f)
                        .bearing(newBearingValue)
                        .build()
                )
            )
        }

        animator!!.start()

        //findNearbyCourier(target)
    }

    private fun findNearbyCourier(target: LatLng) {
        if (Constants.couriersFound.size > 0) {
            Log.d("number of couriers = ",Constants.couriersFound.size.toString())
            var min = 0f
            var foundCourier: CourierGeoModel? = null

            val currentCourierLocation = Location("")
            currentCourierLocation.latitude = target.latitude
            currentCourierLocation.longitude = target.longitude

            for (key in Constants.couriersFound.keys) {

                val courierLocation = Location("")
                courierLocation.latitude = Constants.couriersFound[key]!!.geoLocation!!.latitude
                courierLocation.longitude = Constants.couriersFound[key]!!.geoLocation!!.longitude

                //First, init min value and found driver if first driver is in list
                if (min == 0f) {
                    min = courierLocation.distanceTo(currentCourierLocation)
                    if (!Constants.couriersFound[key]!!.isDeclined) {
                        foundCourier = Constants.couriersFound[key]
                        break // exit the loop because we already have found a courier
                    } else {
                        continue // if it is already declined skip and continue
                    }
                } else if (courierLocation.distanceTo(currentCourierLocation) < min) {
                    min = courierLocation.distanceTo(currentCourierLocation)
                    if (!Constants.couriersFound[key]!!.isDeclined) {
                        foundCourier = Constants.couriersFound[key]
                        break // exit the loop because we already have found a courier
                    } else {
                        continue // if it is already declined skip and continue
                    }
                }
            }
            if (foundCourier != null){
                UserUtils.sendRequestToDriver(
                    this@RequestCourierActivity,
                    findViewById<View>(R.id.main_layout),
                    foundCourier,
                    target,
                    order,
                    Constants.formatDuration(duration).toString()
                )
                lastCourierCall = foundCourier
            }else{
                Log.d("no Courier","There is no Courier")
                Toast.makeText(this@RequestCourierActivity, getString(R.string.no_courier_accepted_the_request), Toast.LENGTH_SHORT).show()
                lastCourierCall = null
                finish()
            }
//            Snackbar.make(
//                mapFragment.requireView(), StringBuilder("Found driver: ")
//                    .append(foundCourier!!.courierInfoModel!!.firstName), Snackbar.LENGTH_LONG
//            ).show()



        } else {
            Snackbar.make(
                mapFragment.requireView(),
                getString(R.string.couriers_not_found),
                Snackbar.LENGTH_LONG
            ).show()
            lastCourierCall = null
            finish()
        }
    }

    private fun setDataPickup() {
        text_address_pickup.text = if (text_origin != null) text_origin.text else "None"
        mMap.clear()
        addPickupMarker()
    }

    private fun addPickupMarker() {
        val view = layoutInflater.inflate(R.layout.pickup_info_window, null, false)

        val generator = IconGenerator(this)
        generator.setContentView(view)
        generator.setBackground(ColorDrawable(Color.TRANSPARENT))
        val icon = generator.makeIcon()
        originMarker = mMap.addMarker(
            MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(icon))
                .position(selectedPlaceEvent!!.origin)
        )
    }
}