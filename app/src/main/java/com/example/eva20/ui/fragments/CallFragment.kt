package com.example.eva20.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.eva20.databinding.FragmentCallBinding
import com.example.eva20.ui.viewmodels.CallViewModel
import com.example.eva20.utils.Logger

class CallFragment : Fragment() {

    private var _binding: FragmentCallBinding? = null
    private val binding get() = _binding!!

    private lateinit var callViewModel: CallViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCallBinding.inflate(inflater, container, false)

        // Make background semi-transparent to give overlay illusion
        binding.root.setBackgroundResource(com.example.eva20.R.color.semi_transparent_background)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Logger.d("CallFragment", "Fragment created")

        setupViewModel()
        setupClickListeners()
        initializeWebSocketConnection()
    }

    private fun setupViewModel() {
        callViewModel = ViewModelProvider(this)[CallViewModel::class.java]
    }

    private fun setupClickListeners() {
        binding.buttonEndCall.setOnClickListener {
            callViewModel.endCall()
            findNavController().navigateUp()
            Logger.d("CallFragment", "Call ended, navigating back")
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        callViewModel.disconnectWebSocket()
        _binding = null
    }
}