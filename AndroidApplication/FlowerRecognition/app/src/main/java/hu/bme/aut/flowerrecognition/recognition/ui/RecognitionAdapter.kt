package hu.bme.aut.flowerrecognition.recognition.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import hu.bme.aut.flowerrecognition.databinding.RecognitionItemBinding
import hu.bme.aut.flowerrecognition.recognition.viewmodel.Recognition
import hu.bme.aut.flowerrecognition.util.FlowerResolver

class RecognitionAdapter(private val ctx: Context) :
    ListAdapter<Recognition, RecognitionAdapter.RecognitionViewHolder>(RecognitionDiffUtil()) {

    private val flowerResolver : FlowerResolver = FlowerResolver()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecognitionViewHolder {
        val inflater = LayoutInflater.from(ctx)
        val binding = RecognitionItemBinding.inflate(inflater, parent, false)
        return RecognitionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecognitionViewHolder, position: Int) {
        //holder.bindTo(getItem(position))
        val recognition :Recognition = getItem(position)
        holder.displayName.text = holder.itemView.context.getString(flowerResolver.getDisplayName(recognition.label))
        holder.probability.text = recognition.probabilityString;
    }

    private class RecognitionDiffUtil : DiffUtil.ItemCallback<Recognition>() {
        override fun areItemsTheSame(oldItem: Recognition, newItem: Recognition): Boolean {
            return oldItem.label == newItem.label
        }

        override fun areContentsTheSame(oldItem: Recognition, newItem: Recognition): Boolean {
            return oldItem.confidence == newItem.confidence
        }
    }

    inner class RecognitionViewHolder(private val binding: RecognitionItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val displayName: TextView = binding.recognitionDisplayName
        val probability: TextView = binding.recognitionProb

        init{

        }
        /*fun bindTo(recognition: Recognition) {
            //binding.recognitionItem = recognition
            //binding.executePendingBindings()
            binding.recognitionDisplayName.text = getString(flowerResolver.getDisplayName(recognition.label))
        }*/
    }


}

