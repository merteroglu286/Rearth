package tr.main.rearth.Fragments.GetUserData

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import tr.main.rearth.databinding.FragmentKullaniciBilgilendirmeBinding
import tr.main.rearth.databinding.GetuserdataWhenClosedDialogBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class KullaniciBilgilendirmeFragment : Fragment() {

    private var _binding: FragmentKullaniciBilgilendirmeBinding? = null
    private val binding get() = _binding!!

    private lateinit var dialog: AlertDialog
    private lateinit var getuserdataWhenClosedDialogBinding: GetuserdataWhenClosedDialogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentKullaniciBilgilendirmeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnNext.setOnClickListener{
            val action = KullaniciBilgilendirmeFragmentDirections.actionKullaniciBilgilendirmeFragment2ToKullaniciAdiFragment2()
            Navigation.findNavController(it).navigate(action)
        }
        val databaseReference = FirebaseDatabase.getInstance().getReference("Users")
        val firebaseAuth = FirebaseAuth.getInstance()
        val uid = firebaseAuth!!.uid!!.toString()
        val map = mapOf(
            "kayitliMi" to false,
        )
        databaseReference!!.child(uid).updateChildren(map)

        binding.btnClose.setOnClickListener {

            val alertDialog = AlertDialog.Builder(requireContext())
            getuserdataWhenClosedDialogBinding = GetuserdataWhenClosedDialogBinding.inflate(layoutInflater)
            alertDialog.setView(getuserdataWhenClosedDialogBinding.root)

            dialog = alertDialog.create()

            getuserdataWhenClosedDialogBinding.btnYes.setOnClickListener {
                requireActivity().finish()
            }
            getuserdataWhenClosedDialogBinding.btnNo.setOnClickListener {
                dialog.dismiss()
            }
            dialog.show()

        }
    }

}