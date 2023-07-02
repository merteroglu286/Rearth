package tr.main.rearth.Activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import tr.main.rearth.Fragments.GetPhoneNumberFragment
import tr.main.rearth.R
import tr.main.rearth.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportFragmentManager.beginTransaction()
            .add(R.id.main_container, GetPhoneNumberFragment())
            .commit()


    }


}