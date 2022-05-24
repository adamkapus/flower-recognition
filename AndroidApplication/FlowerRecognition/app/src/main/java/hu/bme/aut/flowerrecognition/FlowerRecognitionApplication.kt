package hu.bme.aut.flowerrecognition

import android.app.Application
import hu.bme.aut.flowerrecognition.data.FlowerLocationRepository

//Applikáció szinten egy repository osztály legyen használva, DI jobb lenne
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