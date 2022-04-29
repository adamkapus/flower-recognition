package hu.bme.aut.flowerrecognition

import android.app.Application
import hu.bme.aut.flowerrecognition.data.FlowerLocationRepository

class FlowerRecognitionApplication : Application() {

    companion object {
        lateinit var flowerLocationRepository: FlowerLocationRepository
            private set
    }

    override fun onCreate() {
        super.onCreate()
        flowerLocationRepository = FlowerLocationRepository()
    }
}