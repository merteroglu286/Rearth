package tr.main.rearth.Activities

import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationRequest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import tr.main.rearth.Adapters.NoticeAdapter
import tr.main.rearth.NoticeModel
import tr.main.rearth.R
import tr.main.rearth.ViewModels.NoticeViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.Marker
import kotlinx.android.synthetic.main.activity_notices.*
import java.util.*
import kotlin.collections.ArrayList

class NearbyNoticesActivity : AppCompatActivity() {

    private lateinit var noticesViewModel : NoticeViewModel
    private  var noticeAdapter = NoticeAdapter(arrayListOf(),this)
    private lateinit var locationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    val locationRequestId = 100

    private val EARTH_RADIUS_IN_METERS = 6371000.0
    private val DISTANCE_THRESHOLD_IN_METERS = 2500.0

    private var centerLatitude : Double = 0.0
    private var centerLongitude : Double = 0.0

    val noticeList = ArrayList<NoticeModel>()

    val nearbyMarkers = mutableListOf<Marker>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notices)
        noticesViewModel = ViewModelProviders.of(this).get(NoticeViewModel::class.java)
        noticesViewModel.getDataFromFirebase()

        noticesList.layoutManager = LinearLayoutManager(this)
        noticesList.adapter = noticeAdapter

        observeLiveData()

        centerLatitude = intent.getDoubleExtra("centerLat",0.0)
        centerLongitude = intent.getDoubleExtra("centerLong",0.0)

        getLocation()

        imgFilter.setOnClickListener {
            if (contextRadio.visibility == View.GONE){
                contextRadio.visibility = View.VISIBLE
            }else{
                contextRadio.visibility = View.GONE
            }
        }



        //konumBilgisi.text = intent.getStringExtra("adres")
/*
        noticeSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {

                    val geoCoder = Geocoder(applicationContext, Locale.getDefault())
                    var addressList = geoCoder.getFromLocationName(query, 1)

                    if (addressList != null && addressList.isNotEmpty()) {
                        var address = addressList[0]
                        updateAddressUI(address.latitude,address.longitude)
                    }


                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {

                return false
            }
        })


 */
    }



    // Verilen iki koordinat arasındaki mesafeyi hesaplayan fonksiyon
    fun distance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val lat1Rad = Math.toRadians(lat1)
        val lat2Rad = Math.toRadians(lat2)
        val a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(lat1Rad) * Math.cos(lat2Rad)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a))
        return EARTH_RADIUS_IN_METERS * c
    }

    private var selectedDegree: String? = null
    fun observeLiveData(){
        noticesViewModel.noticesLiveData.observe(this, Observer { notices ->
            notices?.let{
                noticeList.clear()
                radioGroupFilter.setOnCheckedChangeListener { group, checkedId ->
                    val selectedDegree = when (checkedId) {
                        R.id.btnGreenFilter -> "green"
                        R.id.btnYellowFilter -> "yellow"
                        R.id.btnRedFilter -> "red"
                        else -> null
                    }

                    val filteredNotices = mutableListOf<NoticeModel>()

                    if (selectedDegree != null) {
                        for (notice in notices) {
                            val distanceInMeters = distance(centerLatitude, centerLongitude, notice.latitude!!.toDouble(), notice.longitude!!.toDouble())
                            if (distanceInMeters <= DISTANCE_THRESHOLD_IN_METERS && notice.noticeDegree == selectedDegree) {
                                filteredNotices.add(notice)
                            }
                        }
                    } else {
                        for (notice in notices) {
                            val distanceInMeters = distance(centerLatitude, centerLongitude, notice.latitude!!.toDouble(), notice.longitude!!.toDouble())
                            if (distanceInMeters <= DISTANCE_THRESHOLD_IN_METERS) {
                                filteredNotices.add(notice)
                            }
                        }
                    }

                    noticeAdapter.updateNoticesList(filteredNotices)
                }

                btnClearRadio.setOnClickListener {
                    radioGroupFilter.clearCheck()

                    val filteredNotices = mutableListOf<NoticeModel>()
                    for (notice in notices) {
                        val distanceInMeters = distance(centerLatitude, centerLongitude, notice.latitude!!.toDouble(), notice.longitude!!.toDouble())
                        if (distanceInMeters <= DISTANCE_THRESHOLD_IN_METERS) {
                            filteredNotices.add(notice)
                        }
                    }
                    noticeAdapter.updateNoticesList(filteredNotices)
                }
                for (notice in notices) {
                    val distanceInMeters = distance(centerLatitude, centerLongitude, notice.latitude!!.toDouble(), notice.longitude!!.toDouble())
                    if (distanceInMeters <= DISTANCE_THRESHOLD_IN_METERS) {
                        noticeList.add(notice)
                    }
                }
                noticeAdapter.updateNoticesList(noticeList)

            }
        })

        noticesViewModel.noticeError.observe(this, Observer { error->
            error?.let{
                if (it){
                    Toast.makeText(this,"hata çıktı", Toast.LENGTH_SHORT).show()
                }
            }

        })
    }

    fun getLocation() {

        if (checkForLocationPermission()) {
            updateLocation()
        } else {
            askLocationPermission()
        }
    }

    fun updateLocation() {
        var locationRequest = com.google.android.gms.location.LocationRequest()
        locationRequest.priority = com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 5000
        locationRequest.fastestInterval = 2000

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        mFusedLocationProviderClient.requestLocationUpdates(
            locationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }


    var mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {

            var location: Location? = p0.lastLocation

            location?.let {
                updateAddressUI(location.latitude,location.longitude)
            }

        }
    }

    fun updateAddressUI(latitude:Double,longitude:Double) {

        var geocoder: Geocoder
        var addressList = ArrayList<Address>()

        geocoder = Geocoder(applicationContext, Locale.getDefault())

        addressList = geocoder.getFromLocation(
            latitude,
            longitude,
            1
        ) as ArrayList<Address>
        var address = addressList[0]
        konumBilgisi.text = "${address.subAdminArea}, ${address.adminArea}/${address.countryCode}"
    }


    fun checkForLocationPermission(): Boolean {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
            return true

        return false

    }


    fun askLocationPermission() {

        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            locationRequestId
        )

    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == locationRequestId) {

            if (grantResults != null && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                getLocation()
            }
        }

    }


}