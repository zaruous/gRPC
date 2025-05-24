package gRpcExample;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.google.common.collect.ImmutableList;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer; // 자바 내장 HTTP 서버

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.examples.helloworld.GreeterImpl;
import io.grpc.protobuf.services.ProtoReflectionService;

public class HelloWorldServer {
    private static final Logger logger = Logger.getLogger(HelloWorldServer.class.getName());

    private Server grpcServer;
    private HttpServer httpServer;
    private final int grpcPort = 50051;
    private final int httpPort = 8080; // HTTP 서버 포트 (gRPC와 다르게 설정)

    private void start() throws IOException {
        // 1. gRPC 서버 시작 (리플렉션 포함)
        grpcServer = ServerBuilder.forPort(grpcPort)
                .addService(new GreeterImpl())
                .addService(ProtoReflectionService.newInstance())
                .build()
                .start();
        logger.info("gRPC Server started, listening on " + grpcPort);
        logger.info("Reflection service enabled.");

        // 2. HTTP 서버 시작
        httpServer = HttpServer.create(new InetSocketAddress(httpPort), 0);
        httpServer.createContext("/docs", new GrpcListHandler(grpcPort)); // /docs 경로 핸들러 등록
        httpServer.setExecutor(Executors.newCachedThreadPool()); // 기본 Executor 설정
        httpServer.start();
        logger.info("HTTP Server started, listening on " + httpPort + ". Visit http://localhost:" + httpPort + "/docs");


        // JVM 종료 훅 등록
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.err.println("*** shutting down gRPC and HTTP servers since JVM is shutting down");
                try {
                    HelloWorldServer.this.stop();
                } catch (InterruptedException e) {
                    e.printStackTrace(System.err);
                }
                System.err.println("*** servers shut down");
            }
        });
    }

    private void stop() throws InterruptedException {
        if (grpcServer != null) {
            grpcServer.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
        if (httpServer != null) {
            httpServer.stop(5); // 5초 대기 후 종료
        }
    }

    private void blockUntilShutdown() throws InterruptedException {
        if (grpcServer != null) {
            grpcServer.awaitTermination();
        }
    }

    // HTTP 요청을 처리하는 핸들러 클래스
    static class GrpcListHandler implements HttpHandler {
        private final int grpcPort;

        GrpcListHandler(int grpcPort) {
            this.grpcPort = grpcPort;
        }

        @Override
        public void handle(HttpExchange t) throws IOException {
            String responseBody = "<html><body><h1>gRPC Service List</h1>";
            ManagedChannel channel = null;
            try {
                // gRPC 서버에 연결하기 위한 채널 생성 (localhost)
                channel = ManagedChannelBuilder.forAddress("localhost", grpcPort)
                        .usePlaintext()
                        .build();

                // 리플렉션 클라이언트 생성
                ServerReflectionClient reflectionClient = ServerReflectionClient.create(channel);

                // 서버에 서비스 목록 요청
                ImmutableList<String> services = reflectionClient.listServices().get(10, TimeUnit.SECONDS);

//                List<String> services = listServiceResponse.getServiceList().stream()
//                        .map(ServiceResponse::getName)
//                        .collect(Collectors.toList());

                // HTML 생성
                responseBody += "<ul>";
                for (String service : services) {
                    // 리플렉션 서비스 자체는 제외 (선택 사항)
                    if (!service.startsWith("grpc.reflection")) {
                         responseBody += "<li>" + service + "</li>";
                         // TODO: 각 서비스의 메서드 목록을 가져오는 로직 추가 가능 (file_containing_symbol 사용)
                    }
                }
                responseBody += "</ul>";

            } catch (Exception e) {
                logger.warning("Failed to get gRPC services via reflection: " + e.getMessage());
                responseBody += "<p>Error: Could not retrieve service list. " + e.getMessage() + "</p>";
            } finally {
                if (channel != null) {
                    try {
                        channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
                    } catch (InterruptedException e) {
                       Thread.currentThread().interrupt();
                    }
                }
                responseBody += "</body></html>";
            }

            // HTTP 응답 전송
            t.sendResponseHeaders(200, responseBody.getBytes().length);
            OutputStream os = t.getResponseBody();
            os.write(responseBody.getBytes());
            os.close();
        }
    }


    public static void main(String[] args) throws IOException, InterruptedException {
        final HelloWorldServer server = new HelloWorldServer();
        server.start();
        server.blockUntilShutdown();
    }
}