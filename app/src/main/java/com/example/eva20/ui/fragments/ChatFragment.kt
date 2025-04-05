package com.example.eva20.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eva20.R
import com.example.eva20.databinding.FragmentChatBinding
import com.example.eva20.ui.adapters.ChatAdapter
import com.example.eva20.ui.viewmodels.ChatViewModel
import com.example.eva20.utils.Logger
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class ChatFragment : Fragment() {
    private val TAG = "ChatFragment"
    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private lateinit var chatViewModel: ChatViewModel
    private lateinit var chatAdapter: ChatAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Log entry into chat fragment with timestamp
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        val currentTime = dateFormat.format(Date())
        Logger.d(TAG, "Fragment created at $currentTime by user: IAmLep")

        setupViewModel()
        setupRecyclerView()
        setupClickListeners()
        observeData()
    }

    private fun setupViewModel() {
        chatViewModel = ViewModelProvider(this)[ChatViewModel::class.java]
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter()
        binding.recyclerViewChat.apply {
            layoutManager = LinearLayoutManager(requireContext()).apply {
                // Scroll to the bottom of the list when loaded
                stackFromEnd = true
                reverseLayout = false
            }
            adapter = chatAdapter
        }
    }

    private fun setupClickListeners() {
        binding.buttonSend.setOnClickListener {
            val message = binding.editTextMessage.text.toString().trim()
            if (message.isNotEmpty()) {
                chatViewModel.sendMessage(message)
                binding.editTextMessage.text.clear()
            } else {
                Toast.makeText(context, "Message cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }

        binding.buttonCall.setOnClickListener {
            findNavController().navigate(R.id.action_chat_fragment_to_call_fragment)
            Logger.d(TAG, "Navigating to call fragment")
        }
    }

    private fun observeData() {
        chatViewModel.chatMessages.observe(viewLifecycleOwner) { messages ->
            chatAdapter.submitList(messages)
            if (messages.isNotEmpty()) {
                binding.recyclerViewChat.scrollToPosition(messages.size - 1)
            }
            binding.emptyView?.visibility = if (messages.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}