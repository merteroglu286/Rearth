package tr.main.rearth.Activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import tr.main.rearth.Adapters.DashboardViewPagerAdapter
import tr.main.rearth.Dashboard.ChatFragment
import tr.main.rearth.Dashboard.MapsFragment
import tr.main.rearth.Dashboard.ProfileFragment
import tr.main.rearth.R
import tr.main.rearth.ViewModels.ProfileViewModel
import tr.main.rearth.databinding.ActivityDashboardBinding

class DashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardBinding
    private lateinit var profileViewModels: ProfileViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        profileViewModels = ViewModelProvider.AndroidViewModelFactory.getInstance(application).create(
            ProfileViewModel::class.java)

        profileViewModels.getUser().observe(this, androidx.lifecycle.Observer { userModel->
            binding.userModel = userModel

            //Picasso.get().load(userModel.image).into(binding.imgProfile)
        })

        val viewPager = binding.viewPager


        val fragments: ArrayList<Fragment> = arrayListOf(
            ChatFragment(),
            ProfileFragment()
        )

        val adapter = DashboardViewPagerAdapter(fragments,this)
        viewPager.adapter = adapter
        //viewPager.currentItem = 1


        binding.bottomBarFabMap.setOnClickListener{
            supportFragmentManager
                .beginTransaction()
                .setCustomAnimations(R.anim.enter_from_left,R.anim.exit_to_right,R.anim.enter_from_right,R.anim.exit_to_left)
                .replace(R.id.dashboardContainer, MapsFragment())
                .addToBackStack(null)
                .commit()
        }
    }

}