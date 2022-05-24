package hu.bme.aut.flowerrecognition.maps

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Paint
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import hu.bme.aut.flowerrecognition.R
import hu.bme.aut.flowerrecognition.data.model.FlowerLocation
import hu.bme.aut.flowerrecognition.databinding.ActivityMapsBinding
import hu.bme.aut.flowerrecognition.maps.fragment.DialogFlowerImage
import hu.bme.aut.flowerrecognition.maps.model.FlowerOnMap
import hu.bme.aut.flowerrecognition.maps.viewmodel.MapsViewModel
import hu.bme.aut.flowerrecognition.recognition.RecognizerActivity
import hu.bme.aut.flowerrecognition.util.FlowerResolver
import hu.bme.aut.flowerrecognition.util.Rarity

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {
    companion object {
        private const val TAG = "Maps Activity"
        private const val DEFAULT_ZOOM = 15 // alap zoom a térképen
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    }

    private lateinit var binding: ActivityMapsBinding

    private lateinit var mMap: GoogleMap

    private var locationPermissionGranted = false //megkaptuk-e az engedélyt a lokációra
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var lastKnownLocation: Location? = null //utolsó ismert location
    private val defaultLocation = LatLng(47.47330488455458, 19.05904249897747) //alap location BME Q épületre

    private var markers = HashMap<Marker, FlowerOnMap>() //Markerek és a hozzájuk tartozó virág adatok map-je

    private var flowerResolver = FlowerResolver()

    private val mapsViewModel: MapsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        title = "Map"

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        //feliratkozunk a látható ritkaságokra
        mapsViewModel.viewableRarities.observe(this,
            Observer {
                onViewableRaritiesChanged(it)
            }
        )

        binding.fabRefresh.setOnClickListener { _ ->
            mapsViewModel.refresh()
        }

        binding.checkboxCommon.setOnClickListener { view ->
            if (view is CheckBox) onCheckBoxClick(
                Rarity.COMMON,
                view.isChecked
            )
        }
        binding.checkboxRare.setOnClickListener { view ->
            if (view is CheckBox) onCheckBoxClick(
                Rarity.RARE,
                view.isChecked
            )
        }
        binding.checkboxSuperRare.setOnClickListener { view ->
            if (view is CheckBox) onCheckBoxClick(
                Rarity.SUPER_RARE,
                view.isChecked
            )
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.setInfoWindowAdapter(FlowerInfoWindowAdapter())
        mMap.setOnInfoWindowClickListener(this)

        //feliratkozunk a virágokra, változás esetén kirajzoljuk
        mapsViewModel.flowers.observe(this,
            Observer {
                Log.d(TAG, it.toString())
                drawFlowersOnMap(it)
            }
        )

        //lokáció engedély elkérése
        getLocationPermission()

        //Google Maps UI frissítése
        updateLocationUI()

        //lokáció meghatározása
        getDeviceLocation()

        mapsViewModel.refresh()

        Log.d(TAG, "OnMapReady")
    }

    private fun onCheckBoxClick(rarity: Rarity, isChecked: Boolean) {
        mapsViewModel.modifyRarities(rarity, isChecked)
    }


    //akkor hívódik, ha változik a látható ritkaságok listája
    private fun onViewableRaritiesChanged(viewableRarities: Set<Rarity>) {
        for (m in markers.entries) {
            val f = m.value
            m.key.isVisible = viewableRarities.contains(f.rarity)
        }
    }

    //virágok kirajzolása a map-re
    private fun drawFlowersOnMap(flowers: List<FlowerLocation>) {
        //az eddig a mapen lévő markereket töröljük
        for (m in markers.keys) {
            m.remove()
        }
        //a [marker, FlowerOnMap] hashmap-et töröljük
        markers.clear()

        for (f in flowers) {

            val pos = LatLng(f.Lat!!.toDouble(), f.Lng!!.toDouble())

            val icon: Int = when (f.rarity) {
                Rarity.COMMON.toString() -> R.drawable.ic_map_flower_common
                Rarity.RARE.toString() -> R.drawable.ic_map_flower_rare
                Rarity.SUPER_RARE.toString() -> R.drawable.ic_map_flower_super_rare
                else -> {
                    R.drawable.ic_map_flower_common
                }
            }

            val marker = mMap.addMarker(
                MarkerOptions().position(pos).title(f.name)
                    .icon(BitmapDescriptorFactory.fromResource(icon))
            )
            if (marker != null) {
                val rarity: Rarity = if (f.rarity == null) {
                    Rarity.COMMON
                } else {
                    Rarity.valueOf(f.rarity)
                }
                //betesszük a HashMap-be a markert és a FlowerOnMap-pé konvertált virágot
                markers[marker] = FlowerOnMap(
                    name = f.name,
                    Lat = f.Lat,
                    Lng = f.Lng,
                    imageUrl = f.imageUrl,
                    displayName = getString(flowerResolver.getDisplayName(f.name)),
                    rarity = rarity
                )
            }
        }

        //látható rarityk beállítása
        mapsViewModel.viewableRarities.value?.let { onViewableRaritiesChanged(it) }
    }

    //InfoWindow adapter az egyedi InfoWindowokért a térképen
    internal inner class FlowerInfoWindowAdapter : GoogleMap.InfoWindowAdapter {

        private val window: View = layoutInflater.inflate(R.layout.flower_info_window, null)

        //ezt a függvényt akkor kéne megvalósítani, ha az infowindow tartalmát akarnánk csak felülírni, de mi a kinézetét is szeretnénk
        override fun getInfoContents(marker: Marker): View? {
            return null
        }

        override fun getInfoWindow(marker: Marker): View? {
            render(marker, window)
            return window
        }

        //beállítjuk az egyedi info window kinézetét
        private fun render(marker: Marker, view: View) {
            val title: String? = markers[marker]?.displayName
            val titleUi = view.findViewById<TextView>(R.id.title)
            titleUi.text = title

            val snippetUi = view.findViewById<TextView>(R.id.snippet)
            val lat = String.format("%.3f", markers[marker]?.Lat)
            val lng = String.format("%.3f", markers[marker]?.Lng)
            snippetUi.text = getString(R.string.lat_lng, lat, lng)

            val viewImagetv = view.findViewById<TextView>(R.id.view_image_tv)
            viewImagetv.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        }

    }

    //egy info windowra való klikkelés esetén egy Fragmenst dobunk fel ami megjeleníti a virág képét
    override fun onInfoWindowClick(marker: Marker) {
        val flowerName = markers[marker]?.displayName
        val imageUrl = markers[marker]?.imageUrl
        DialogFlowerImage.newInstance(flowerName, imageUrl).show(
            supportFragmentManager, DialogFlowerImage::class.java.simpleName
        )
    }

    private fun getLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionGranted = true
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        locationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {

                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    locationPermissionGranted = true
                    updateLocationUI()
                    getDeviceLocation()
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    //ha kapunk lokációt, odamozgatjuk a kamerát, egyébként a default lokációra
    @SuppressLint("MissingPermission")
    private fun getDeviceLocation() {
        try {
            if (locationPermissionGranted) {
                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        lastKnownLocation = task.result
                        if (lastKnownLocation != null) {
                            mMap.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(
                                        lastKnownLocation!!.latitude,
                                        lastKnownLocation!!.longitude
                                    ), DEFAULT_ZOOM.toFloat()
                                )
                            )
                        }
                    } else {
                        Log.d(TAG, "Current location is null. Using defaults.")
                        Log.e(TAG, "Exception: %s", task.exception)
                        mMap.moveCamera(
                            CameraUpdateFactory
                                .newLatLngZoom(defaultLocation, DEFAULT_ZOOM.toFloat())
                        )
                        mMap.uiSettings.isMyLocationButtonEnabled = false
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    //Térkép UI frissítése, ha kaptunk engedélyt a lokációra
    @SuppressLint("MissingPermission")
    private fun updateLocationUI() {
        try {
            if (locationPermissionGranted) {
                mMap.isMyLocationEnabled = true
                mMap.uiSettings.isMyLocationButtonEnabled = true
            } else {
                mMap.isMyLocationEnabled = false
                mMap.uiSettings.isMyLocationButtonEnabled = false
                lastKnownLocation = null
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_toolbar, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_recognizer -> {
                val recognizerIntent = Intent(this, RecognizerActivity::class.java)
                startActivity(recognizerIntent)
                finish()
                true
            }
            R.id.menu_map -> {
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}