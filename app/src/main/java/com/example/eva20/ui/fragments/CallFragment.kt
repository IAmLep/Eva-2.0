package com.example.eva20.ui.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.eva20.R
import com.example.eva20.databinding.FragmentCallBinding
import com.example.eva20.ui.viewmodels.CallViewModel
import com.example.eva20.utils.Logger
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class CallFragment : Fragment() {
    private val TAG = "CallFragment"
    private var _binding: FragmentCallBinding? = null
    private val binding get() = _binding!!

    private lateinit var callViewModel: CallViewModel
    private var callDurationInSeconds = 0
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var timerRunnable: Runnable

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCallBinding.inflate(inflater, container, false)

        // Make background semi-transparent to give overlay illusion
        binding.root.setBackgroundResource(R.color.semi_transparent_background)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Log entry with timestamp
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        val currentTime = dateFormat.format(Date())
        Logger.d(TAG, "Call fragment created at $currentTime by user: IAmLep")

        setupViewModel()
        setupClickListeners()
        initializeWebSocketConnection()
        setupCallTimer()
    }

    private fun setupViewModel() {
        callViewModel = ViewModelProvider(this)[CallViewModel::class.java]
    }

    private fun setupClickListeners() {
        binding.buttonEndCall.setOnClickListener {
            callViewModel.endCall()
            findNavController().navigateUp()
            Logger.d(TAG, "Call ended, navigating back")
        }

        binding.buttonMute.setOnClickListener {
            callViewModel.toggleMute()
        }
    }

    private fun initializeWebSocketConnection() {
        callViewModel.connectWebSocket()

        callViewModel.connectionStatus.observe(viewLifecycleOwner) { status ->
            binding.textViewStatus.text = status
        }

        callViewModel.isMuted.observe(viewLifecycleOwner) { isMuted ->
            binding.buttonMute.setIconResource(
                if (isMuted) R.drawable.ic_mic_off else R.drawable.ic_mic
            )
        }
    }

    private fun setupCallTimer() {
        // Initialize timer
        timerRunnable = object : Runnable {
            override fun run() {
                callDurationInSeconds++
                binding.textViewDuration.text = formatDuration(callDurationInSeconds)
                callViewModel.updateCallDuration(callDurationInSeconds)
                handler.postDelayed(this, 1000)
            }
        }

        // Start timer
        handler.post(timerRunnable)
    }

    private fun formatDuration(seconds: Int): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(timerRunnable)
        callViewModel.disconnectWebSocket()
        _binding = null
    }
}