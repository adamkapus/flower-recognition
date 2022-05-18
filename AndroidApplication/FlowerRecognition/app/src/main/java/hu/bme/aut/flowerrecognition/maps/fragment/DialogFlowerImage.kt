package hu.bme.aut.flowerrecognition.maps.fragment

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatDialogFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import hu.bme.aut.flowerrecognition.databinding.DialogFlowerImageBinding

private const val ARG_PARAM1 = "displayName"
private const val ARG_PARAM2 = "imageURI"

class DialogFlowerImage : AppCompatDialogFragment() {
    private lateinit var binding: DialogFlowerImageBinding

    private var displayName: String? = null
    private var imageURI: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, 0)
        arguments?.let {
            displayName = it.getString(ARG_PARAM1)
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

        binding.flowerImageName.text = displayName

        if (imageURI != null) {
            context?.let {
                Glide.with(it).load(imageURI).listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        binding.imgLoadProgressbar.visibility = View.GONE;
                        return false;
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        binding.imgLoadProgressbar.visibility = View.GONE;
                        return false;
                    }
                }).into(binding.flowerImage)
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
        fun newInstance(flowerName: String?, imageURI: String?) =
            DialogFlowerImage().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, flowerName)
                    putString(ARG_PARAM2, imageURI)
                }
            }
    }

}
