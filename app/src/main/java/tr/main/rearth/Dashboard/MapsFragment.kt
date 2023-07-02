package tr.main.rearth.Dashboard

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PorterDuff
import android.location.*
import android.net.Uri
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import tr.main.rearth.Activities.NearbyNoticesActivity
import tr.main.rearth.Constants.AppConstants
import tr.main.rearth.UserModel
import tr.main.rearth.ViewModels.BlackListViewModel
import tr.main.rearth.ViewModels.NoticeViewModel
import tr.main.rearth.ViewModels.ProfileViewModel
import tr.main.rearth.databinding.BildiriDialogBinding
import tr.main.rearth.databinding.FragmentMapsBinding
import tr.main.rearth.R

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.bildiri_dialog.*
import kotlin.math.roundToInt
import tr.main.rearth.Activities.NoticeDetailActivity
import tr.main.rearth.NoticeModel
import kotlinx.android.synthetic.main.activity_notices.*
import kotlinx.android.synthetic.main.custom_infoview.*
import kotlinx.android.synthetic.main.custom_infoview.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MapsFragment : Fragment(),OnMapReadyCallback,GoogleMap.OnMarkerClickListener,
    GoogleMap.OnCameraIdleListener{

    private var _binding: FragmentMapsBinding? = null
    private val binding get() = _binding!!
    private lateinit var mMap : GoogleMap
    private lateinit var locationManager : LocationManager
    private lateinit var locationListener : LocationListener
    private lateinit var guncelKonum : LatLng
    private lateinit var dialog:  AlertDialog
    private lateinit var dialogBinding : BildiriDialogBinding
    private lateinit var noticeViewModel : NoticeViewModel
    private lateinit var profileViewModels: ProfileViewModel
    private lateinit var blackListViewModel: BlackListViewModel
    private lateinit var username:String
    private lateinit var userImage:String
    private lateinit var userID:String
    private  var noticeImageUri: Uri? = null
    private lateinit var noticeImageUrl:String

    private var centerLat:Double = 0.0
    private var centerLong:Double = 0.0

    private var storageReference: StorageReference? = null
    private var noticeDegree : String = "green"
    private var markerSize = 100

    val noticesList = ArrayList<NoticeModel>()
    val blackList = ArrayList<String>()

    private val markers = mutableListOf<Marker>()
    var markerSet: Hashtable<String, Boolean> = Hashtable<String, Boolean>()

    private var konumBulunduMu : Boolean = false
    private var noticeCredit = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val fab = activity?.findViewById<FloatingActionButton>(R.id.bottomBarFabMap)
        fab!!.isVisible = false
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMapsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?

        binding.progressBar.visibility = View.VISIBLE

        binding.overlayView.visibility = View.VISIBLE
        binding.overlayView.setOnClickListener(null)

        userLiveData()


        mapFragment?.getMapAsync {
                googleMap -> mMap = googleMap
                onMapReady(googleMap)

            storageReference = FirebaseStorage.getInstance().reference
            blackListViewModel = ViewModelProviders.of(this@MapsFragment).get(BlackListViewModel::class.java)
            blackListViewModel.getDataFromAPI()

            getBlackList()



            binding.mapSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
                androidx.appcompat.widget.SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    if (query != null) {
                        // Arama sonucunu işleme koymak için gereken işlemler burada yazılır
                        val geoCoder = Geocoder(context)
                        val addressList = geoCoder.getFromLocationName(query, 2)
                        if (addressList != null && addressList.isNotEmpty()) {
                            val address = addressList[0]
                            val latLng = LatLng(address.latitude, address.longitude)
                            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 14f)
                            googleMap.animateCamera(cameraUpdate)
                        }
                    }
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    return false
                }
            })

        }


        binding.btnToNoticesFragment.setOnClickListener {
            val intent = Intent(context, NearbyNoticesActivity::class.java)
            intent.putExtra("centerLat", centerLat)
            intent.putExtra("centerLong", centerLong)
            startActivity(intent)
        }
        binding.btnToAllNoticesFragment.setOnClickListener {
            val intent = Intent(context, tr.main.rearth.Activities.AllNoticesActivity::class.java)
            startActivity(intent)
        }
    }


    @SuppressLint("PotentialBehaviorOverride")
    override fun onMapReady(p0: GoogleMap) {
        mMap = p0

        try {
            var success = mMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(requireContext(),
                    R.raw.map_style)
            )
            if (!success){
                Log.e("TagMaps","Harita stili yuklenirken hata olustu")
            }
        }catch (e: Exception){
            Log.e("TagMaps","Duzgun yuklendi")
        }

        noticeViewModel = ViewModelProviders.of(this@MapsFragment).get(NoticeViewModel::class.java)
        noticeViewModel.getDataFromFirebase()

        locationLiveData(mMap)


        val databaseReference = FirebaseDatabase.getInstance().getReference("Notices")

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                noticesList.clear()
                for (data in snapshot.children) {
                    val noticeModel = data.getValue(NoticeModel::class.java)
                    noticeModel?.let {
                        noticesList.add(noticeModel)
                    }
                }

            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

        binding.fabShowSearcbar.setOnClickListener{
            if (binding.mapSearchView.visibility == View.GONE){
                binding.mapSearchView.visibility = View.VISIBLE
            }else{
                binding.mapSearchView.visibility = View.GONE
            }
        }


        locationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationListener = object : LocationListener{
            @SuppressLint("UseCompatLoadingForDrawables")
            override fun onLocationChanged(konum: Location) {
                // lokasyon,konum degisince yapilacak islemer
                binding.progressBar.visibility = View.GONE
                guncelKonum = LatLng(konum.latitude,konum.longitude)

                centerLat = konum.latitude
                centerLong = konum.longitude

                binding.fabAddLocation.setOnClickListener{

                    Toast.makeText(context,"tıklandı",Toast.LENGTH_SHORT).show()
                    val alertDialog = AlertDialog.Builder(context)
                    dialogBinding = BildiriDialogBinding.inflate(layoutInflater)
                    alertDialog.setView(dialogBinding.root)
                    dialog = alertDialog.create()
                    dialog.show()
                    noticeDegree = "green"
                    dialog.radioGroup.setOnCheckedChangeListener{ group , checkedId ->
                        if (checkedId == R.id.btnGreen){
                            dialogBinding.dialogContext.background = resources.getDrawable(R.drawable.border_dialog_green)
                            noticeDegree = "green"

                        }
                        if (checkedId == R.id.btnYellow){
                            dialogBinding.dialogContext.background = resources.getDrawable(R.drawable.border_dialog_yellow)
                            noticeDegree = "yellow"
                        }
                        if (checkedId == R.id.btnRed){
                            dialogBinding.dialogContext.background = resources.getDrawable(R.drawable.border_dialog_red)
                            noticeDegree = "red"
                        }

                    }

                    dialogBinding.messageEdittext.addTextChangedListener(object :
                        TextWatcher {
                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                            val inputText = s.toString().toLowerCase() // Girilen metni küçük harflere çevir
                            val words = inputText.split(" ")
                            val containsBlacklistWord = words.any { word -> blackList.contains(word) }// Girilen metin kara listedeki kelimelerden herhangi birini içeriyor mu?

                            if (containsBlacklistWord) {
                                dialogBinding.alertTxt.text = "Mesajınız uygunsuz ifadeler içermektedir."
                                dialogBinding.messageEdittext.background = resources.getDrawable(R.drawable.edittext_alert_border)
                                dialogBinding.alertTxt.visibility = View.VISIBLE
                            }else{
                                dialogBinding.alertTxt.text = "Lütfen bir mesaj giriniz."
                                dialogBinding.messageEdittext.background = resources.getDrawable(R.drawable.bildiri_edittext_bg)
                                dialogBinding.alertTxt.visibility = View.GONE
                            }
                        }

                        override fun afterTextChanged(s: Editable?) {}
                    })

                    dialogBinding.imgPickImage.setOnClickListener{
                        if (checkStoragePermission())
                            pickImage()
                        else storageRequestPermission()
                    }

                    dialogBinding.coordinatorLayout.setOnClickListener {
                        noticeImageUri = null
                        noticeImageUrl = ""
                        dialogBinding.coordinatorLayout.visibility = View.GONE
                    }

                    dialogBinding.bildiriOlusturBtn.setOnClickListener {
                        if (dialogBinding.messageEdittext.text.toString().isEmpty()){
                            dialogBinding.messageEdittext.background = resources.getDrawable(R.drawable.edittext_alert_border)
                            dialogBinding.alertTxt.visibility = View.VISIBLE
                        }else{

                            val timestamp = System.currentTimeMillis().toString()

                            if (noticeImageUri != null){
                                FirebaseStorage.getInstance().reference.child(userID + "/SharedPhotos/${timestamp}").putFile(noticeImageUri!!)
                                    .addOnSuccessListener {
                                        val task = it.storage.downloadUrl
                                        task.addOnCompleteListener { uri ->
                                            noticeImageUrl = uri.result.toString()
                                            val map = mapOf(
                                                "username" to username,
                                                "userID" to userID,
                                                "latitude" to konum.latitude.toString(),
                                                "longitude" to konum.longitude.toString(),
                                                "noticeID" to userID+timestamp,
                                                "noticeMessage" to dialogBinding.messageEdittext.text.toString(),
                                                "userImage" to userImage,
                                                "noticeDegree" to noticeDegree,
                                                "noticeTime" to timestamp,
                                                "noticeImage" to noticeImageUrl,
                                                "noticeRating" to "0"
                                            )
                                            FirebaseDatabase.getInstance().getReference("Notices").child(userID+timestamp).setValue(map)
                                        }
                                    }
                            }else{
                                val map = mapOf(
                                    "username" to username,
                                    "userID" to userID,
                                    "latitude" to konum.latitude.toString(),
                                    "longitude" to konum.longitude.toString(),
                                    "noticeID" to userID+timestamp,
                                    "noticeMessage" to dialogBinding.messageEdittext.text.toString(),
                                    "userImage" to userImage,
                                    "noticeDegree" to noticeDegree,
                                    "noticeTime" to timestamp,
                                    "noticeRating" to "0"
                                )

                                FirebaseDatabase.getInstance().getReference("Notices").child(userID+timestamp).setValue(map)
                            }

                            var referenceUsers =
                                FirebaseDatabase.getInstance().getReference("Users")


                            val databaseReferenceReceiver = FirebaseDatabase.getInstance().getReference("Users").child(userID)
                            databaseReferenceReceiver.addListenerForSingleValueEvent(object :
                                ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if (snapshot.exists()) {
                                        val userModel = snapshot.getValue(UserModel::class.java)
                                        if (userModel != null) {
                                            var noticesCount = (userModel.notices.toInt() + 1).toString()
                                            var noticeCredit = (userModel.noticeCredit) - 1
                                            val map = mapOf(
                                                "notices" to noticesCount,
                                                "noticeCredit" to noticeCredit
                                            )
                                            referenceUsers.child(userID).updateChildren(map)

                                        }
                                        //Picasso.get().load(userModel!!.image).into(activityMessageBinding.imgProfile)
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    TODO("Not yet implemented")
                                }
                            })


                            noticeImageUrl = ""
                            noticeImageUri = null

                            dialog.dismiss()

                        }
                    }
                }

                if (konumBulunduMu == false){
                    var baslangicKonum = mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(guncelKonum,15f))

                    baslangicKonum.apply {
                        baslangicKonum = mMap.moveCamera(CameraUpdateFactory.zoomTo(15f))
                    }
                    konumBulunduMu = true
                }

                binding.fabShowLocation.setOnClickListener {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(guncelKonum,15f))
                }

                mMap.setOnMarkerClickListener(this@MapsFragment)

                //mMap.uiSettings.isZoomGesturesEnabled = false

            }

            override fun onProviderDisabled(@NonNull provider: String) {

            }

            override fun onProviderEnabled(@NonNull provider: String) {

            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
                super.onStatusChanged(provider, status, extras)
            }

        }

        // set listeners for camera movements
        mMap.setOnCameraIdleListener(this)

        mMap.setInfoWindowAdapter(object :GoogleMap.InfoWindowAdapter{
            override fun getInfoContents(marker: Marker): View {
                TODO("Not yet implemented")
            }

            override fun getInfoWindow(marker: Marker): View {
                val view: View = layoutInflater.inflate(
                    R.layout.custom_infoview,
                    requireView().findViewById(R.id.map),
                    false
                )

                val imgInfo = view!!.findViewById<ImageView>(R.id.infoImageView)
                for (notice in noticesList) {
                    if (marker.snippet == notice.noticeID) {
                        view.infoName.text = notice.username
                        view.txtİnfoView.text = notice.noticeMessage
                        view.countRating.text = notice.noticeRating


                        var currentTime = System.currentTimeMillis()

                        val simpleDateFormat = SimpleDateFormat("kk:mm", Locale.getDefault())
                        val simpleDateFormat2 = SimpleDateFormat("kk:mm dd.MM.yyyy", Locale.getDefault())

                        val day = SimpleDateFormat("dd", Locale.getDefault())
                        val month = SimpleDateFormat("MM", Locale.getDefault())
                        val year = SimpleDateFormat("yyyy", Locale.getDefault())

                        val sharedNoticeDay = day.format(notice.noticeTime?.toLong())
                        val sharedNoticeMonth = month.format(notice.noticeTime?.toLong())
                        val sharedNoticeYear = year.format(notice.noticeTime?.toLong())

                        val currentDay = day.format(currentTime)
                        val currentMonth = month.format(currentTime)
                        val currentYear = year.format(currentTime)

                        val date = simpleDateFormat.format(notice.noticeTime?.toLong())
                        val date2 = simpleDateFormat2.format(notice.noticeTime?.toLong())


                        val now = Calendar.getInstance()
                        val noticeTimeMillis = notice.noticeTime?.toLong() ?: 0
                        val noticeTime = Calendar.getInstance().apply { timeInMillis = noticeTimeMillis }

                        val diffMillis = now.timeInMillis - noticeTime.timeInMillis
                        val diffMinutes = diffMillis / (1000 * 60)
                        var zaman = currentTime - notice.noticeTime!!.toLong()

                        if (zaman < 60_000) {
                            view.infoTime.text = "Şimdi"
                        }
                        else {
                            if (diffMinutes < 60 && diffMinutes >= 1){
                                view.infoTime.text = "${diffMinutes} dakika önce"
                            }else{
                                if(sharedNoticeDay == currentDay && sharedNoticeMonth == currentMonth && sharedNoticeYear == currentYear){
                                    view.infoTime.text = "Bugün ${date.toString()}"
                                }else{
                                    view.infoTime.text = date2.toString()

                                }
                            }
                        }

                        if (notice.noticeDegree == "green"){
                            view.linearContext.background = this@MapsFragment.resources.getDrawable(R.drawable.border_notice_detail_green)
                            view.countRating.setTextColor(ContextCompat.getColor(requireContext(), R.color.main_green))
                            view.imgRating.setColorFilter(ContextCompat.getColor(requireContext(), R.color.main_green), PorterDuff.Mode.SRC_IN)
                        }
                        if (notice.noticeDegree == "yellow"){
                            view.linearContext.background = this@MapsFragment.resources.getDrawable(R.drawable.border_notice_detail_yellow)
                            view.countRating.setTextColor(ContextCompat.getColor(requireContext(), R.color.mainYellow))
                            view.imgRating.setColorFilter(ContextCompat.getColor(requireContext(), R.color.mainYellow), PorterDuff.Mode.SRC_IN)
                        }
                        if (notice.noticeDegree == "red"){
                            view.linearContext.background = this@MapsFragment.resources.getDrawable(R.drawable.border_notice_detail_red)
                            view.countRating.setTextColor(ContextCompat.getColor(requireContext(), R.color.main_red))
                            view.imgRating.setColorFilter(ContextCompat.getColor(requireContext(), R.color.main_red), PorterDuff.Mode.SRC_IN)
                        }
                        Glide.with(requireContext()).load(notice.userImage).into(imgInfo)
                    }
                }

                return view
            }

        })

        if (ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                AppConstants.LOCATION_PERMISSION)
        }else{
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,1,1f,locationListener)
        }



    }

    private fun updateCounter(zoom: Int) {
        markerSize = when (zoom) {
            1 -> 1
            2 -> 1
            3 -> 1
            4 -> 30
            5 -> 35
            6 -> 40
            7 -> 45
            8 -> 50
            9 -> 55
            10 -> 60
            11 -> 65
            12 -> 70
            13 -> 75
            14 -> 80
            15 -> 85
            16 -> 90
            17 -> 95
            else -> 100
        }
    }


    fun locationLiveData(mMap : GoogleMap){
        noticeViewModel.noticesLiveData.observe(viewLifecycleOwner, Observer { notices ->
            notices?.let{
                if (notices.isNotEmpty()){
                    for (notice in notices){

                        val databaseReference = FirebaseDatabase.getInstance().getReference("Notices").child(notice.noticeID.toString())

                        val currentMillis = System.currentTimeMillis()
                        val noticeMillis = notice.noticeTime?.toLongOrNull() ?: 0L

                        if ((currentMillis - noticeMillis) > (24 * 60 * 60 * 1000)) { // 24 saatlik süre milisaniye cinsinden hesaplanır
                            databaseReference.removeValue()
                            for (marker in markers) {
                                if (marker.snippet == notice.noticeID) {
                                    marker.remove()
                                }
                            }
                        }else{
                            var markerBitmap: Bitmap
                            // var color = BitmapDescriptorFactory.HUE_GREEN
                            if (notice.noticeDegree == "green"){
                                // color = BitmapDescriptorFactory.HUE_GREEN
                                markerBitmap = BitmapFactory.decodeResource(resources, R.drawable.marker_green)
                                val resizedMarkerBitmap = Bitmap.createScaledBitmap(markerBitmap, markerSize, markerSize, false)
                                val markerOptions = MarkerOptions()
                                    .position(LatLng(ParseDouble(notice.latitude),ParseDouble(notice.longitude)))
                                    .title(notice.userID)
                                    .icon(BitmapDescriptorFactory.fromBitmap(resizedMarkerBitmap))
                                    .snippet(notice.noticeID)
                                val marker = mMap.addMarker(markerOptions)

                                marker?.tag = "green"
                                markerSet[marker?.id.toString()] = false

                                marker?.let { it1 -> markers.add(it1) }
                            }
                            if (notice.noticeDegree == "yellow"){
                                // color = BitmapDescriptorFactory.HUE_YELLOW
                                markerBitmap = BitmapFactory.decodeResource(resources, R.drawable.marker_yellow)
                                val resizedMarkerBitmap = Bitmap.createScaledBitmap(markerBitmap, markerSize, markerSize, false)
                                val markerOptions = MarkerOptions()
                                    .position(LatLng(ParseDouble(notice.latitude),ParseDouble(notice.longitude)))
                                    .title(notice.userID)
                                    .icon(BitmapDescriptorFactory.fromBitmap(resizedMarkerBitmap))
                                    .snippet(notice.noticeID)
                                val marker = mMap.addMarker(markerOptions)

                                marker?.tag = "yellow"
                                markerSet[marker?.id.toString()] = false
                                marker?.let { it1 -> markers.add(it1) }
                            }
                            if (notice.noticeDegree == "red"){
                                // color = BitmapDescriptorFactory.HUE_RED
                                markerBitmap = BitmapFactory.decodeResource(resources, R.drawable.marker_red)
                                val resizedMarkerBitmap = Bitmap.createScaledBitmap(markerBitmap, markerSize, markerSize, false)
                                val markerOptions = MarkerOptions()
                                    .position(LatLng(ParseDouble(notice.latitude),ParseDouble(notice.longitude)))
                                    .title(notice.userID)
                                    .icon(BitmapDescriptorFactory.fromBitmap(resizedMarkerBitmap))
                                    .snippet(notice.noticeID)
                                val marker = mMap.addMarker(markerOptions)

                                marker?.tag = "red"
                                markerSet[marker?.id.toString()] = false

                                marker?.let { it1 -> markers.add(it1) }


                            }
                        }

                    }

                }

            }


        })

        noticeViewModel.noticeError.observe(viewLifecycleOwner, Observer { error->

            error?.let{
                if (it){
                    Toast.makeText(context,"hata çıktı",Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    fun userLiveData(){
        profileViewModels = ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity()!!.application).create(
            ProfileViewModel::class.java)

        profileViewModels.getUser().observe(viewLifecycleOwner, Observer {  userModel ->
            username = userModel.username
            userImage = userModel.image
            userID = userModel.uid
            noticeCredit = userModel.noticeCredit
            binding.noticeCredit.text = "${userModel.noticeCredit}/3"

            if (noticeCredit == 0){
                binding.fabAddLocation.visibility = View.INVISIBLE
                binding.fabAddLocationPale.visibility = View.VISIBLE
            }else{
                binding.fabAddLocationPale.visibility = View.INVISIBLE
                binding.fabAddLocation.visibility = View.VISIBLE
            }
        })
    }


    fun getBlackList(){
        blackListViewModel.blackList.observe(viewLifecycleOwner, Observer { list->

            list?.let{
                blackList.addAll(list)
                /*
                for (list in blackList){
                    Log.i("blacklist",list)
                }
                 */
            }

        })
    }

    @SuppressLint("PotentialBehaviorOverride")
    override fun onMarkerClick(mymarker: Marker): Boolean {

        mymarker.showInfoWindow()


        val delayMillis = 1000L // 3 saniye
        val runnable = Runnable {
            mymarker.hideInfoWindow()
            mymarker.showInfoWindow()
        }

        Handler(Looper.getMainLooper()).postDelayed(runnable, delayMillis)



        mMap.setOnInfoWindowClickListener { clickedMarker ->
            if (mymarker == clickedMarker) {
                // InfoWindow'a tıklandığında yapılacak işlemler
                val intent = Intent(requireContext(), NoticeDetailActivity::class.java)
                intent.putExtra("hisId", mymarker.title.toString())
                intent.putExtra("noticeID",mymarker.snippet.toString())
                requireContext().startActivity(intent)
            }
        }

        return true
    }

    fun ParseDouble(strNumber: String?): Double {
        return if (strNumber != null && strNumber.length > 0) {
            try {
                return strNumber.toDouble()
            } catch (e: java.lang.Exception) {
                (-1).toDouble() // or some value to mark this field is wrong. or make a function validates field first ...
            }
        } else {
            return 0.0
        }
    }

    private fun checkStoragePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun storageRequestPermission() = ActivityCompat.requestPermissions(
        requireActivity(),
        arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ), 1000
    )

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1000 ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    pickImage()
                else Toast.makeText(requireContext(), "Storage permission denied", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                val result = CropImage.getActivityResult(data)
                if (resultCode == Activity.RESULT_OK) {
                    noticeImageUri = result.uri
                    dialogBinding.noticeImage.setImageURI(noticeImageUri)
                    dialogBinding.coordinatorLayout.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun pickImage() {
        context?.let {
            CropImage.activity()
                .setCropMenuCropButtonTitle(resources.getString(R.string.crop_image_save_ok))
                .setCropShape(CropImageView.CropShape.RECTANGLE)
                .setAspectRatio(4, 3)
                .start(it, this)
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        val fab = activity?.findViewById<FloatingActionButton>(R.id.bottomBarFabMap)
        fab!!.isVisible = true
    }

    override fun onCameraIdle() {
        updateCounter(mMap.cameraPosition.zoom.roundToInt())
        for (marker in markers) {
            if (marker.tag == "green"){
                val markerBitmap = BitmapFactory.decodeResource(resources, R.drawable.marker_green)
                val resizedMarkerBitmap = Bitmap.createScaledBitmap(markerBitmap, markerSize, markerSize, false)
                marker.setIcon(BitmapDescriptorFactory.fromBitmap(resizedMarkerBitmap))
            }
            else if(marker.tag == "yellow"){
                val markerBitmap = BitmapFactory.decodeResource(resources, R.drawable.marker_yellow)
                val resizedMarkerBitmap = Bitmap.createScaledBitmap(markerBitmap, markerSize, markerSize, false)
                marker.setIcon(BitmapDescriptorFactory.fromBitmap(resizedMarkerBitmap))
            }else{
                val markerBitmap = BitmapFactory.decodeResource(resources, R.drawable.marker_red)
                val resizedMarkerBitmap = Bitmap.createScaledBitmap(markerBitmap, markerSize, markerSize, false)
                marker.setIcon(BitmapDescriptorFactory.fromBitmap(resizedMarkerBitmap))
            }

        }
    }
    fun checkIfFragmentAttached(operation: Context.() -> Unit) {
        if (isAdded && context != null) {
            operation(requireContext())
        }
    }


}