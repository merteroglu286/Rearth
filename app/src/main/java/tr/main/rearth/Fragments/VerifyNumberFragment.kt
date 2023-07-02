package tr.main.rearth.Fragments

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import tr.main.rearth.Activities.GetUserDataActivity
import tr.main.rearth.Activities.SplashScreenActivity
import tr.main.rearth.ConversationsModel
import tr.main.rearth.UserModel
import tr.main.rearth.databinding.FragmentVerifyNumberBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.*
import com.google.gson.Gson

class VerifyNumberFragment : Fragment() {

    private var _binding: FragmentVerifyNumberBinding? = null
    private val binding get() = _binding!!

    private var firebaseAuth: FirebaseAuth? = null
    private lateinit var databaseReference: DatabaseReference
    private lateinit var sharedPreferences : SharedPreferences
    private lateinit var userArrayList: ArrayList<UserModel>
    private lateinit var conversationsArrayList: ArrayList<ConversationsModel>

    private lateinit var sharedPreferences2: SharedPreferences
    private var code : String? = null
    private lateinit var pin: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            code = it.getString("Code")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentVerifyNumberBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().getReference("Users")
        firebaseAuth = FirebaseAuth.getInstance()
        userArrayList = arrayListOf<UserModel>()

        conversationsArrayList = arrayListOf<ConversationsModel>()
        conversationsArrayList.add(ConversationsModel("","","","","","",false))

        binding.btnVerify.setOnClickListener {
            if(checkPin()){
                val credential = PhoneAuthProvider.getCredential(code!!,pin)
                signInUser(credential)
            }
            val inputMethodManager = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(activity?.currentFocus?.windowToken, 0)
            loading(true)
        }

    }

    private fun signInUser(credential: PhoneAuthCredential) {

        firebaseAuth!!.signInWithCredential(credential).addOnCompleteListener {
            if(it.isSuccessful){

                conversationsArrayList = arrayListOf<ConversationsModel>()

                conversationsArrayList.add(ConversationsModel("","","","","","",true))


                sharedPreferences = requireContext().getSharedPreferences("Conversations", Context.MODE_PRIVATE)
                var editor = sharedPreferences.edit()
                var gson = Gson()
                var json : String = gson.toJson(conversationsArrayList)
                editor.putString("user",json)
                editor.apply()


                val ref: DatabaseReference =
                    FirebaseDatabase.getInstance().getReference("Users").child(firebaseAuth!!.uid.toString())

                ref.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val user = snapshot.getValue(UserModel::class.java)
                        checkIfFragmentAttached {
                            if (user?.kayitliMi == true){

                                sharedPreferences2 = requireContext()!!.getSharedPreferences("MY_INFO", Context.MODE_PRIVATE)
                                var editor2 = sharedPreferences2.edit()

                                editor2.putString("name",user.username)
                                editor2.putString("image",user.image)
                                editor2.putString("uid",user.uid)
                                editor2.putString("status",user.status)
                                editor2.putString("followers",user.followers)
                                editor2.putString("following",user.following)
                                editor2.putBoolean("kayitliMi",true)
                                editor2.apply()

                                startActivity(Intent(this, SplashScreenActivity::class.java))
                                requireActivity().finish()
                            }else{

                                startActivity(Intent(this, GetUserDataActivity::class.java))
                                requireActivity().finish()
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })
                /*
                requireActivity().supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.main_container, GetUserDataFragment())
                    .commit();
                 */

            }
        }
    }

    companion object {

        @JvmStatic
        fun newInstance(code: String,) =
            VerifyNumberFragment().apply {
                arguments = Bundle().apply {
                    putString("Code", code)
                }
                print("80.satır")
            }
    }

    private fun checkPin(): Boolean{
        pin= binding.otpTextView.text.toString()
        if(pin.isEmpty()) {
            binding.otpTextView.error = "Alan gereklidir."
            return false
        } else if (pin.length<6){
            binding.otpTextView.error = " Geçerli pini giriniz."
            return false
        }else return true
    }

    private fun loading(isLoading:Boolean){

        if(isLoading){
            binding.progressBar.visibility = View.VISIBLE
        }else{
            binding.progressBar.visibility = View.GONE
        }
    }

    fun checkIfFragmentAttached(operation: Context.() -> Unit) {
        if (isAdded && context != null) {
            operation(requireContext())
        }
    }
}