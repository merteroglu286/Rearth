package tr.main.rearth.Fragments.GetUserData

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import tr.main.rearth.databinding.FragmentKullaniciDogumTarihiBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class KullaniciDogumTarihiFragment : Fragment() {
    private var _binding: FragmentKullaniciDogumTarihiBinding? = null
    private val binding get() = _binding!!

    private var databaseReference: DatabaseReference? = null
    private var firebaseAuth: FirebaseAuth? = null

    private lateinit var uid: String
    private lateinit var birthday: String

    private lateinit var kullaniciAdi:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentKullaniciDogumTarihiBinding.inflate(inflater, container, false)

        firebaseAuth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().getReference("Users")

        val myCalendar = Calendar.getInstance()

        val datePicker = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, month)
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateLable(myCalendar)
        }

        binding.txtDate.setOnClickListener {
            DatePickerDialog(this.requireContext(),datePicker,myCalendar.get(Calendar.YEAR),myCalendar.get(Calendar.MONTH),myCalendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnNext.setOnClickListener{

            arguments?.let {
                kullaniciAdi = KullaniciDogumTarihiFragmentArgs.fromBundle(it).kullaniciAdi
            }

            uid = firebaseAuth!!.uid!!.toString()
            birthday = binding.txtDate.text.toString()

            val action = KullaniciDogumTarihiFragmentDirections.actionKullaniciDogumTarihiFragment2ToKullaniciCinsiyetFragment2(kullaniciAdi,birthday)
            Navigation.findNavController(it).navigate(action)
        }

        binding.btnBack.setOnClickListener{
            Navigation.findNavController(it).navigateUp()
        }

    }

    private fun updateLable(mycalendar: Calendar) {
        val myFormat = "dd/MM/yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.UK)
        binding.txtDate.setText(sdf. format(mycalendar. time))

    }
}

