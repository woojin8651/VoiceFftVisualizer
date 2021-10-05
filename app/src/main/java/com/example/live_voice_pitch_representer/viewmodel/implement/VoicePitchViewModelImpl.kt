package com.example.live_voice_pitch_representer.viewmodel.implement

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.util.Log
import androidx.core.graphics.toColor
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.example.live_voice_pitch_representer.Util.FFT
import com.example.live_voice_pitch_representer.viewmodel.`interface`.VoicePitchViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.annotations.SchedulerSupport.IO
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.Observables
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jtransforms.fft.DoubleFFT_1D
import org.reactivestreams.Subscription
import java.lang.Math.pow
import java.lang.StringBuilder
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

class VoicePitchViewModelImpl:VoicePitchViewModel,ViewModel() {
    companion object{
        private val TAG = "VoicePitchViewModelImpl"
    }
    private var mAudioRecord:AudioRecord? = null
    private val mAudioSource = MediaRecorder.AudioSource.MIC
    private val mSampleRate = 44100
    private val mChannelCount = AudioFormat.CHANNEL_IN_MONO
    private val mAudioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val mBufferSize = AudioRecord.getMinBufferSize(mSampleRate,mChannelCount,mAudioFormat)
    private val mBlockSize = 4096
    private lateinit var presentFFTSample:ShortArray
    private lateinit var canvas: Canvas
    private lateinit var drawMap: Bitmap
    private val paint = Paint().apply {
        color = Color.BLUE
    }

    private val _isRecording = MutableLiveData<Boolean>();
    val isRecording:LiveData<Boolean> get() = _isRecording;

    private val _presentPitch = MutableLiveData<Int>();
    val presentPitch:LiveData<Int> get() = _presentPitch;

    private val _grapgh = MutableLiveData<Bitmap>()
    val graph :LiveData<Bitmap> get() = _grapgh


    override fun audioConfigSet() {
        _isRecording.value = false;
        _presentPitch.value = 0;
        drawMap = Bitmap.createBitmap(256,256,Bitmap.Config.ARGB_8888)
        _grapgh.value = drawMap
        canvas = Canvas(drawMap)
        presentFFTSample = ShortArray(mBlockSize)

    }

    override fun toggleIsStart() {
        _isRecording.value = !isRecording.value!!
        Log.d(TAG,"Toggled ${isRecording.value}")
        if(isRecording.value == true)
            recordAndComputePitch()
    }
    private fun drawing(mag:DoubleArray){
        canvas.drawColor(Color.WHITE)
        run{
            mag.indices.forEach {
                if(it.toFloat()>255.0) return@run
                canvas.drawLine(it.toFloat(), (255.0-mag[it]).toFloat(),it.toFloat(),255.0.toFloat(),paint,)
            }
        }

        _grapgh.postValue(drawMap)
    }
    private fun recordAndComputePitch() {

        mAudioRecord = AudioRecord(mAudioSource, mSampleRate, mChannelCount, mAudioFormat, mBufferSize)
        mAudioRecord?.startRecording()
        CoroutineScope(Dispatchers.Default).launch {
            recording()
        }
    }
    private fun recording(){
        var readData = ShortArray(mBlockSize)
        Observable.create<PCMReturn>{
            while(isRecording.value == true){
                mAudioRecord?.read(readData,0,mBlockSize).let { size ->
                    it.onNext(PCMReturn(size!!, readData))
                }
            }
            mAudioRecord?.stop()
            mAudioRecord?.release()
            it.onComplete()
        }.subscribeOn(Schedulers.io())
            .subscribe{
                drawing(fftAnalyze(it.size,it.buffer))
            }
    }
    private fun fftAnalyze(size:Int,sample:ShortArray):DoubleArray{
        var toTransform = DoubleArray(mBlockSize){0.0}
        var img = DoubleArray(mBlockSize){0.0}
        var mag = DoubleArray(mBlockSize/2){0.0}
        var postvalue = 0
        for(i in 0 until min(size,mBlockSize)){
            toTransform[i] = sample[i].toDouble()/Short.MAX_VALUE
        }
        var doFFT = FFT(mBlockSize)
        doFFT.fft(toTransform,img)
        for(i in 0 until mBlockSize/2){
            mag[i] = sqrt(toTransform[i].pow(2) + img[i].pow(2))
        }

        postvalue = mag.map{ if(it>100) it else 0.0 }.let {
            it.indices.maxByOrNull { idx->
                it[idx]
            }?: -1
        }
        _presentPitch.postValue(postvalue*mSampleRate/mBlockSize)
        return mag
    }
    private fun List<Int>.shiftAndSet(present:Int = 0):List<Int>{
        val ret = mutableListOf<Int>()
        for(i in 0 until this.size - 1)
            ret.add(this[i+1])
        ret.add(present)
        return ret;
    }
    data class PCMReturn(val size:Int,val buffer:ShortArray)
}