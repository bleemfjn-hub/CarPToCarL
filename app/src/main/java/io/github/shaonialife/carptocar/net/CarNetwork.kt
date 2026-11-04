package io.github.shaonialife.carptocar.net

import android.net.Network
import java.net.DatagramSocket
import java.net.Socket

/**
 * 车机热点网络（socket 级绑定）。
 *
 * 背景：VPN（Clash/mihomo 的 TUN）在场时，进程级 `bindProcessToNetwork` 会被 VPN 的
 * underlying network 覆盖，业务 socket 仍然走 tun0 —— 表现为「蓝牙连上、AirPlay 连不上」，
 * 日志里是 `wifi=[tun0/172.19.0.1]`（2026-11-03 12:41 实测）。
 *
 * 这里改成**逐 socket 绑定**：连接车机用的 TCP/UDP 都从热点 `Network` 的 socketFactory 建，
 * 或建好后 `Network.bindSocket()` 绑上去。这样即使 Clash 开着，这条链路也不会被劫走。
 *
 * 未加入热点（network == null）时自动退回普通构造，行为与改动前一致。
 */
object CarNetwork {
    @Volatile
    var network: Network? = null
        private set

    fun use(network: Network?) {
        this.network = network
    }

    fun tcp(): Socket = network?.socketFactory?.createSocket() ?: Socket()

    fun tcp(
        host: String,
        port: Int,
    ): Socket = network?.socketFactory?.createSocket(host, port) ?: Socket(host, port)

    fun udp(): DatagramSocket = DatagramSocket().also { bindUdp(it) }

    fun udp(port: Int): DatagramSocket = DatagramSocket(port).also { bindUdp(it) }

    private fun bindUdp(socket: DatagramSocket) {
        runCatching { network?.bindSocket(socket) }
    }
}
