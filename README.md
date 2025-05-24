# GRPC

코딩 파트너

gRPC는 Google에서 개발한 오픈소스 원격 프로시저 호출(RPC) 프레임워크입니다. 클라이언트와 서버 애플리케이션 간의 효율적인 통신을 가능하게 합니다. gRPC는 HTTP/2를 프로토콜로 사용하고, Protocol Buffers를 데이터 형식으로 사용하여 높은 성능과 낮은 지연 시간을 제공합니다.

gRPC의 주요 특징은 다음과 같습니다.

- **프로토콜**: HTTP/2를 사용합니다. 일반적인 RESTful API가 주로 HTTP/1.1을 사용하는 것과 대조됩니다.
- **데이터 형식**: Protocol Buffers라는 바이너리 형식을 사용합니다. 이는 JSON이나 XML과 같은 텍스트 기반 형식에 비해 메시지 크기가 작아 효율적입니다.
- **API 계약**: `.proto` 파일을 통해 엄격한 API 계약을 정의합니다. 이를 통해 다양한 언어로 코드를 자동으로 생성할 수 있습니다.
- **통신 패턴**: 단방향(Unary) 통신뿐만 아니라 서버 스트리밍, 클라이언트 스트리밍, 양방향 스트리밍과 같은 다양한 통신 패턴을 지원합니다.
- **성능**: HTTP/2와 Protocol Buffers의 조합으로 인해 낮은 지연 시간과 높은 처리량을 제공하여 성능이 뛰어납니다.
- **코드 생성**: `.proto` 파일을 기반으로 다양한 프로그래밍 언어(자바, C++, Python 등)에서 클라이언트 및 서버 코드를 자동으로 생성할 수 있는 내장 기능을 제공합니다.
- **브라우저 지원**: gRPC-Web을 통해 브라우저 환경에서도 gRPC를 사용할 수 있지만, 프록시가 필요합니다.
- **가독성**: 바이너리 데이터 형식으로 인해 메시지의 가독성은 RESTful API의 텍스트 형식에 비해 낮습니다.

| 특징 | gRPC | 일반 Request (REST) |
| --- | --- | --- |
| 프로토콜 | HTTP/2 | 주로 HTTP/1.1 (HTTP/2 사용 가능) |
| 데이터 형식 | Protocol Buffers (바이너리) | JSON, XML (텍스트) |
| API 계약 | .proto 파일 (엄격, 코드 생성) | OpenAPI/Swagger, 문서 (상대적 유연) |
| 통신 패턴 | Unary, Server/Client/Bi-directional Streaming | 주로 Unary (요청-응답) |
| 코드 생성 | 내장 (다양한 언어 지원) | 주로 서드파티 도구 필요 |
| 성능 | 높음 (낮은 지연 시간, 높은 처리량) | 상대적으로 낮음 |
| 메시지 크기 | 작음 | 상대적으로 큼 |
| 브라우저 지원 | gRPC-Web (프록시 필요) | 네이티브 지원 |
| 가독성 | 낮음 (바이너리) | 높음 (텍스트) |

프로토콜 파일 예제

**src/main/proto/helloworld.proto**

```
`syntax = "proto3"; // 사용할 프로토콜 버퍼 버전 지정

option java_multiple_files = true; // 자바 코드를 여러 파일로 생성할지 여부
option java_package = "io.grpc.examples.helloworld"; // 생성될 자바 코드의 패키지 이름
option java_outer_classname = "HelloWorldProto"; // 모든 것을 포함하는 외부 클래스 이름 (multiple_files=false 일 때 주로 사용)

package helloworld; // 프로토콜 버퍼 네임스페이스

// Greeter 서비스 정의
service Greeter {
  // SayHello 메서드 정의 (Unary RPC)
  rpc SayHello (HelloRequest) returns (HelloReply) {}
}

// SayHello 요청 메시지 정의
message HelloRequest {
  string name = 1; // 클라이언트가 보낼 이름 (필드 번호 1)
}

// SayHello 응답 메시지 정의
message HelloReply {
  string message = 1; // 서버가 보낼 인사말 (필드 번호 1)
}`

```

build.gradle

```java
plugins {
    id 'java'
    id 'com.google.protobuf' version '0.9.4' // 프로토콜 버퍼 플러그인
    id 'eclipse' // (선택) IntelliJ IDEA 설정
}

group 'io.grpc.examples'
version '1.0-SNAPSHOT'

repositories {
    mavenCentral()
}

// 프로토콜 버퍼 설정
protobuf {
    protoc {
        // gRPC 코드 생성을 위한 protoc 실행 파일 경로 지정
        artifact = "com.google.protobuf:protoc:3.25.1"
    }
    plugins {
        grpc {
            // gRPC Java 플러그인 경로 지정
            artifact = "io.grpc:protoc-gen-grpc-java:1.64.0" // 최신 버전 확인 권장
        }
    }
    generateProtoTasks {
        all().each { task ->
            task.builtins {
                java {} // 기본 자바 코드 생성
            }
            task.plugins {
                grpc {} // gRPC 자바 코드 생성
            }
        }
    }
}

// gRPC 및 관련 의존성 추가
dependencies {
    // gRPC 관련 라이브러리 (최신 버전 확인 권장)
    implementation 'io.grpc:grpc-netty-shaded:1.64.0'
    implementation 'io.grpc:grpc-protobuf:1.64.0'
    implementation 'io.grpc:grpc-stub:1.64.0'
	implementation 'io.grpc:grpc-services:1.64.0' // 리플렉션 서비스 포함 (버전은 gRPC 버전에 맞게)
	implementation 'io.grpc:grpc-reflection:1.64.0' // 리플렉션 클라이언트 사용 시 필요할 수 있음
    // JSR 305 어노테이션 (gRPC에서 필요)
    compileOnly 'org.apache.tomcat:annotations-api:6.0.53' // 또는 'javax.annotation:javax.annotation-api:1.3.2'

    // 테스트용 (선택)
    testImplementation 'io.grpc:grpc-testing:1.64.0'
    testImplementation platform('org.junit:junit-bom:5.9.1')
    testImplementation 'org.junit.jupiter:junit-jupiter'
}

// Java 버전 설정
java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

test {
    useJUnitPlatform()
}

```

프로토콜 파일일을 src/main/proto 에 위치시키고 아래 명령을 수행하면 스펙에 따른 코드가 자동생성된다.

```bash
./gradlew generateProto

```

생성된 코드를 아래와 같이 구현

```java
package io.grpc.examples.helloworld;

import io.grpc.stub.StreamObserver;

// 생성된 GreeterGrpc.GreeterImplBase를 상속받아 서비스 구현
public class GreeterImpl extends GreeterGrpc.GreeterImplBase {

    // SayHello 메서드를 오버라이드하여 실제 로직 구현
    @Override
    public void sayHello(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
        // 클라이언트로부터 받은 이름으로 인사말 생성
        String message = "Hello " + request.getName();
        // HelloReply 메시지 빌드
        HelloReply reply = HelloReply.newBuilder().setMessage(message).build();

        // onNext()를 호출하여 클라이언트에게 응답 전송
        responseObserver.onNext(reply);
        // onCompleted()를 호출하여 RPC 호출 완료를 알림
        responseObserver.onCompleted();

        System.out.println("Server received: " + request.getName());
        System.out.println("Server responded: " + message);
    }
}

```

서버 코드

```java
package io.grpc.examples.helloworld;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class HelloWorldServer {
    private static final Logger logger = Logger.getLogger(HelloWorldServer.class.getName());

    private Server server;

    // 서버 시작 메서드
    private void start() throws IOException {
        int port = 50051; // 서버가 리스닝할 포트
        server = ServerBuilder.forPort(port)
                .addService(new GreeterImpl()) // 구현한 Greeter 서비스 추가
                .build()
                .start(); // 서버 시작
        logger.info("Server started, listening on " + port);

        // JVM 종료 시 서버를 깔끔하게 종료하기 위한 훅 등록
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                try {
                    HelloWorldServer.this.stop();
                } catch (InterruptedException e) {
                    e.printStackTrace(System.err);
                }
                System.err.println("*** server shut down");
            }
        });
    }

    // 서버 종료 메서드
    private void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    // 서버가 종료될 때까지 대기
    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        final HelloWorldServer server = new HelloWorldServer();
        server.start(); // 서버 시작
        server.blockUntilShutdown(); // 서버 종료까지 대기
    }
}

```

클라이언트 코드

```java
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
        logger.info("HTTP Server started, listening on " + httpPort + ". Visit <http://localhost>:" + httpPort + "/docs");

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

```
