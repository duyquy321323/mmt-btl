package com.mmt.btl.config;

// import java.util.Map;

// import org.springframework.messaging.handler.annotation.MessageMapping;
// import org.springframework.messaging.handler.annotation.SendTo;
// import org.springframework.messaging.simp.SimpMessagingTemplate;
// import org.springframework.web.bind.annotation.RestController;

// @RestController
// public class WebSocketController {

//     private final SimpMessagingTemplate messagingTemplate;

//     public WebSocketController(SimpMessagingTemplate messagingTemplate) {
//         this.messagingTemplate = messagingTemplate;
//     }

//     @MessageMapping("/sendMessageServer") // Nhận tin nhắn từ client tại endpoint /app/sendMessage
//     @SendTo("/topic/server") // Gửi tin nhắn đến các client qua kênh /server
//     public String processMessageServerFromClient(String message) {
//         return "Server received: " + message;
//     }

//     // Gửi tin nhắn đến tất cả các client từ server
//     // hàm cho phép gửi tin chủ động mà không cần nhận tin từ client trước
//     public void sendMessageServerToClients(String message) {
//         messagingTemplate.convertAndSend("/topic/server", message);
//     }

//     public void sendMessageServerToClients(Map<String, Object> message) {
//         messagingTemplate.convertAndSend("/topic/server", message);
//     }

//     @MessageMapping("/sendMessageTracker") // Nhận tin nhắn từ client tại endpoint /app/sendMessage
//     @SendTo("/topic/tracker") // Gửi tin nhắn đến các client qua kênh /tracker
//     public String processMessageTrackerFromClient(String message) {
//         return "Server received: " + message;
//     }

//     // Gửi tin nhắn đến tất cả các client từ tracker
//     // hàm cho phép gửi tin chủ động mà không cần nhận tin từ client trước
//     public void sendMessageTrackerToClients(String message) {
//         messagingTemplate.convertAndSend("/topic/tracker", message);
//     }

//     public void sendMessageTrackerToClients(Map<String, Object> message) {
//         messagingTemplate.convertAndSend("/topic/tracker", message);
//     }
// }

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mmt.btl.repository.TrackerRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Configuration
public class MultiSocketServer {

    final private TrackerRepository trackerRepository;

    // Lưu danh sách PrintWriter của từng cổng
    private final static Map<Integer, PrintWriter> clientWriters = new HashMap<>();
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @Bean
    public void createSocket() {
        List<Integer> ports = trackerRepository.findAll()
                .stream()
                .map(it -> Integer.valueOf(it.getPort().toString()))
                .collect(Collectors.toList());
        String hostname = "localhost";

        for (int port : ports) {
            executorService.submit(() -> runServer(port, hostname));
        }
    }

    private void runServer(int port, String hostname) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Tracker đang chạy trên " + hostname + ":" + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client kết nối trên cổng " + port);

                new Thread(() -> handleClient(clientSocket, port)).start();
            }
        } catch (IOException e) {
            System.err.println("Lỗi ở cổng " + port + ": " + e.getMessage());
        }
    }

    private void handleClient(Socket clientSocket, int port) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("Client trên cổng " + port + " gửi: " + message);
                clientWriters.put(port, out);
                out.println("Tracker trên cổng " + port + " đã nhận: " + message);
            }
        } catch (IOException e) {
            System.err.println("Lỗi khi xử lý client trên cổng " + port + ": " + e.getMessage());
        }
    }

    // public void createSocket() {

    //     List<Integer> ports = trackerRepository.findAll().stream().map(it -> {return Integer.valueOf(it.getPort().toString());}).collect(Collectors.toList());
    //     String hostname = "localhost";
    //     // Tạo danh sách ServerSocket
    //     List<Thread> serverThreads = new ArrayList<>();

    //     for (int port : ports) {
    //         Thread serverThread = new Thread(() -> {
    //             try (ServerSocket serverSocket = new ServerSocket(port)) {
    //                 System.out.println("Tracker đang chạy trên " + hostname + ":" + port);

    //                 while (true) {
    //                     // Chấp nhận kết nối từ client
    //                     Socket clientSocket = serverSocket.accept();
    //                     System.out.println("Client kết nối trên cổng " + port);

    //                     // Xử lý tin nhắn từ client
    //                     try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    //                          PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
    //                             clientWriters.put(port, out);
    //                         String message = in.readLine();
    //                         System.out.println("Client gửi trên cổng " + port + ": " + message);

    //                         out.println("Tracker trên cổng " + port + " đã nhận: " + message);
    //                     }
    //                     clientSocket.close();
    //                 }
    //             } catch (IOException e) {
    //                 System.err.println("Lỗi ở cổng " + port + ": " + e.getMessage());
    //             }
    //         });

    //         // Chạy từng server trên một thread riêng
    //         serverThreads.add(serverThread);
    //         serverThread.start();
    //     }

    //     // Chờ tất cả các thread
    //     serverThreads.forEach(thread -> {
    //         try {
    //             thread.join();
    //         } catch (InterruptedException e) {
    //             e.printStackTrace();
    //         }
    //     });
    // }

    // Truy xuất danh sách PrintWriter của một cổng
    public static PrintWriter getWritersForPort(int port) {
        return clientWriters.get(port);
    }
}
