package tr.main.rearth.ViewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import tr.main.rearth.Repository.AppRepo
import tr.main.rearth.UserModel

class ProfileViewModel:ViewModel() {
    private var appRepo = AppRepo.StaticFun.getInstance()

    fun getUser(): LiveData<UserModel> {
        return appRepo.getUser()
    }

    fun getHisUser(uid:String): LiveData<UserModel> {
        return appRepo.getHisUser(uid)
    }

    fun updateStatus(status: String) {
        appRepo.updateStatus(status)
    }

    fun updateName(userName: String?) {
        appRepo.updateName(userName!!)
    }

    fun updateImage(imagePath: String) {
        appRepo.updateImage(imagePath)
    }

}