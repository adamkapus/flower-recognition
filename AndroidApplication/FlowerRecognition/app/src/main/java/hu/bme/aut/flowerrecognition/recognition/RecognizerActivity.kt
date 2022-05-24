package hu.bme.aut.flowerrecognition.recognition

import android.Manifest
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.storage.FirebaseStorage
import hu.bme.aut.flowerrecognition.R
import hu.bme.aut.flowerrecognition.databinding.ActivityRecognizerBinding
import hu.bme.aut.flowerrecognition.maps.MapsActivity
import hu.bme.aut.flowerrecognition.ml.ConvModMeta
import hu.bme.aut.flowerrecognition.recognition.ui.RecognitionAdapter
import hu.bme.aut.flowerrecognition.recognition.util.YuvToRgbConverter
import hu.bme.aut.flowerrecognition.recognition.viewmodel.Recognition
import hu.bme.aut.flowerrecognition.recognition.viewmodel.RecognitionViewModel
import hu.bme.aut.flowerrecognition.recognition.viewmodel.StateOfRecognition
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.model.Model
import java.io.ByteArrayOutputStream
import java.net.URLEncoder
import java.util.*
import java.util.concurrent.Executors

class RecognizerActivity : AppCompatActivity() {

    companion object {
        private const val PERMISSIONS_REQUEST_CAMERA = 999
        private const val PERMISSIONS_ACCESS_FINE_LOCATION = 1
        private const val TAG = "Recognizer Activity"
    }

    private lateinit var binding: ActivityRecognizerBinding
    private lateinit var submitButton: Button
    private lateinit var startButton: Button

    // CameraX variables
    private lateinit var preview: Preview
    private lateinit var imageAnalyzer: ImageAnalysis // Analysis use case, for running ML code
    private lateinit var camera: Camera
    private val cameraExecutor = Executors.newSingleThreadExecutor()

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private val recogViewModel: RecognitionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecognizerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        title = "Recognizer"

        submitButton = binding.submitButton
        startButton = binding.startCameraButton

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        val viewAdapter = RecognitionAdapter(this)
        binding.recognitionResults.adapter = viewAdapter
        binding.recognitionResults.itemAnimator = null

        //feliratkozunk a viewmodel felismeréslistájára, ha frissül, akkor az adapterbe submiteljük
        recogViewModel.recognitionList.observe(this,
            Observer {
                viewAdapter.submitList(it)
            }
        )

        //feliratkozunk a viewmodelnél arra, hogy milyen állapotban van a felismerés
        recogViewModel.stateOfRecognition.observe(this,
            Observer {
                when (it) {
                    StateOfRecognition.READY_TO_START -> {
                        //ha készen állunk a kezdésra -> submit gomb nem kattintható, start gomb kattintható
                        submitButton.isEnabled = false; startButton.isEnabled = true
                    }
                    StateOfRecognition.IN_PROGRESS -> {
                        //ha folyamatban van -> egyik gomb se kattintható, elindítjuk a kamerát
                        submitButton.isEnabled = false; startButton.isEnabled = false; startCamera()
                    }
                    StateOfRecognition.FINISHED -> {
                        //ha befejeződött -> mindkét gomb kattintható, leállítjuk a kamerát
                        submitButton.isEnabled = true; startButton.isEnabled = true; stopCamera()
                    }
                }
            }
        )

        submitButton.setOnClickListener {
            handleSubmittingFlower()
        }

        startButton.setOnClickListener {
            handleStartingRecognition()
        }

    }

    //start gomb megnyomására hívódik, elindítjuk a felismerést, ha van engedélyünk a kamera használatára;  ha nincs, akkor kérünk engedélyt
    private fun handleStartingRecognition() {
        if (permissionGranted(arrayOf(Manifest.permission.CAMERA))) {
            recogViewModel.startRecognition()
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.CAMERA
            )
        ) {
            showRationaleDialog(
                explanation = R.string.camera_permission_explanation,
                onNegativeButton = {
                    Toast.makeText(
                        this,
                        getString(R.string.permission_deny_camera_text),
                        Toast.LENGTH_SHORT
                    ).show()
                },
                onPositiveButton = {
                    (this::requestPermission)(
                        arrayOf(Manifest.permission.CAMERA),
                        PERMISSIONS_REQUEST_CAMERA
                    )
                }
            )

        } else {
            requestPermission(arrayOf(Manifest.permission.CAMERA), PERMISSIONS_REQUEST_CAMERA)
        }
    }


    //submit gomb megnyomására hívódik, ha nincs engedélyünk a lokációra, akkor elkéri, egyébként folytatja a submit folyamatot
    private fun handleSubmittingFlower() {
        if (permissionGranted(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))) {
            uploadFlowerImage()
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            showRationaleDialog(
                explanation = R.string.location_permission_submit_explanation,
                onNegativeButton = {
                    Toast.makeText(
                        this,
                        getString(R.string.permission_deny_location_submit_text),
                        Toast.LENGTH_SHORT
                    ).show()
                },
                onPositiveButton = {
                    (this::requestPermission)(
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        PERMISSIONS_ACCESS_FINE_LOCATION
                    )
                }
            )

        } else {
            requestPermission(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_ACCESS_FINE_LOCATION
            )
        }

    }


    private fun permissionGranted(permissions: Array<String>): Boolean = permissions.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun showRationaleDialog(
        @StringRes title: Int = R.string.rationale_dialog_title,
        @StringRes explanation: Int,
        onPositiveButton: () -> Unit,
        onNegativeButton: () -> Unit = this::finish
    ) {
        val alertDialog = AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(explanation)
            .setCancelable(false)
            .setPositiveButton(R.string.proceed) { dialog, id ->
                dialog.cancel()
                onPositiveButton()
            }
            .setNegativeButton(R.string.exit) { dialog, id -> onNegativeButton() }
            .create()
        alertDialog.show()
    }

    private fun requestPermission(permission: Array<String>, requestCode: Int) {
        ActivityCompat.requestPermissions(
            this,
            permission,
            requestCode
        )
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            PERMISSIONS_REQUEST_CAMERA -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    recogViewModel.startRecognition()
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.permission_deny_camera_text),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return
            }

            PERMISSIONS_ACCESS_FINE_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    uploadFlowerImage()
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.permission_deny_location_submit_text),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return
            }
        }
    }

    //kép feltöltése Firebase Storage-ba, majd az URL-je birtokában továbbhívás a submitFlower-re
    private fun uploadFlowerImage(){
        val bitmap: Bitmap? = binding.viewFinder.bitmap

        if(bitmap == null) {
            submitFlower()
        }

        val baos = ByteArrayOutputStream()
        bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val imageInBytes = baos.toByteArray()

        val storageReference = FirebaseStorage.getInstance().reference
        val newImageName = URLEncoder.encode(UUID.randomUUID().toString(), "UTF-8") + ".jpg"
        val newImageRef = storageReference.child("images/$newImageName")

        showProgressDialog()

        newImageRef.putBytes(imageInBytes)
            .addOnFailureListener { _ ->
                hideProgressDialog()
                Toast.makeText(
                    this,
                    getString(R.string.unsuccesful_image_upload),
                    Toast.LENGTH_SHORT
                ).show()
            }
            .continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                }

                newImageRef.downloadUrl
            }
            .addOnSuccessListener { downloadUri ->
                hideProgressDialog()
                submitFlower(downloadUri.toString())
            }
    }


    //lokáció meghatározása, majd a viewmodellen továbbhívás a virág beküldésére
    @SuppressLint("MissingPermission")
    private fun submitFlower(imageURL: String? = null) {


        val locationResult = fusedLocationProviderClient.lastLocation
        locationResult.addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                val location = task.result
                if (location != null) {
                    recogViewModel.submitFlower(location.latitude, location.longitude, imageURL)
                }
            } else {
                Log.d(TAG, "Current location is null.")
            }
        }
    }

    //kamera indítása, preview use case és image analyzer use case inicializálása, preview beállítása a PreviewView-ra
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(Runnable {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            preview = Preview.Builder()
                .build()

            imageAnalyzer = ImageAnalysis.Builder()
                // This sets the ideal size for the image to be analyse, CameraX will choose the
                // the most suitable resolution which may not be exactly the same or hold the same
                // aspect ratio
                .setTargetResolution(Size(224, 224))
                // How the Image Analyser should pipe in input, 1. every frame but drop no frame, or
                // 2. go to the latest frame and may drop some frame. The default is 2.
                // STRATEGY_KEEP_ONLY_LATEST. The following line is optional, kept here for clarity
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { analysisUseCase: ImageAnalysis ->
                    analysisUseCase.setAnalyzer(cameraExecutor, ImageAnalyzer(this) { items ->
                        // updating the list of recognised objects
                        recogViewModel.addRecognition(items)
                    })
                }

            // Select camera, back is the default. If it is not available, choose front camera
            val cameraSelector =
                if (cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA))
                    CameraSelector.DEFAULT_BACK_CAMERA else CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera - try to bind everything at once and CameraX will find
                // the best combination.
                camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalyzer
                )

                // Attach the preview to preview view, aka View Finder
                preview.setSurfaceProvider(binding.viewFinder.surfaceProvider)
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    //use casek unbindolása
    private fun stopCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
        cameraProvider.unbindAll()
    }

    //ImageAnalysis.analizer implementálása, amiben a kép alapján a TF Lite modellt futtatjuk
    private class ImageAnalyzer(ctx: Context, private val listener: (recognition: List<Recognition>) -> Unit ) :
        ImageAnalysis.Analyzer {


        private val flowerModel: ConvModMeta by lazy {

            val compatList = CompatibilityList()

            val options = if (compatList.isDelegateSupportedOnThisDevice) {
                Log.d(TAG, "This device is GPU Compatible ")
                Model.Options.Builder().setDevice(Model.Device.GPU).build()
            } else {
                Log.d(TAG, "This device is GPU Incompatible ")
                Model.Options.Builder().setNumThreads(4).build()
            }

            // Initialize the Flower Model
            ConvModMeta.newInstance(ctx, options)
        }

        override fun analyze(imageProxy: ImageProxy) {

            val items = mutableListOf<Recognition>()

            // Convert Image to Bitmap then to TensorImage
            val tfImage = TensorImage.fromBitmap(toBitmap(imageProxy))

            //Process the image using the trained model, sort and pick out the top results
            val outputs = flowerModel.process(tfImage)
                .probabilityAsCategoryList.apply {
                    sortByDescending { it.score } // Sort with highest confidence first
                }// take the top results

            //Converting the top probability items into a list of recognitions
            for (output in outputs) {
                items.add(Recognition(output.label, output.score))
            }


            // Return the result
            listener(items.toList())

            // Close the image,this tells CameraX to feed the next image to the analyzer
            imageProxy.close()
        }

        /**
         * Convert Image Proxy to Bitmap
         */
        private val yuvToRgbConverter = YuvToRgbConverter(ctx)
        private lateinit var bitmapBuffer: Bitmap
        private lateinit var rotationMatrix: Matrix

        @SuppressLint("UnsafeExperimentalUsageError", "UnsafeOptInUsageError")
        private fun toBitmap(imageProxy: ImageProxy): Bitmap? {

            val image = imageProxy.image ?: return null

            // Initialise Buffer
            if (!::bitmapBuffer.isInitialized) {
                // The image rotation and RGB image buffer are initialized only once
                Log.d(TAG, "Initalise toBitmap()")
                rotationMatrix = Matrix()
                rotationMatrix.postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())
                bitmapBuffer = Bitmap.createBitmap(
                    imageProxy.width, imageProxy.height, Bitmap.Config.ARGB_8888
                )
            }

            // Pass image to an image analyser
            yuvToRgbConverter.yuvToRgb(image, bitmapBuffer)

            // Create the Bitmap in the correct orientation
            return Bitmap.createBitmap(
                bitmapBuffer,
                0,
                0,
                bitmapBuffer.width,
                bitmapBuffer.height,
                rotationMatrix,
                false
            )
        }

    }

    //progress dialog képfeltöltés közbenre
    private var progressDialog: ProgressDialog? = null

    private fun showProgressDialog() {
        if (progressDialog != null) {
            return
        }

        progressDialog = ProgressDialog(this).apply {
            setCancelable(false)
            setMessage("Uploading image...")
            show()
        }
    }

    private fun hideProgressDialog() {
        progressDialog?.let { dialog ->
            if (dialog.isShowing) {
                dialog.dismiss()
            }
        }
        progressDialog = null
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_toolbar, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_recognizer -> {
                true
            }
            R.id.menu_map -> {
                val mapsIntent = Intent(this, MapsActivity::class.java)
                startActivity(mapsIntent)
                finish()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

}
