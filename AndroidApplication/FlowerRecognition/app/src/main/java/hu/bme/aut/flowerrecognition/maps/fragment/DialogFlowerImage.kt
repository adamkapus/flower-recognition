package hu.bme.aut.flowerrecognition.maps.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialogFragment
import com.bumptech.glide.Glide
import hu.bme.aut.flowerrecognition.databinding.DialogFlowerImageBinding

private const val ARG_PARAM1 = "flowerName"
private const val ARG_PARAM2 = "imageURI"

class DialogFlowerImage : AppCompatDialogFragment() {
    private lateinit var binding: DialogFlowerImageBinding

    private var flowerName: String? = null
    private var imageURI: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, 0)
        arguments?.let {
            flowerName = it.getString(ARG_PARAM1)
            imageURI = it.getString(ARG_PARAM2)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        binding = DialogFlowerImageBinding.inflate(LayoutInflater.from(context))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding.flowerImageName.text = flowerName

        if (imageURI != null) {
            context?.let {
                Glide.with(it).load(imageURI).into(binding.flowerImage)
            }
        } else {
            context?.let {
                Glide.with(it).load("https://picsum.photos/400/600").into(binding.flowerImage)
            }
        }

        binding.flowerCloseBtn.setOnClickListener {
            activity?.supportFragmentManager?.beginTransaction()
                ?.remove(this)?.commit();
        }

        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance(flowerName: String, imageURI: String?) =
            DialogFlowerImage().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, flowerName)
                    putString(ARG_PARAM2, imageURI)
                }
            }
    }

}
