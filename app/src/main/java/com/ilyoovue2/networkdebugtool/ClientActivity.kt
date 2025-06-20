package com.ilyoovue2.networkdebugtool

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ilyoovue2.networkdebugtool.databinding.ActivityClientBinding
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.Socket
import java.net.SocketTimeoutException
import kotlin.concurrent.thread

class ClientActivity : AppCompatActivity() {

    private lateinit var binding: ActivityClientBinding
    private var socket: Socket? = null
    private var inputStreamReader: InputStreamReader? = null
    private var bufferedReader: BufferedReader? = null
    private var outputStreamWriter: OutputStreamWriter? = null
    private var isRunning: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClientBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.connectButton.setOnClickListener {
            val ipAddress = binding.ipAddressEditText.text.toString()
            val port = binding.portEditText.text.toString().toIntOrNull()
            if (ipAddress.isNotEmpty() && port != null) {
                thread(start = true) {
                    connectToServer(ipAddress, port)
                }
            } else {
                Toast.makeText(this, "请输入IP和端口", Toast.LENGTH_SHORT).show()
            }
        }

        binding.disconnectButton.setOnClickListener {
            thread(start = true) {
                disconnectFromServer()
            }
        }

        binding.sendButton.setOnClickListener {
            val message = binding.messageEditText.text.toString()
            if (message.isNotEmpty()) {
                thread(start = true) {
                    sendMessage(message)
                }
            } else {
                Toast.makeText(this, "发送前请输入消息", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun connectToServer(ipAddress: String, port: Int) {
        try {
            // Set a timeout for the connection attempt
            socket = Socket().apply {
                connect(java.net.InetSocketAddress(ipAddress, port), 5000) // 5 seconds timeout
            }
            runOnUiThread {
                binding.statusTextView.append("连接到服务器 $ipAddress:$port\n")
            }
            inputStreamReader = InputStreamReader(socket?.getInputStream())
            bufferedReader = BufferedReader(inputStreamReader!!)
            outputStreamWriter = OutputStreamWriter(socket?.getOutputStream())

            // Start a separate thread to read messages from the server
            thread(start = true) {
                while (isRunning && socket?.isConnected == true) {
                    val receivedMessage = bufferedReader?.readLine()
                    if (receivedMessage != null) {
                        runOnUiThread {
                            binding.messagesTextView.append("收到: $receivedMessage\n")
                        }
                    } else {
                        break // Exit loop if connection is closed or no more data is available
                    }
                }
            }
        } catch (e: SocketTimeoutException) {
            e.printStackTrace()
            runOnUiThread {
                binding.statusTextView.append("连接超时\n")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            runOnUiThread {
                binding.statusTextView.append("连接服务器错误: ${e.message}\n")
            }
        }
    }

    private fun disconnectFromServer() {
        isRunning = false
        try {
            bufferedReader?.close()
            inputStreamReader?.close()
            outputStreamWriter?.close()
            socket?.close()
            runOnUiThread {
                binding.statusTextView.append("断开连接\n")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            runOnUiThread {
                binding.statusTextView.append("断开连接错误: ${e.message}\n")
            }
        } finally {
            bufferedReader = null
            inputStreamReader = null
            outputStreamWriter = null
            socket = null
        }
    }

    private fun sendMessage(message: String) {
        if (outputStreamWriter == null || !socket!!.isConnected) {
            runOnUiThread {
                Toast.makeText(this, "无已连接服务器", Toast.LENGTH_SHORT).show()
            }
            return
        }
        try {
            outputStreamWriter?.write("$message\n")
            outputStreamWriter?.flush()
            runOnUiThread {
                binding.messagesTextView.append("发送: $message\n")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            runOnUiThread {
                binding.statusTextView.append("发送错误: ${e.message}\n")
            }
        }
    }
}



