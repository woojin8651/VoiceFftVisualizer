package com.example.live_voice_pitch_representer.ui.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.live_voice_pitch_representer.R
import com.example.live_voice_pitch_representer.databinding.VoicePitchFragmentBinding
import com.example.live_voice_pitch_representer.viewmodel.`interface`.VoicePitchViewModel
import com.example.live_voice_pitch_representer.viewmodel.implement.VoicePitchViewModelImpl

class VoicePitchFragment: Fragment() {

    companion object{
        val newInstance = VoicePitchFragment();
    }
    private lateinit var vm:VoicePitchViewModel
    private lateinit var binding:VoicePitchFragmentBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vm = ViewModelProvider(this).get(VoicePitchViewModelImpl::class.java)
        vm.audioConfigSet()
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = VoicePitchFragmentBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        binding.vm = vm as VoicePitchViewModelImpl?
        permissionCheck()
        (vm as VoicePitchViewModelImpl).isRecording.observe(this){
            binding.tvIsrecording.text = it.toString()
        }
        (vm as VoicePitchViewModelImpl).presentPitch.observe(this){
            binding.tvPresentPitch.text = it.toString()
        }
        (vm as VoicePitchViewModelImpl).graph.observe(this){
            binding.imageView.setImageBitmap(it)
        }
        super.onActivityCreated(savedInstanceState)
    }

    private fun permissionCheck(){
        if (ContextCompat.checkSelfPermission(activity!!,
                Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity!!,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                1234);
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode){
            1234->{
                if(grantResults.isNotEmpty()
                    &&grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    Log.d("VoiceFragment","permission Denied")
                else
                    Log.d("VoiceFragment","permission Denied")
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}