package tr.main.rearth.Fragments.GetUserData

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import tr.main.rearth.R
import tr.main.rearth.databinding.FragmentKullaniciCinsiyetBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class KullaniciCinsiyetFragment : Fragment() {
    private var _binding: FragmentKullaniciCinsiyetBinding? = null
    private val binding get() = _binding!!

    private var databaseReference: DatabaseReference? = null
    private var firebaseAuth: FirebaseAuth? = null

    private lateinit var uid: String
    private lateinit var gender: String

    var femaleBool : Boolean = false
    var maleBool : Boolean = false

    private lateinit var kullaniciAdi : String
    private lateinit var dogumTarihi: String



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentKullaniciCinsiyetBinding.inflate(inflater, container, false)

        firebaseAuth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().getReference("Users")

        binding.btnFemale.setOnClickListener {
            femaleBool = true
            maleBool = false

            binding.btnFemale.setBackgroundResource(R.drawable.female_button)
            binding.btnFemale.setImageResource(R.drawable.ic_baseline_female_24)

            binding.btnMale.setBackgroundResource(R.drawable.gender_button)
            binding.btnMale.setImageResource(R.drawable.ic_baseline_male_gray)

        }

        binding.btnMale.setOnClickListener {
            femaleBool = false
            maleBool = true
            binding.btnMale.setBackgroundResource(R.drawable.male_button)
            binding.btnMale.setImageResource(R.drawable.ic_baseline_male_24)

            binding.btnFemale.setBackgroundResource(R.drawable.gender_button)
            binding.btnFemale.setImageResource(R.drawable.ic_baseline_female_24_gray)
        }



        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnNext.setOnClickListener{

            if (femaleBool == true){
                gender = "kadin"
            }
            else if(maleBool == true){
                gender = "erkek"
            }else{
                gender = ""
            }

            arguments?.let {
                kullaniciAdi = KullaniciCinsiyetFragmentArgs.fromBundle(it).kullaniciAdi
                dogumTarihi = KullaniciCinsiyetFragmentArgs.fromBundle(it).dogumTarihi
            }


            uid = firebaseAuth!!.uid!!.toString()

            val action = KullaniciCinsiyetFragmentDirections.actionKullaniciCinsiyetFragment2ToKullaniciFotografFragment2(kullaniciAdi,dogumTarihi,gender)
            Navigation.findNavController(it).navigate(action)
        }

        binding.btnBack.setOnClickListener{
            Navigation.findNavController(it).navigateUp()
        }
    }
}