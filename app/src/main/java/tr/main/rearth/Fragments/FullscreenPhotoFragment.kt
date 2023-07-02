package tr.main.rearth.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import tr.main.rearth.databinding.FragmentFullscreenPhotoBinding


class FullscreenPhotoFragment : Fragment() {
    private var _binding: FragmentFullscreenPhotoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentFullscreenPhotoBinding.inflate(inflater, container, false)
        return binding.root
    }

}