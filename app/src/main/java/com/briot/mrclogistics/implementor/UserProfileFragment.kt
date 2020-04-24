package com.briot.mrclogistics.implementor

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.briot.mrclogistics.implementor.repository.local.PrefConstants
import com.briot.mrclogistics.implementor.repository.local.PrefRepository
import kotlinx.android.synthetic.main.user_profile_fragment.*


class UserProfileFragment : Fragment() {

    companion object {
        fun newInstance() = UserProfileFragment()
    }

    private lateinit var viewModel: UserProfileViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.user_profile_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
        viewModel = ViewModelProvider(this).get(UserProfileViewModel::class.java)

        (this.activity as AppCompatActivity).setTitle("User Profile")


        val userName = PrefRepository.singleInstance.getValueOrDefault(PrefConstants().username, "")
        val deviceId = PrefRepository.singleInstance.getValueOrDefault(PrefConstants().deviceId, "")
       // val password = PrefRepository.singleInstance.getValueOrDefault(PrefConstants().password, "")
        val userId = PrefRepository.singleInstance.getValueOrDefault(PrefConstants().USER_ID, "")

        userNameValue.text = userName
       // userPassword.text = password
        userDeviceValue.text = deviceId
        userIdValue.text = userId
        this.activity?.invalidateOptionsMenu()
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.clear()
    }

}
