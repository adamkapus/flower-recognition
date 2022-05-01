package hu.bme.aut.flowerrecognition.maps.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialogFragment
import com.bumptech.glide.Glide
import hu.bme.aut.flowerrecognition.databinding.DialogFlowerImageBinding

class DialogFlowerImage : AppCompatDialogFragment() {
    private lateinit var binding: DialogFlowerImageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, 0)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        binding = DialogFlowerImageBinding.inflate(LayoutInflater.from(context))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding.flowerImageName.text = "Szuper viragnev"

        context?.let {
            Glide.with(it).load("https://picsum.photos/400/600").into(binding.flowerImage)
        }

        binding.flowerCloseBtn.setOnClickListener {
            activity?.supportFragmentManager?.beginTransaction()
                ?.remove(this)?.commit();
        }

        return binding.root
    }

}
