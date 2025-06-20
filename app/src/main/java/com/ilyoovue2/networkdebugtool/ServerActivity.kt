package com.ilyoovue2.networkdebugtool

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ilyoovue2.networkdebugtool.databinding.FragmentServerBinding
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.ServerSocket
import java.net.Socket
import java.net.NetworkInterface
import kotlin.concurrent.thread

class ServerActivity : AppCompatActivity() {

    private lateinit var binding: FragmentServerBinding
    private var serverSocket: ServerSocket? = null
    private var clientSocket: Socket? = null
    private var inputStreamReader: InputStreamReader? = null
    private var bufferedReader: BufferedReader? = null
    private var outputStreamWriter: OutputStreamWriter? = null
    private var isRunning: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentServerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.startServerButton.setOnClickListener {
            val port = binding.portEditText.text.toString().toIntOrNull()
            if (port != null) {
                thread(start = true) {
                    startServer(port)
                }
            } else {
                Toast.makeText(this, "请输入端口", Toast.LENGTH_SHORT).show()
            }
        }

        binding.stopServerButton.setOnClickListener {
            stopServer()
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

    private fun getLocalIpAddress(): String {
        try {
            val en = NetworkInterface.getNetworkInterfaces()
            while (en.hasMoreElements()) {
                val intf = en.nextElement()
                val enumIpAddr = intf.inetAddresses
                while (enumIpAddr.hasMoreElements()) {
                    val inetAddress = enumIpAddr.nextElement()
                    if (!inetAddress.isLoopbackAddress && inetAddress.address.size == 4) {
                        return inetAddress.hostAddress
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return "未知IP"
    }

    private fun startServer(port: Int) {
        try {
            serverSocket = ServerSocket(port)
            runOnUiThread {
                binding.statusTextView.append("服务器端口为 $port\n")
                binding.localIpTextView.text = "本地服务器IP: ${getLocalIpAddress()}"
            }
            while (isRunning) {
                clientSocket = serverSocket?.accept()
                runOnUiThread {
                    binding.statusTextView.append("客户端已连接\n")
                }
                inputStreamReader = InputStreamReader(clientSocket?.getInputStream())
                bufferedReader = BufferedReader(inputStreamReader!!)
                outputStreamWriter = OutputStreamWriter(clientSocket?.getOutputStream())

                // Start a separate thread to read messages from the client
                thread(start = true) {
                    while (clientSocket?.isConnected == true) {
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
            }
        } catch (e: Exception) {
            e.printStackTrace()
            runOnUiThread {
                binding.statusTextView.append("启动服务器错误: ${e.message}\n")
            }
        }
    }

    private fun stopServer() {
        isRunning = false
        try {
            bufferedReader?.close()
            inputStreamReader?.close()
            outputStreamWriter?.close()
            clientSocket?.close()
            serverSocket?.close()
            runOnUiThread {
                binding.statusTextView.append("服务器已关闭\n")
                binding.localIpTextView.text = "本地服务器IP:"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            runOnUiThread {
                binding.statusTextView.append("关闭服务器错误: ${e.message}\n")
            }
        } finally {
            bufferedReader = null
            inputStreamReader = null
            outputStreamWriter = null
            clientSocket = null
            serverSocket = null
        }
    }

    private fun sendMessage(message: String) {
        if (outputStreamWriter == null || !clientSocket!!.isConnected) {
            runOnUiThread {
                Toast.makeText(this, "无已连接客户端", Toast.LENGTH_SHORT).show()
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



